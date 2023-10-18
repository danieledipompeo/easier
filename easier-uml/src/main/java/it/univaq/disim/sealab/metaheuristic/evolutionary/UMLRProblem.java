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

        // 2. Generate the performance model
        try {
            WorkflowUtils.applyTransformation(solution.getModelPath());
        } catch (EasierException e) {
            JMetalLogger.logger.severe(e.getMessage());
        }

        // 3. Invoke the performance solver
        try {
            WorkflowUtils.invokeSolver(solution.getFolderPath());
        } catch (EasierException | LQNException e) {
            String line = solution.getName() + "," + e.getMessage() + "," + solution.getVariable(0).toString();
            new FileUtils().failedSolutionLogToCSV(line);
            EasierLogger.logger_.severe(e.getMessage());
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
        setObjectives(solution);

        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "evaluate_end");

        // Add the solution to the population of the experiment for the export to JSON
        EasierExperimentDAO.eINSTANCE.addPopulation(solution);

        EasierLogger.logger_.info(String.format("Objectives of Solution # %s have been set.", solution.getName()));

    }


    private void setObjectives(UMLRSolution solution) {

        /*if (Configurator.eINSTANCE.getProbPas() != 0)
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

        try {
            solution.setSystemEnergy(WorkflowUtils.energyEstimation(solution.getModelPath()));
        } catch (EasierException e) {
            EasierLogger.logger_.severe(String.format("Solution # '%s' has thrown an error when evaluating " +
                            "energy. The objective will be set to Double.MAX_VALUE.",
                    solution.getName()));
            solution.setSystemEnergy(Double.MAX_VALUE);
        }
        solution.computeReliability();
        solution.computeArchitecturalChanges();*/

        List<String> objectives = Configurator.eINSTANCE.getObjectivesList();

        for(String obj : objectives){
            int index = objectives.indexOf(obj);
            switch (obj){
                case Configurator.PERF_Q_LABEL:
                    // to be maximized
                    try {
                        solution.setObjective(index,
                                (-1 * WorkflowUtils.perfQ(sourceModelPath, solution.getModelPath())));
                    } catch (EasierException e) {
                        EasierLogger.logger_.severe("SolutionID : " + solution.getName() + " error in computing the " +
                                "perfQ: " + e.getMessage());
                        EasierLogger.logger_.info("PerfQ is set to Double.MAX_VALUE");
                        solution.setObjective(index, -1 * Double.MAX_VALUE);
                    }
                    break;
                case Configurator.SYS_RESP_T_LABEL:
                    // to be minimized
                    try {
                        solution.setObjective(index, WorkflowUtils.systemResponseTime(solution.getModelPath()));
                    } catch (EasierException e) {
                        EasierLogger.logger_.severe("SolutionID : " + solution.getName() + " error in computing the " +
                                "system response time: " + e.getMessage());
                        EasierLogger.logger_.info("System response time is set to Double.MAX_VALUE");
                        solution.setObjective(index, Double.MAX_VALUE);
                    }
                    break;
                case Configurator.CHANGES_LABEL:
                    // to be minimized
                    try {
                        solution.setObjective(index, WorkflowUtils.refactoringCost(solution));
                    } catch (EasierException e) {
                       EasierLogger.logger_.severe("SolutionID : " + solution.getName() + " error in computing the " +
                                "refactoring cost: " + e.getMessage());
                        EasierLogger.logger_.info("Refactoring cost is set to Double.MAX_VALUE");
                        solution.setObjective(index, Double.MAX_VALUE);
                    }
                    break;
                case Configurator.RELIABILITY_LABEL:
                    // to be maximized
                    try {
                        solution.setObjective(index, (-1 * WorkflowUtils.reliability(solution.getModelPath())));
                    } catch (MissingTagException e) {
                        EasierLogger.logger_.severe(
                                "SolutionID : " + solution.getName() + " error in computing the reliability: " +
                                        e.getMessage());
                        String line =
                                solution.getName() + "," + e.getMessage() + "," +
                                        solution.getVariable(RSolution.VARIABLE_INDEX).toString();
                        new FileUtils().reliabilityErrorLogToCSV(line);

                        EasierLogger.logger_.info("Reliability is set to Double.MAX_VALUE");
                        solution.setObjective(index, -1 * Double.MIN_VALUE);
                    }
                    break;
                case Configurator.ENERGY_LABEL:
                    // to be minimized
                    try {
                        solution.setObjective(index, WorkflowUtils.energyEstimation(solution.getModelPath()));
                    } catch (EasierException e) {
                        EasierLogger.logger_.severe("SolutionID : " + solution.getName() + " error in computing the " +
                                "energy: " + e.getMessage());
                        EasierLogger.logger_.info("Energy is set to Double.MAX_VALUE");
                        solution.setObjective(index, Double.MAX_VALUE);
                    }
                    break;
                case Configurator.PAS_LABEL:
                    // to be minimized
                    try {
                        solution.setObjective(index,
                                WorkflowUtils.countPerformanceAntipattern(solution.getModelPath(), solution.getName()));
                    } catch (EasierException e) {
                        EasierLogger.logger_.severe(
                                String.format("Solution: #%s has thrown an error when computing the pas on: %s " +
                                        "because of: %s", solution.getName(), sourceModelPath, e.getMessage()));
                        EasierLogger.logger_.info("PAs is set to Double.MAX_VALUE");
                        solution.setObjective(index, Double.MAX_VALUE);
                    }
                    break;
                default:
                    EasierLogger.logger_.severe(String.format("Objective '%s' not recognized.", obj));
                    break;
            }

        }

        // Set objectives
        /*solution.setObjective(0, (-1 * solution.getPerfQ())); // to be maximized
        solution.setObjective(1, solution.getArchitecturalChanges());
        if (Configurator.eINSTANCE.getProbPas() != 0) {
            solution.setObjective(2, solution.getPAs());
            solution.setObjective(3, (-1 * solution.getReliability())); // to be maximized
        } else {
            solution.setObjective(2, solution.getSystemEnergy()); // to be minimized
            solution.setObjective(3, (-1 * solution.getReliability())); // to be maximized
        }*/
    }
}
