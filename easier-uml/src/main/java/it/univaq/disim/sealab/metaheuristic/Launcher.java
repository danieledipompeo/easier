package it.univaq.disim.sealab.metaheuristic;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.lab.experiment.ExperimentBuilder;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import com.beust.jcommander.JCommander;

import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.RExecuteAlgorithms;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.RExperiment;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.RExperimentBuilder;
import it.univaq.disim.sealab.metaheuristic.evolutionary.factory.FactoryBuilder;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.RSolutionListEvaluator;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRCrossover;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRMutation;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierLogger;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
import it.univaq.disim.sealab.metaheuristic.utils.UMLMemoryOptimizer;
import it.univaq.disim.sealab.metaheuristic.utils.WorkflowUtils;
import it.univaq.easier.LQN;
import it.univaq.easier.Scenario;

public class Launcher {

    public static void main(String[] args) throws Exception {

        JCommander jc = new JCommander();

        jc.addObject(Configurator.eINSTANCE);
        jc.parse(args);

        UMLRCrossover<UMLRSolution> crossoverOperator = new UMLRCrossover<>(Configurator.eINSTANCE.getXoverProbabiliy());

        Path modelPath = Configurator.eINSTANCE.getModelPath();

        EasierLogger.logger_.info(String.format("Processing model : %s", modelPath.getFileName().toString()));
        RProblem<UMLRSolution> rProblem = createProblem(modelPath, Configurator.eINSTANCE.getMaxEvaluation());

        if (!modelPath.getParent().resolve("output.xml").toFile().exists()) {
            WorkflowUtils.applyTransformation(modelPath);
            WorkflowUtils.invokeSolver(modelPath.getParent());
        }

        List<String> scenarios = getScenarios(modelPath.getParent().resolve("output.lqxo"));

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
        runExperiment(rProblem, qIndicators, crossoverOperator, Configurator.eINSTANCE.getMaxEvaluation());
        new UMLMemoryOptimizer().cleanup();
        System.gc();
        EasierResourcesLogger.dumpToJSON();
    }

    public static void runExperiment(final RProblem<UMLRSolution> rProblem,
                                           final List<GenericIndicator<UMLRSolution>> qualityIndicators,
                                           UMLRCrossover<UMLRSolution> crossoverOperator, int eval) {
        final int INDEPENDENT_RUNS = Configurator.eINSTANCE.getIndependentRuns(); // should be 31 or 51
        final int CORES = 1;

        List<ExperimentProblem<UMLRSolution>> problemList = new ArrayList<>();

        ExperimentAlgorithm<UMLRSolution, List<UMLRSolution>> experimentAlgorithm =
                configureAlgorithm(new ExperimentProblem<>(rProblem), crossoverOperator,
                        eval);

        Path referenceFrontDirectory = Paths.get(Configurator.eINSTANCE.getOutputFolder().toString(), "referenceFront");

        List<String> tags = new ArrayList<>();

        if (Configurator.eINSTANCE.generateRF())
            problemList.forEach(p -> tags.add(p.getTag() + ".rf"));
        else
            problemList.forEach(p -> tags.add("super-reference-pareto.rf"));

        ExperimentBuilder<UMLRSolution, List<UMLRSolution>> experimentBuilder =
                new RExperimentBuilder<UMLRSolution, List<UMLRSolution>>("Exp")
                        .setAlgorithmList(List.of(experimentAlgorithm))
                        .setProblemList(problemList)
                        .setExperimentBaseDirectory(referenceFrontDirectory.toString())
                        .setReferenceFrontDirectory(referenceFrontDirectory.toString())
                        .setIndependentRuns(INDEPENDENT_RUNS).setNumberOfCores(CORES)
                        .setOutputParetoFrontFileName("FUN")
                        .setOutputParetoSetFileName("VAR")
                        .setIndicatorList(qualityIndicators);

        RExperiment<UMLRSolution, List<UMLRSolution>> experiment =
                ((RExperimentBuilder<UMLRSolution, List<UMLRSolution>>) experimentBuilder)
                        .setReferenceFrontFileNames(tags).build();
            new RExecuteAlgorithms<>(experiment).run();

            // Print experiment results to JSON file
            EasierLogger.logger_.info("Writing experiment results to JSON file");
            new FileUtils().experimentToJSON(EasierExperimentDAO.eINSTANCE);

            crossoverOperator.writeCrossoverReport(experiment.getExperimentBaseDirectory());
    }

    public static ExperimentAlgorithm<UMLRSolution, List<UMLRSolution>> configureAlgorithm(
            ExperimentProblem<UMLRSolution> p, UMLRCrossover<UMLRSolution> crossoverOperator, int eval) {

        FactoryBuilder<UMLRSolution> fBuilder = new FactoryBuilder<>();

        final SolutionListEvaluator<UMLRSolution> solutionListEvaluator = new RSolutionListEvaluator<>();

        final MutationOperator<UMLRSolution> mutationOperator = new UMLRMutation<>(Configurator.eINSTANCE.getMutationProbability(), Configurator.eINSTANCE.getDistributionIndex());

        String algo = Configurator.eINSTANCE.getAlgorithm();

        return fBuilder.configureAlgorithm(p, eval, crossoverOperator, solutionListEvaluator,
                            mutationOperator, algo);
    }

    public static RProblem<UMLRSolution> createProblem(Path modelPath, int eval) {
        String pName = modelPath.getName(modelPath.getNameCount() - 2) + "__Algo_" + Configurator.eINSTANCE.getAlgorithm();
        return new UMLRProblem<>(modelPath, pName);
    }


    public static List<String> getScenarios(Path lqxoFile) {

        LQN lqn = new LQN(lqxoFile.toString());
        Map<String, Scenario> entriesByScenario = lqn.getEntriesByScenario();

        return new ArrayList<>(entriesByScenario.keySet());
    }

}
