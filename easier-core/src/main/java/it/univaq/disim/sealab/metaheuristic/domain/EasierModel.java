package it.univaq.disim.sealab.metaheuristic.domain;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class EasierModel implements Cloneable {

    Map<String, Set<String>> initialElements;
    Map<String, Set<String>> targetRefactoringElement;
    Map<String, Set<String>> createdRefactoringElement;

    String modelPath;

    public EasierModel(final String mPath) {
        modelPath = mPath;
        initMap();
    }

    protected abstract void initMap();

    public Map<String, Set<String>> getTargetRefactoringElement() {
        return targetRefactoringElement;
    }

    public Map<String, Set<String>> getCreatedRefactoringElement() {
        return createdRefactoringElement;
    }

    /**
     * @return the immutable map of available elements that next refactoring action
     * could use as target element.
     */
    public Map<String, Set<String>> getAvailableElements() {
        return Map.copyOf(targetRefactoringElement);
    }

    /**
     * @return the immutable map of initial elements
     */
    public Map<String, Set<String>> getInitialElements() {
        return Map.copyOf(initialElements);
    }

    /**
     * Return true if the actionTargetElements are contained in the targetRefactoringElement, false otherwise.
     *
     * @param actionTargetElements
     * @return
     */
    public boolean contains(Map<String, Set<String>> actionTargetElements) {
        return actionTargetElements.values().stream().flatMap(Set::stream).map(String.class::cast).
                collect(Collectors.toSet()).stream().allMatch(targetRefactoringElement.values().stream().
                        flatMap(Set::stream).collect(Collectors.toSet())::contains);
    }

//    public boolean contains(Set<String> actionTargetElements) {
//        return actionTargetElements.stream().allMatch(targetRefactoringElement.values().stream().flatMap(Set::stream).collect(Collectors.toSet())::contains);
//    }

    /**
     * @param elemToBeStored
     * is a map of elements that have been created by a refactoring action.
     * The created elements are added within the createdRefactoringElement and can be used by
     * subsequent refactoring actions.
     */
    public void addElements(Map<String, Set<String>> elemToBeStored) {
        elemToBeStored.keySet().stream().forEach(k -> {
                targetRefactoringElement.get(k).addAll(elemToBeStored.get(k));
                createdRefactoringElement.get(k).addAll(elemToBeStored.get(k));
        });
    }

    /**
     * @param elemToBeRemoved
     * is a map of elements that have been deleted by a refactoring action.
     * The elements are removed from the createdRefactoringElement and cannot be used by
     * subsequent refactoring actions.
     */
    public void removeElements(Map<String, Set<String>> elemToBeRemoved){
        elemToBeRemoved.keySet().stream().forEach(k -> {
            targetRefactoringElement.get(k).removeAll(elemToBeRemoved.get(k));
            createdRefactoringElement.get(k).removeAll(elemToBeRemoved.get(k));
        });
    }

    @Override
    public EasierModel clone() {
        try {
            return (EasierModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EasierModel that = (EasierModel) o;
        return Objects.equals(initialElements, that.initialElements) &&
                Objects.equals(targetRefactoringElement, that.targetRefactoringElement) &&
                Objects.equals(createdRefactoringElement, that.createdRefactoringElement);
//                && Objects.equals(modelPath, that.modelPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialElements, targetRefactoringElement, createdRefactoringElement, modelPath);
    }
}
