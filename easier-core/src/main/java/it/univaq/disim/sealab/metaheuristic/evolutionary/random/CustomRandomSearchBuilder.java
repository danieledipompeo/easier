package it.univaq.disim.sealab.metaheuristic.evolutionary.random;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import org.uma.jmetal.algorithm.multiobjective.randomsearch.RandomSearchBuilder;
import org.uma.jmetal.problem.Problem;

public class CustomRandomSearchBuilder<S extends RSolution<?>>  extends RandomSearchBuilder<S> {

    private Problem<S> _problem;

    public CustomRandomSearchBuilder(Problem<S> problem) {
        super(problem);
        _problem = problem;
    }

    @Override
    public CustomRandomSearch<S> build() {
        return new CustomRandomSearch<S>(this._problem, this.getMaxEvaluations());
    }

}
