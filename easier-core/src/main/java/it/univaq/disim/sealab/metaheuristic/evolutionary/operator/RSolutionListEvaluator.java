package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

public abstract class RSolutionListEvaluator<S extends RSolution<?>> implements SolutionListEvaluator<S> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected EasierResourcesLogger easierResourcesLogger;

}
