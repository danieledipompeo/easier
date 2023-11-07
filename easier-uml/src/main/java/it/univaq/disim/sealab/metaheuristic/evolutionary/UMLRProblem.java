package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.utils.*;
import it.univaq.sealab.umlreliability.MissingTagException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.uma.jmetal.util.JMetalLogger;

import java.io.ObjectInputFilter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

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
        UMLRSolution solution = (UMLRSolution) s;

        // 1. Execute refactoring action
        solution.executeRefactoring();

        try {
            // 2. Generate the performance model
            WorkflowUtils.applyTransformation(solution.getModelPath());

            // 3. Invoke the performance solver
            WorkflowUtils.invokeSolver(solution.getFolderPath());

            // 4. Feed back the software model with performance indices
            WorkflowUtils.backAnnotation(solution.getModelPath());

            // compute all the available objectives.
            // It impacts the execution time of the process. However, it enables a post-hoc analysis
            solution.computeObjectives();

            // Add the solution to the population of the experiment for the export to JSON
            EasierExperimentDAO.eINSTANCE.addPopulation(solution);

        } catch (EasierException | LQNException | URISyntaxException | EolRuntimeException e) {
            String line = solution.getName() + "," + e.getMessage() + "," + solution.getVariable(0).toString();
            new FileUtils().failedSolutionLogToCSV(line);
            EasierLogger.logger_.severe("All the objectives have been set to the relative unfeasible value, due to: " + e.getMessage());

            // In case of any failures within the evaluation process, set all the objectives to the unfeasible value
            // should avoid selecting the solution for the next generation
            solution.computeObjectivesToUnfeasibleValues();
        }

        // set objectives for the fitness function
        setObjectives(solution);

        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "evaluate_end");
    }

    private void setObjectives(UMLRSolution solution) {

        List<String> objectives = Configurator.eINSTANCE.getObjectivesList();

        for (String obj : objectives) {
            int index = objectives.indexOf(obj);
            switch (obj) {
                case Configurator.PERF_Q_LABEL:
                    solution.setObjective(index, solution.getMapOfObjectives().get(Configurator.PERF_Q_LABEL));
                    break;
                case Configurator.SYS_RESP_T_LABEL:
                    solution.setObjective(index, solution.mapOfObjectives.get(Configurator.SYS_RESP_T_LABEL));
                    break;
                case Configurator.CHANGES_LABEL:
                    solution.setObjective(index, solution.mapOfObjectives.get(Configurator.CHANGES_LABEL));
                    break;
                case Configurator.RELIABILITY_LABEL:
                    solution.setObjective(index, solution.mapOfObjectives.get(Configurator.RELIABILITY_LABEL));
                    break;
                case Configurator.ENERGY_LABEL:
                    solution.setObjective(index, solution.mapOfObjectives.get(Configurator.ENERGY_LABEL));
                    break;
                case Configurator.PAS_LABEL:
                    solution.setObjective(index, solution.mapOfObjectives.get(Configurator.PAS_LABEL));
                    break;
                case Configurator.POWER_LABEL:
                    solution.setObjective(index, solution.mapOfObjectives.get(Configurator.POWER_LABEL));
                    break;
                case Configurator.ECONOMIC_COST:
                    solution.setObjective(index, solution.mapOfObjectives.get(Configurator.ECONOMIC_COST));
                    break;
                default:
                    EasierLogger.logger_.severe(String.format("Objective '%s' not recognized.", obj));
                    break;
            }
        }
        EasierLogger.logger_.info(String.format("Objectives of Solution # %s have been set.", solution.getName()));
    }
}
