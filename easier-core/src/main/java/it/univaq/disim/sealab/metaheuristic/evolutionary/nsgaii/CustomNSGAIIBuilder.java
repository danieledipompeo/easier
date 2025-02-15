package it.univaq.disim.sealab.metaheuristic.evolutionary.nsgaii;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.problem.Problem;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;

public class CustomNSGAIIBuilder<S extends RSolution<?>> extends NSGAIIBuilder<S>{

	public CustomNSGAIIBuilder(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
			MutationOperator<S> mutationOperator, int popSize) {
		super(problem, crossoverOperator, mutationOperator, popSize);
	}

	public NSGAII<S> build() {
		NSGAII<S> algorithm = new CustomNSGAII<S>(this.getProblem(), this.getMaxIterations(), this.getPopulationSize(),
				this.getCrossoverOperator(), this.getMutationOperator(), this.getSelectionOperator(), this.getSolutionListEvaluator());
		
	    return algorithm ;
	  }


}
