package it.univaq.disim.sealab.metaheuristic.evolutionary.random;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import org.uma.jmetal.algorithm.multiobjective.randomsearch.RandomSearch;
import org.uma.jmetal.algorithm.multiobjective.randomsearch.RandomSearchBuilder;
import org.uma.jmetal.problem.Problem;

public class CustomRandomSearch<S extends RSolution<?>> extends RandomSearch<S> {
    /**
     * Constructor
     *
     * @param problem
     * @param maxEvaluations
     */
    public CustomRandomSearch(Problem<S> problem, int maxEvaluations) {
        super(problem, maxEvaluations);
    }

    @Override
    public void run(){
        EasierResourcesLogger.checkpoint(getName(), "run_start");
        super.run();
        EasierResourcesLogger.checkpoint(getName(), "run_end");
    }
}


