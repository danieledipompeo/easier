package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import it.univaq.disim.sealab.metaheuristic.utils.EasierLogger;
import it.univaq.disim.sealab.metaheuristic.utils.EasierObjectiveNotFoundException;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
import it.univaq.disim.sealab.metaheuristic.utils.WorkflowUtils;

public class RSolutionListEvaluator<S extends RSolution<?>> implements SolutionListEvaluator<S> {

    /**
     * This method evaluates a list of solutions.
     *
     * @param solutionList The list of solutions to be evaluated
     * @param problem      The problem to be solved
     * @return The list of solutions with their objectives and constraints values set
     * @throws JMetalException
     */
    @Override
    public List<S> evaluate(List<S> solutionList, Problem<S> problem) throws JMetalException {

        Stack<S> toEvaluate = new Stack<>();
        toEvaluate.addAll(solutionList);

        List<S> evaluatedPopulation = new ArrayList<>();

        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "evaluate_start");
        while(!toEvaluate.isEmpty()){
            S s = toEvaluate.pop();
            try {
                new WorkflowUtils().executeFlow(s);
                new ObjectiveEstimator().computeObjectives(s);
                new ObjectiveEstimator().setConsideredObjectives(s);
                evaluatedPopulation.add(s);
            } catch (EasierException | EasierObjectiveNotFoundException e) {
                String line = s.getName() + "," + e.getMessage() + "," + s.getVariable(0).toString();
                new FileUtils().failedSolutionLogToCSV(line);
                S newSolution = problem.createSolution();
                EasierLogger.logger_.severe(String.format("Solution id: # %d has been replaced by solution id: %s because %s",
                        s.getName(), newSolution.getName(), e.getMessage()));
                toEvaluate.push(newSolution);
            }
        }

        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "evaluate_end");
//        evaluatedPopulation.forEach(problem::evaluate);

        return evaluatedPopulation;
    }

    @Override
    public void shutdown() {

    }

}
