package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.Map;
import java.util.Set;

public class RefactoringActionFactory {

    // TODO change param. Instead of using UMRSolution, use availableElements, and initialElements directly
    public static RefactoringAction getRandomAction(Map<String, Set<String>> availableElements, Map<String, Set<String>> initialElements) {

        int extractedAction = JMetalRandom.getInstance().nextInt(0, 3);
        switch (extractedAction) {
            case 0:
                return new UMLCloneNode(availableElements, initialElements);
            case 1:
                return new UMLMvComponentToNN(availableElements, initialElements);
            case 2:
                return new UMLMvOperationToNCToNN(availableElements, initialElements);
            case 3:
                return new UMLMvOperationToComp(availableElements, initialElements);
            default:
                return null;
        }
    }
}
