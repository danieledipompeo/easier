package it.univaq.disim.sealab.metaheuristic;

import com.beust.jcommander.JCommander;
import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.evolutionary.ProgressBar;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.RExecuteAlgorithms;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.RExperiment;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.RExperimentBuilder;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.util.RComputeQualityIndicators;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.util.RGenerateReferenceParetoFront;
import it.univaq.disim.sealab.metaheuristic.evolutionary.factory.FactoryBuilder;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.RSolutionListEvaluator;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRCrossover;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRMutation;
import it.univaq.disim.sealab.metaheuristic.utils.*;
import it.univaq.easier.LQN;
import it.univaq.easier.Scenario;
import org.uma.jmetal.lab.experiment.ExperimentBuilder;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Launcher {

    public static void main(String[] args) throws Exception {

        JCommander jc = new JCommander();

        jc.addObject(Configurator.eINSTANCE);
        jc.parse(args);

        UMLRCrossover<UMLRSolution> crossoverOperator = new UMLRCrossover<>(Configurator.eINSTANCE.getXoverProbabiliy());

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
                        WorkflowUtils.applyTransformation(m);
                        WorkflowUtils.invokeSolver(m.getParent());
                    }

                    List<String> scenarios = getScenarios(m.getParent().resolve("output.lqxo"));

                    // Dynamically expand the list of scenarios by configuring the performance testing scenarios
                    // if the objectiveList in the config file contains "pricePerScenario" and/or "energyPerScenario"
                    // placeholders
                    Configurator.eINSTANCE.updateObjectiveList(scenarios);

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
        EasierResourcesLogger.dumpToJSON();
    }

    public static List<Path> runExperiment(final List<RProblem<UMLRSolution>> rProblems,
                                           final List<GenericIndicator<UMLRSolution>> qualityIndicators,
                                           UMLRCrossover<UMLRSolution> crossoverOperator, int eval) {
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
            new RExecuteAlgorithms<>(experiment).run();

            // Print experiment results to JSON file
            EasierLogger.logger_.info("Writing experiment results to JSON file");
            new FileUtils().experimentToJSON(EasierExperimentDAO.eINSTANCE);

            crossoverOperator.writeCrossoverReport(experiment.getExperimentBaseDirectory());

        return refFront;

    }

    public static List<ExperimentAlgorithm<UMLRSolution, List<UMLRSolution>>> configureAlgorithmList(
            List<ExperimentProblem<UMLRSolution>> problemList, UMLRCrossover<UMLRSolution> crossoverOperator, int eval) {

        List<ExperimentAlgorithm<UMLRSolution, List<UMLRSolution>>> algorithms = new ArrayList<>();
        FactoryBuilder<UMLRSolution> fBuilder = new FactoryBuilder<>();

        final SolutionListEvaluator<UMLRSolution> solutionListEvaluator = new RSolutionListEvaluator<>();

        final MutationOperator<UMLRSolution> mutationOperator = new UMLRMutation<>(Configurator.eINSTANCE.getMutationProbability(), Configurator.eINSTANCE.getDistributionIndex());

        String algo = Configurator.eINSTANCE.getAlgorithm();

        for (ExperimentProblem<UMLRSolution> expProblem : problemList) {
            algorithms.addAll(
                    fBuilder.configureAlgorithmList(expProblem, eval, crossoverOperator, solutionListEvaluator,
                            mutationOperator, algo));
        }

        return algorithms;

    }

    public static RProblem<UMLRSolution> createProblems(Path modelPath, int eval) {

//        double probPas = Configurator.eINSTANCE.getProbPas();

//        String brf = Configurator.eINSTANCE.getBrfList().toString().replace(":", "_").replace(",", "__")
//                .replace(" ", "").replace("[", "").replace("]", "");
//        String pName = String.format("%s__BRF_%s__MaxEval_%d__ProbPAs_%.2f__sb_%s_sbth_%s__Algo_%s",
//                modelPath.getName(modelPath.getNameCount() - 2), brf, eval, probPas,
//                Configurator.eINSTANCE.getSearchBudget(), Configurator.eINSTANCE.getSearchBudgetThreshold(),
//                Configurator.eINSTANCE.getAlgorithm());
        String pName = modelPath.getName(modelPath.getNameCount() - 2) + "__Algo_" + Configurator.eINSTANCE.getAlgorithm();

//        if ("rs".equals(Configurator.eINSTANCE.getAlgorithm()))
//            return new RandomSearchUMLRProblem<>(modelPath, pName);

        return new UMLRProblem<>(modelPath, pName);
    }


    public static List<String> getScenarios(Path lqxoFile) {

        LQN lqn = new LQN(lqxoFile.toString());
        Map<String, Scenario> entriesByScenario = lqn.getEntriesByScenario();

        return new ArrayList<>(entriesByScenario.keySet());
    }

}
