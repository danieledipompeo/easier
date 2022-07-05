package it.univaq.disim.sealab.metaheuristic.actions.uml;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;

public class RefactoringActionFactory {

    /**
     * Case 4 and 5 come from the performance tactics used in
     * <br/>
     * <b>Distributed quality-attribute optimization of software architectures.</b><br/>
     * Alejandro Rago, Santiago A. Vidal, J. Andres Diaz-Pace, Sebastian Frank, Andre' van Hoorn <br/>
     * SBCARS 2017, <url>https://doi.org/10.1145/3132498.3132509</url>
     *
     * @param availableElements
     * @param initialElements
     * @return
     */
    public static RefactoringAction getRandomAction(Map<String, Set<String>> availableElements, Map<String, Set<String>> initialElements) {

        int extractedAction = JMetalRandom.getInstance().nextInt(0, 5);
        switch (extractedAction) {
            case 0:
                return new UMLCloneNode(availableElements, initialElements);
            case 1:
                return new UMLMvComponentToNN(availableElements, initialElements);
            case 2:
                return new UMLMvOperationToNCToNN(availableElements, initialElements);
            case 3:
                return new UMLMvOperationToComp(availableElements, initialElements);
            case 4:
                return new UMLChangePassiveResource(availableElements, initialElements);
            case 5:
                return new UMLResourceScaling(availableElements, initialElements);
            default:
                return null;
        }

/*		try {
			return (RefactoringAction) supportedRefactoringActions[JMetalRandom.getInstance().nextInt(0,
					supportedRefactoringActions.length - 1)].getDeclaredConstructor(UMLRSolution.class).newInstance(sol);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			System.err.println("Error in getRandomRefactoringAction.");
			e.printStackTrace();
		}
		return null;*/
    }

}
