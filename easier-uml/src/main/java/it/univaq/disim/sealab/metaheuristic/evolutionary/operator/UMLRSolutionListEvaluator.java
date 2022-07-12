package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import org.uma.jmetal.problem.Problem;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;

public class UMLRSolutionListEvaluator <S extends RSolution<?>> extends RSolutionListEvaluator<S> {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UMLRSolutionListEvaluator(){
//		easierResourcesLogger = new EasierResourcesLogger("UMLRSolutionListEvaluator");
	}

	@Override
	public List<S> evaluate(List<S> solutionList, Problem<S> problem) {

		easierResourcesLogger.checkpoint("UMLRSolutionListEvaluator","evaluate_start");

		solutionList.stream().forEach(sol -> {
			sol.executeRefactoring();
			sol.applyTransformation();
			sol.invokeSolver();
			if(Configurator.eINSTANCE.getProbPas() != 0)
				sol.countingPAs();
			sol.evaluatePerformance();
			sol.computeReliability();
			sol.computeArchitecturalChanges();
//			sol.computeScenarioRT();
			problem.evaluate(sol);

			// Dump to file resources usage stats
//			sol.flushResourcesUsageStats();
		});

		easierResourcesLogger.checkpoint("UMLRSolutionListEvaluator","evaluate_end");

		return solutionList;
	}

	@Override
	public void shutdown() {

	}

}
