package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import it.univaq.disim.sealab.metaheuristic.utils.EasierLogger;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RSolutionListEvaluator<S extends RSolution<?>> implements SolutionListEvaluator<S> {

    public List<S> evaluate(List<S> solutionList, Problem<S> problem) throws JMetalException {

        Stack<S> toEvaluate = new Stack<>();
        toEvaluate.addAll(solutionList);

        List<S> evaluatedPopulation = new ArrayList<>();

        while(!toEvaluate.isEmpty()){
            S s = toEvaluate.pop();
            try {
                s.executeFlow();
                s.computeObjectives();
                evaluatedPopulation.add(s);
            } catch (EasierException e) {
                String line = s.getName() + "," + e.getMessage() + "," + s.getVariable(0).toString();
                new FileUtils().failedSolutionLogToCSV(line);
//                EasierLogger.logger_.severe(String.format("Solution id: # %d has been replaced from the population because %s",
//                        s.getName(), e.getMessage()));
                S newSolution = problem.createSolution();
                EasierLogger.logger_.severe(String.format("Solution id: # %d has been replaced by solution id: %s because %s",
                        s.getName(), newSolution.getName(), e.getMessage()));
                toEvaluate.push(newSolution);
            }
        }

        evaluatedPopulation.forEach(problem::evaluate);

        return evaluatedPopulation;
    }

    @Override
    public void shutdown() {

    }

}
