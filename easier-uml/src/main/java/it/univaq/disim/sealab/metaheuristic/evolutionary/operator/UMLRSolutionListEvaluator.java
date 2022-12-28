package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.domain.EasierPopulationDAO;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import org.uma.jmetal.problem.Problem;

import java.util.List;

public class UMLRSolutionListEvaluator <S extends RSolution<?>> extends RSolutionListEvaluator<S> {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UMLRSolutionListEvaluator(){
	}

	@Override
	public List<S> evaluate(List<S> solutionList, Problem<S> problem) {

		EasierResourcesLogger.checkpoint("UMLRSolutionListEvaluator","evaluate_start");

		solutionList.stream().forEach(sol -> {
			sol.executeRefactoring();
			sol.applyTransformation();
			sol.invokeSolver();
			if(Configurator.eINSTANCE.getProbPas() != 0)
				sol.countingPAs();
			sol.evaluatePerformance();
			sol.computeReliability();
			sol.computeArchitecturalChanges();
			problem.evaluate(sol);
		});

		EasierResourcesLogger.checkpoint("UMLRSolutionListEvaluator","evaluate_end");

		EasierExperimentDAO.eINSTANCE.addPopulation(new EasierPopulationDAO((List<RSolution<?>>) solutionList));

		return solutionList;
	}

	@Override
	public void shutdown() {

	}

}
