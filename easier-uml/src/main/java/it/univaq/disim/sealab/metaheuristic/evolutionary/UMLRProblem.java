package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;

import java.nio.file.Path;

public class UMLRProblem<S extends RSolution<?>> extends RProblem<S> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public UMLRProblem(Path srcModelPath, String name) {
        super(srcModelPath);
        this.setName(name);
    }

    /**
     * @return a new UMLRSolution. It also creates the refactoring owned by the solution.
     */
    @Override
    public S createSolution() {
        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "createSolution_start");
        UMLRSolution sol = new UMLRSolution(sourceModelPath, getName());
        sol.createRandomRefactoring();
        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "createSolution_end");
        sol.refactoringToCSV();

        // Add the solution to the population of the experiment for the export to JSON
        //        EasierExperimentDAO.eINSTANCE.addPopulation(sol);
        return (S) sol;
    }

    /**
     * This method is intentionally empty. The evaluation of each solution is done by
     * it.univaq.disim.sealab.metaheuristic.evolutionary.operator.RSolutionListEvaluator
     *
     * @param s the solution to be evaluated
     */
    @Override
    public void evaluate(S s) {

//        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "evaluate_start");

        // set objectives for the fitness function
//        new ObjectiveEstimator().setConsideredObjectives(s);

        // Add the solution to the population of the experiment for the export to JSON
//        EasierExperimentDAO.eINSTANCE.addPopulation(s);

//        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "evaluate_end");
    }

}
