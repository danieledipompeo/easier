package it.univaq.disim.sealab.metaheuristic;

import com.beust.jcommander.JCommander;
import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.evolutionary.*;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.RExecuteAlgorithms;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.RExperiment;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.RExperimentBuilder;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.util.RComputeQualityIndicators;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.util.RGenerateReferenceParetoFront;
import it.univaq.disim.sealab.metaheuristic.evolutionary.factory.FactoryBuilder;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRCrossover;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRMutation;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRSolutionListEvaluator;
import it.univaq.disim.sealab.metaheuristic.utils.*;
import org.uma.jmetal.lab.experiment.ExperimentBuilder;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Launcher {

    public static void main(String[] args) throws Exception {

        JCommander jc = new JCommander();

        jc.addObject(Configurator.eINSTANCE);
        jc.parse(args);

        UMLRCrossover crossoverOperator = new UMLRCrossover(Configurator.eINSTANCE.getXoverProbabiliy());

        List<Path> referenceFront = new ArrayList<>();
        double qThreshold = 0.1;

        if (Configurator.eINSTANCE.getReferenceFront() != null)
            referenceFront = Configurator.eINSTANCE.getReferenceFront();

        else {

            List<Path> modelsPath = new ArrayList<>(Configurator.eINSTANCE.getModelsPath());
            int i = 1;
            int[] eval = Configurator.eINSTANCE.getMaxEvaluation().stream().mapToInt(e -> e).toArray();

            for (Path m : modelsPath) {
                System.out.println("Number of source model");
                ProgressBar.showBar(i, modelsPath.size());
                List<RProblem<UMLRSolution>> rProblems = new ArrayList<>();
                for (int j = 0; j < eval.length; j++) {
                    rProblems.add(createProblems(m, eval[j]));

                    if (!m.getParent().resolve("output.xml").toFile().exists()) {
                        new WorkflowUtils().applyTransformation(m);
                        new WorkflowUtils().invokeSolver(m.getParent());
                    }
                    List<GenericIndicator<UMLRSolution>> qIndicators = new ArrayList<>();
                    FactoryBuilder<UMLRSolution> factory = new FactoryBuilder<>();
                    for (String qI : Configurator.eINSTANCE.getQualityIndicators()) {
                        GenericIndicator<UMLRSolution> ind = factory.createQualityIndicators(qI);
                        if (ind != null)
                            qIndicators.add(ind);
                    }
                    runExperiment(rProblems, qIndicators, crossoverOperator, eval[j]);
                    new UMLMemoryOptimizer().cleanup();
                    System.gc();
                }
                i++;
            }
        }
        EasierResourcesLogger.dumpToCSV();
    }

    public static List<Path> runExperiment(final List<RProblem<UMLRSolution>> rProblems,
                                           final List<GenericIndicator<UMLRSolution>> qualityIndicators,
                                           UMLRCrossover crossoverOperator, int eval) {
        final int INDEPENDENT_RUNS = Configurator.eINSTANCE.getIndependetRuns(); // should be 31 or 51
        final int CORES = 1;

        List<Path> refFront = new ArrayList<>();

        List<ExperimentProblem<UMLRSolution>> problemList = new ArrayList<>();

        rProblems.forEach(problem -> problemList.add(new ExperimentProblem<>(problem)));

        List<ExperimentAlgorithm<UMLRSolution, List<UMLRSolution>>> algorithmList =
                configureAlgorithmList(problemList, crossoverOperator,
                        eval);

        Path referenceFrontDirectory = Paths.get(Configurator.eINSTANCE.getOutputFolder().toString(), "referenceFront");

        List<String> tags = new ArrayList<>();

        if (Configurator.eINSTANCE.generateRF())
            problemList.forEach(p -> tags.add(p.getTag() + ".rf"));
        else
            problemList.forEach(p -> tags.add("super-reference-pareto.rf"));

        for (String tag : tags) {
            refFront.add(Paths.get(Configurator.eINSTANCE.getOutputFolder().toString(), "referenceFront", tag));
        }

        ExperimentBuilder<UMLRSolution, List<UMLRSolution>> experimentBuilder =
                new RExperimentBuilder<UMLRSolution, List<UMLRSolution>>(
                        "Exp").setAlgorithmList(algorithmList).setProblemList(problemList)
                        .setExperimentBaseDirectory(referenceFrontDirectory.toString())
                        .setReferenceFrontDirectory(referenceFrontDirectory.toString())
                        .setIndependentRuns(INDEPENDENT_RUNS).setNumberOfCores(CORES)
                        .setOutputParetoFrontFileName("FUN").setOutputParetoSetFileName("VAR")
                        .setIndicatorList(qualityIndicators);

        RExperiment<UMLRSolution, List<UMLRSolution>> experiment =
                ((RExperimentBuilder<UMLRSolution, List<UMLRSolution>>) experimentBuilder)
                        .setReferenceFrontFileNames(tags).build();
        try {
            new RExecuteAlgorithms<>(experiment).run();

            // Print experiment results to JSON file
            new FileUtils().experimentToJSON(EasierExperimentDAO.eINSTANCE);

            if (Configurator.eINSTANCE.generateRF())
                new RGenerateReferenceParetoFront(experiment).run();

            RComputeQualityIndicators<UMLRSolution, List<UMLRSolution>> qualityIndicator =
                    new RComputeQualityIndicators<>(
                            experiment);
            try {
                qualityIndicator.run();
            } catch (JMetalException e) {
                JMetalLogger.logger.warning(e.getMessage());
            }

            crossoverOperator.writeCrossoverReport(experiment.getExperimentBaseDirectory());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return refFront;

    }

    public static List<ExperimentAlgorithm<UMLRSolution, List<UMLRSolution>>> configureAlgorithmList(
            List<ExperimentProblem<UMLRSolution>> problemList, UMLRCrossover crossoverOperator, int eval) {

        List<ExperimentAlgorithm<UMLRSolution, List<UMLRSolution>>> algorithms = new ArrayList<>();
        FactoryBuilder<UMLRSolution> fBuilder = new FactoryBuilder<>();
        final SolutionListEvaluator<UMLRSolution> solutionListEvaluator = new UMLRSolutionListEvaluator<>();
        final MutationOperator<UMLRSolution> mutationOperator =
                new UMLRMutation(Configurator.eINSTANCE.getMutationProbability(),
                        Configurator.eINSTANCE.getDistributionIndex());

        String algo = Configurator.eINSTANCE.getAlgorithm();

        for (ExperimentProblem<UMLRSolution> expProblem : problemList) {
            algorithms.addAll(
                    fBuilder.configureAlgorithmList(expProblem, eval, crossoverOperator, solutionListEvaluator,
                            mutationOperator, algo));
        }

        return algorithms;

    }

    public static RProblem<UMLRSolution> createProblems(Path modelPath, int eval) {

        double probPas = Configurator.eINSTANCE.getProbPas();

        String brf = Configurator.eINSTANCE.getBrfList().toString().replace(":", "_").replace(",", "__")
                .replace(" ", "").replace("[", "").replace("]", "");
        String pName = String.format("%s__BRF_%s__MaxEval_%d__ProbPAs_%.2f__sb_%s_sbth_%s__Algo_%s",
                modelPath.getName(modelPath.getNameCount() - 2), brf, eval, probPas,
                Configurator.eINSTANCE.getSearchBudget(), Configurator.eINSTANCE.getSearchBudgetThreshold(),
                Configurator.eINSTANCE.getAlgorithm());

        if ("rs".equals(Configurator.eINSTANCE.getAlgorithm()))
            return new RandomSearchUMLRProblem<>(modelPath, pName);

        return new UMLRProblem<>(modelPath, pName);
    }

}
