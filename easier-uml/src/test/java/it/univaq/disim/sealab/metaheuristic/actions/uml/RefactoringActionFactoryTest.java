package it.univaq.disim.sealab.metaheuristic.actions.uml;

import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;


import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RefactoringActionFactoryTest {

	UMLRSolution sol;
	private EasierModel eModel;

	@BeforeEach
	public void init() {
		int allowedFailures = 100;
		int desired_length = 4;
		int populationSize = 4;
		
		String modelpath = getClass().getResource("/models/model/automatedGuidedVehicle.uml").getFile();
		UMLRProblem<RSolution<?>> p = new UMLRProblem<>(Paths.get(modelpath),"agv__test");
		sol = (UMLRSolution) p.createSolution();
		eModel = sol.getVariable(0).getEasierModel();
	}
	
	@Test
	public void getRandomActionTest() {
		RefactoringAction action = RefactoringActionFactory.getRandomAction(eModel.getAvailableElements(), eModel.getInitialElements());
		assertNotNull(action);
		assertFalse(action.getTargetElements().values().stream().flatMap(Set::stream).collect(Collectors.toSet()).isEmpty());
	}
	
}
