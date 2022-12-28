package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierLogger;
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
        EasierResourcesLogger.checkpoint("UMLRProblem","createSolution_start");
        UMLRSolution sol = new UMLRSolution(sourceModelPath, getName());
        sol.createRandomRefactoring();
        EasierResourcesLogger.checkpoint("UMLRProblem","createSolution_end");
        sol.refactoringToCSV();
        return (S) sol;
    }

    /**
     * Sets objectives computed by UMLRSolutionListEvaluator.
     * obj_1 = perfQ
     * obj_2 = architectural changes
     * obj_3 = performance antipatterns
     * obj_4 = reliability
     */
    @Override
    public void evaluate(S s) {

        EasierResourcesLogger.checkpoint("UMLRProblem","evaluate_start");
        UMLRSolution solution = (UMLRSolution) s;

        solution.setObjective(0, (-1 * solution.getPerfQ())); // to be maximized
        solution.setObjective(1, solution.getArchitecturalChanges());
        if (Configurator.eINSTANCE.getProbPas() != 0) {
            solution.setObjective(2, solution.getPAs());
            solution.setObjective(3, (-1 * solution.getReliability())); // to be maximized
        } else {
            solution.setObjective(2, (-1 * solution.getReliability())); // to be maximized
        }
        EasierResourcesLogger.checkpoint("UMLRProblem","evaluate_end");

        EasierLogger.logger_.info(String.format("Objectives of Solution # %s have been set.", solution.getName()));

    }
}
