package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;

import java.nio.file.Path;

public class RandomSearchUMLRProblem<S extends UMLRSolution> extends UMLRProblem<S> {

    public RandomSearchUMLRProblem(Path srcModelPath, String name) {
        super(srcModelPath, name);
    }

    @Override
    public void evaluate(S solution) {

        // Calculate objectives

        EasierResourcesLogger.checkpoint("RandomSearchUMLRProblem","evaluate_start");
        EasierResourcesLogger.checkpoint("RandomSearchUMLRSolutionListEvaluator","evaluate_start");
        UMLRSolution sol = solution;
        sol.executeRefactoring();
        sol.applyTransformation();
        sol.invokeSolver();
        if (Configurator.eINSTANCE.getProbPas() != 0)
            sol.countingPAs();
        sol.evaluatePerformance();
        sol.computeReliability();
        sol.computeArchitecturalChanges();
        EasierResourcesLogger.checkpoint("RandomSearchUMLRSolutionListEvaluator","evaluate_end");

        super.evaluate(solution);
        EasierResourcesLogger.checkpoint("RandomSearchUMLRProblem","evaluate_end");

    }
}
