package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.ObjectiveEstimator;
import it.univaq.disim.sealab.metaheuristic.utils.*;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;

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
     * Sets objectives computed.
     * The list of the objectives is defined in the Configurator.
     * Configurator.eInstance.getObjectivesList()
     */
    @Override
    public void evaluate(S s) {

        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "evaluate_start");
//        UMLRSolution solution = (UMLRSolution) s;

        /*while (solution.getMapOfObjectives().isEmpty()) {

            // 1. Execute refactoring action
            solution.executeRefactoring();

            try {
                // 2. Generate the performance model
                IWorkflowUtils.applyTransformation(solution.getModelPath());

                // 3. Invoke the performance solver
                IWorkflowUtils.invokeSolver(solution.getFolderPath());

                // 4. Feed back the software model with performance indices
                IWorkflowUtils.backAnnotation(solution.getModelPath());

                // compute all the available objectives.
                // It impacts the execution time of the process. However, it enables a post-hoc analysis
                ObjectiveEstimator.computeObjectives(solution);

            } catch (EasierException | LQNException | URISyntaxException | EolRuntimeException e) {
                String line = solution.getName() + "," + e.getMessage() + "," + solution.getVariable(0).toString();
                new FileUtils().failedSolutionLogToCSV(line);

                // In case of any failures within the evaluation process, set all the objectives to the unfeasible value
                // should avoid selecting the solution for the next generation
                EasierLogger.logger_.severe(String.format("Solution id: # %d has been replaced because %s", solution.getName(), e.getMessage()));
                solution = (UMLRSolution) createSolution();
            }
        }

        EasierLogger.logger_.info(String.format("Solution id: # %d has been evaluated: %s", solution.getName(), solution.getMapOfObjectives()));
*/
        // set objectives for the fitness function
        new ObjectiveEstimator().setConsideredObjectives(s);

        // Add the solution to the population of the experiment for the export to JSON
        EasierExperimentDAO.eINSTANCE.addPopulation(s);

        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "evaluate_end");
    }

}
