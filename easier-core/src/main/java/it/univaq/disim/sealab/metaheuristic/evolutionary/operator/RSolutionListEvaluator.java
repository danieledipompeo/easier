package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;

public abstract class RSolutionListEvaluator<S extends RSolution<?>> implements SolutionListEvaluator<S> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected EasierResourcesLogger easierResourcesLogger;

}
