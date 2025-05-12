package it.univaq.disim.sealab.metaheuristic.evolutionary.nsgaiii;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIII;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.problem.Problem;

public class CustomNSGAIIIBuilder<S extends RSolution<?>> extends NSGAIIIBuilder<S> {

    public CustomNSGAIIIBuilder(Problem<S> problem, CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator, int populationSize) {
        super(problem);
        this.setCrossoverOperator(crossoverOperator);
        this.setMutationOperator(mutationOperator);
        this.setPopulationSize(populationSize);
    }

    public NSGAIII<S> build() {
        return new CustomNSGAIII<>(this);
    }
}
