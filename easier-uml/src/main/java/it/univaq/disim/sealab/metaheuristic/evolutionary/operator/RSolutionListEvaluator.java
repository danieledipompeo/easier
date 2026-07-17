package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import it.univaq.disim.sealab.metaheuristic.utils.*;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;

public class RSolutionListEvaluator<S extends RSolution<?>> implements SolutionListEvaluator<S> {

    static int ITERATION_COUNTER = 0;
    /**
     * This method evaluates a list of solutions.
     *
     * @param solutionList The list of solutions to be evaluated
     * @param problem      The problem to be solved
     * @return The list of solutions with their objective values
     * @throws JMetalException if any error occurs
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
                if(!Configurator.eINSTANCE.isSurrogate() || !s.isMarkedForSurrogate()) {
                    EasierLogger.logger_.info(String.format("Solution id: # %s has been evaluated by EASIER.", s.getName()));
                    ObjectiveEstimator.computeObjectives(s);
                    ObjectiveEstimator.setConsideredObjectives(s);
                }
                evaluatedPopulation.add(s);
            }
            catch(EasierException | EasierObjectiveNotFoundException e){
                String line = s.getName() + "," + e.getMessage() + "," + s.getVariable(0).toString();
                new FileUtils().failedSolutionLogToCSV(line);
                S newSolution = problem.createSolution();
                EasierLogger.logger_.severe(String.format("Solution id: # %d has been replaced by solution id: %s because %s",
                        s.getName(), newSolution.getName(), e.getMessage()));
                toEvaluate.push(newSolution);
            }
        }

        if(Configurator.eINSTANCE.isSurrogate()) {
            EasierLogger.logger_.info("Invoking surrogate evaluation for iteration: " + ITERATION_COUNTER);
            String caseStudyName = Configurator.eINSTANCE
            .getInitialModelPath().getFileName()
            .toString()
            .replace(".uml", "")
            .replace("-","");
            ObjectiveEstimator.surrogateEvaluation((List<RSolution<?>>) evaluatedPopulation, 
                                                    ITERATION_COUNTER,
                                                    caseStudyName,
                                                    Configurator.eINSTANCE.getSurrogateRetrainInterval());
        }

        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "evaluate_end");
        ITERATION_COUNTER++;
        return evaluatedPopulation;
    }

    @Override
    public void shutdown() {

    }

}
