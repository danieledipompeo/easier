package it.univaq.disim.sealab.metaheuristic.evolutionary.ibea;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.List;

public class CustomIBEABuilder<S extends RSolution<?>> {

    private final int populationSize;
    private final int archiveSize;
    private int maxEvaluations;
    private final CrossoverOperator<S> crossover;
    private final MutationOperator<S> mutation;
    private final SelectionOperator<List<S>, S> selection;
    private SolutionListEvaluator<S> evaluator;

    private Problem<S> problem;

    public CustomIBEABuilder(Problem<S> p, CrossoverOperator<S> crossoverOperator,
			MutationOperator<S> mutationOperator, int popSize) {
        problem = p;
        this.populationSize = popSize;
        this.archiveSize = popSize;
        this.maxEvaluations = 25000;
        this.crossover = crossoverOperator;
        this.mutation = mutationOperator;
        this.selection = new BinaryTournamentSelection<>();
    }

    public CustomIBEABuilder<S> setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;
        return this;
    }

    public CustomIBEABuilder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
        this.evaluator = evaluator;
        return this;
    }

    public CustomIBEA<S> build() {
        return new CustomIBEA<>(this.problem, this.populationSize, this.archiveSize, this.maxEvaluations,
                this.selection, this.crossover, this.mutation, this.evaluator);
    }
}
