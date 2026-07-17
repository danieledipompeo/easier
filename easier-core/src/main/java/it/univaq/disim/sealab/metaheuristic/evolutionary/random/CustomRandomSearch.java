package it.univaq.disim.sealab.metaheuristic.evolutionary.random;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.support.CheckpointPhases;
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
    // TODO: we have moved the evaluation of each solution to the RSolutionListEvaluator class.
    //  Reimplement this method because it uses the Problem::evaluate method
    public void run(){
        CheckpointPhases.phase(getName(), "run", (Runnable) super::run);
    }
}


