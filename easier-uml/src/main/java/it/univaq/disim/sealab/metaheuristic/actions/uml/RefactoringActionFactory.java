package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import it.univaq.disim.sealab.metaheuristic.utils.EasierLogger;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class RefactoringActionFactory {

    /**
     * To handle the case of the random action throws an exception
     * The action type is kept until either a feasible action is extracted or the number of failures is less than
     * <p>
     * Case 4 and 5 come from the performance tactics used in
     * the allowed failures                                                                                      <br/>
     * <b>Distributed quality-attribute optimization of software architectures.</b><br/>
     * Alejandro Rago, Santiago A. Vidal, J. Andres Diaz-Pace, Sebastian Frank, Andre' van Hoorn <br/>
     * SBCARS 2017, <url>https://doi.org/10.1145/3132498.3132509</url>
     *
     * @param availableElements
     * @param initialElements
     * @return
     */
    public static RefactoringAction getRandomAction(Map<String, Set<String>> availableElements,
                                                    Map<String, Set<String>> initialElements) throws EasierException {

        List<String> listOfActions = Configurator.eINSTANCE.listOfActions();
        int extractedActionIndex = JMetalRandom.getInstance().nextInt(0, listOfActions.size() - 1);
        String extractedAction = listOfActions.get(extractedActionIndex);

        for (int failures = 0; ; failures++) {
            try {
                switch (extractedAction) {
                    case "clone":
                        return new UMLCloneNode(availableElements, initialElements);
                    case "mcnn":
                        return new UMLMvComponentToNN(availableElements, initialElements);
                    case "moncnn":
                        return new UMLMvOperationToNCToNN(availableElements, initialElements);
                    case "moc":
                        return new UMLMvOperationToComp(availableElements, initialElements);
                    case "change_passive_resource":
                        return new UMLChangePassiveResource(availableElements, initialElements);
                    case "resource_scaling":
                        return new UMLResourceScaling(availableElements, initialElements);
                    case "remove_node":
                        return new UMLRemoveNode(availableElements, initialElements);
                    case "remove_component":
                        return new UMLRemoveComponent(availableElements, initialElements);
                    default:
                        return null;
                }
            } catch (EasierException e) {
                if (failures > Configurator.eINSTANCE.getAllowedFailures()) {
                    EasierLogger.logger_.log(Level.SEVERE, e.getMessage(), e);
                    throw new EasierException(e);
                }
            }
        }
    }
}
