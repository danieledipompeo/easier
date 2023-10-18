package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.actions.uml.RefactoringActionFactory;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UMLRMutation<S extends UMLRSolution> extends RMutation<S> {

    /**
     * Constructor
     *
     * @param mutationProbability
     * @param distributionIndex
     */
    public UMLRMutation(double mutationProbability, double distributionIndex) {
        super(mutationProbability, distributionIndex);
//        easierResourcesLogger = new EasierResourcesLogger("UMLMutationOperator");
    }

    /**
     * Perform the mutation operation
     */
    @Override
    protected void doMutation(double probability, UMLRSolution solution, int allowed_failures) throws EasierException {

        EasierResourcesLogger.checkpoint("UMLMutationOperator","doMutation_start");

        for (int i = 0; i < solution.getNumberOfVariables(); i++) {

            // guard condition
            if (JMetalRandom.getInstance().nextDouble() <= probability) {

                final Map<String, Set<String>> initialElements = solution.getVariable(0).getEasierModel().getInitialElements();

                for (int j = 0; j < allowed_failures; j++) {
                    Refactoring ref = solution.getVariable(i);

                    int randomPosition = JMetalRandom.getInstance().nextInt(0, ref.getActions().size() - 1);

                    // the refactoring action that will be changed
                    RefactoringAction candidateToBeMutated = ref.getActions().get(randomPosition);

                    Map<String, Set<String>> filteredAvailableElements = filterOutElementOf(solution, candidateToBeMutated);

                    // filter out elements belong to the candidateToBeRemoved from the availableElements
//                    for (String k : availableElements.keySet()) {
//                        Set<String> kFilteredElements = availableElements.get(k);
//                        if (candidateToBeMutated.getCreatedElements().get(k) != null)
//                            kFilteredElements = availableElements.get(k).stream().filter(aElem -> !candidateToBeMutated.getCreatedElements().get(k).contains(aElem)).collect(Collectors.toSet());
//                        filteredAvailableElements.put(k, kFilteredElements);
//                    }

                    RefactoringAction newCandidate =
                            RefactoringActionFactory.getRandomAction(filteredAvailableElements, initialElements, solution.getVariable(0).getEasierModel().getAllContents());
                    ref.getActions().set(randomPosition, newCandidate);

                    if (solution.isFeasible()) {
                        solution.setMutated(true);
                        break;
                    }

                    filteredAvailableElements = filterOutElementOf(solution, newCandidate);

                }
            }
        }
        EasierResourcesLogger.checkpoint("UMLMutationOperator","doMutation_end");
    }

    private Map<String, Set<String>> filterOutElementOf(UMLRSolution solution, RefactoringAction refactoringAction) {
        Map<String, Set<String>> availableElements = solution.getVariable(0).getEasierModel().getAvailableElements();

        Map<String, Set<String>> filteredAvailableElements = new HashMap<>();
        // filter out elements belong to the candidateToBeRemoved from the availableElements
        for (String k : availableElements.keySet()) {
            Set<String> kFilteredElements = availableElements.get(k);
            if (refactoringAction.getCreatedElements().get(k) != null)
                kFilteredElements = availableElements.get(k).stream().filter(aElem -> !refactoringAction.getCreatedElements().get(k).contains(aElem)).collect(Collectors.toSet());
            filteredAvailableElements.put(k, kFilteredElements);
        }
        return filteredAvailableElements;
    }
}
