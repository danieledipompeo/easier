package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.utils.*;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.uma.jmetal.util.JMetalLogger;

import java.io.ObjectInputFilter;
import java.net.URISyntaxException;
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
     * Sets objectives computed by UMLRSolutionListEvaluator.
     * obj_1 = perfQ
     * obj_2 = architectural changes
     * obj_3 = performance antipatterns
     * obj_4 = reliability
     */
    @Override
    public void evaluate(S s) {

        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "evaluate_start");
        UMLRSolution solution = (UMLRSolution) s;

        // 1. Execute refactoring action
        solution.executeRefactoring();

        // 2. Generate the performance model
        try {
            WorkflowUtils.applyTransformation(solution.getModelPath());
        } catch (EasierException e) {
            JMetalLogger.logger.severe(e.getMessage());
        }

        // 3. Invoke the performance solver
        try {
            WorkflowUtils.invokeSolver(solution.getFolderPath());
        } catch (EasierException e) {
            String line = solution.getName() + "," + e.getMessage() + "," + solution.getVariable(0).toString();
            new FileUtils().failedSolutionLogToCSV(line);
            JMetalLogger.logger.severe(e.getMessage());
        }

        // 4. Feed back the software model with performance indices
        try {
            WorkflowUtils.backAnnotation(solution.getModelPath());
        } catch (URISyntaxException | EolRuntimeException e) {
            String line = solution.getName() + "," + e.getMessage() + "," + solution.getVariable(0).toString() + "," +
                    solution.isMutated() + "," + solution.isCrossover();
            new FileUtils().failedSolutionLogToCSV(line);
            JMetalLogger.logger.severe(e.getMessage());
        }

        // Compute objectives
        if (Configurator.eINSTANCE.getProbPas() != 0)
            WorkflowUtils.countPerformanceAntipattern(solution.getModelPath(), solution.getName());

        try {
            // it will use the system response time as objective if the isPerfQ returns false
            if(Configurator.eINSTANCE.isPerfQ())
                solution.setPerfQ(WorkflowUtils.perfQ(sourceModelPath, solution.getModelPath()));
            else
                solution.setPerfQ(WorkflowUtils.systemResponseTime(solution.getModelPath()));
        } catch (EasierException e) {
            EasierLogger.logger_.severe(String.format("Solution # '%s' has thrown an error when evaluating " +
                            "performance. The objective will be set to Double.MIN_VALUE.",
                    solution.getName()));
            // TODO check whether using the min value is the best choice
            solution.setPerfQ(Double.MIN_VALUE);
        }
        solution.computeReliability();
        solution.computeArchitecturalChanges();

        // Set objectives
        solution.setObjective(0, (-1 * solution.getPerfQ())); // to be maximized
        solution.setObjective(1, solution.getArchitecturalChanges());
        if (Configurator.eINSTANCE.getProbPas() != 0) {
            solution.setObjective(2, solution.getPAs());
            solution.setObjective(3, (-1 * solution.getReliability())); // to be maximized
        } else {
            solution.setObjective(2, (-1 * solution.getReliability())); // to be maximized
        }

        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "evaluate_end");

        // Add the solution to the population of the experiment for the export to JSON
        EasierExperimentDAO.eINSTANCE.addPopulation(solution);

        EasierLogger.logger_.info(String.format("Objectives of Solution # %s have been set.", solution.getName()));

    }
}
