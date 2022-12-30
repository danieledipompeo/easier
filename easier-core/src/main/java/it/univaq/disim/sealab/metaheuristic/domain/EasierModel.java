package it.univaq.disim.sealab.metaheuristic.domain;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class EasierModel {

    Map<String, Set<String>> initialElements;
    Map<String, Set<String>> targetRefactoringElement;
    Map<String, Set<String>> createdRefactoringElement;

    String modelPath;

    protected EasierModel(final String mPath) {
        modelPath = mPath;
        initMap();
    }

    protected EasierModel(EasierModel eModel){
        this.modelPath = eModel.modelPath;
        this.targetRefactoringElement = eModel.targetRefactoringElement;
        this.createdRefactoringElement = eModel.createdRefactoringElement;
        this.initialElements = eModel.initialElements;
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

    public boolean contains(Map<String, Set<String>> actionTargetElements) {
        return actionTargetElements.values().stream().flatMap(Set::stream).map(String.class::cast).
                collect(Collectors.toSet()).stream().allMatch(targetRefactoringElement.values().stream().
                        flatMap(Set::stream).collect(Collectors.toSet())::contains);
    }

    public void store(Map<String, Set<String>> elemToBeStored) {
        elemToBeStored.keySet().stream().forEach(k ->
                createdRefactoringElement.get(k).addAll(elemToBeStored.get(k)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EasierModel that = (EasierModel) o;
        return Objects.equals(initialElements, that.initialElements) &&
                Objects.equals(targetRefactoringElement, that.targetRefactoringElement) &&
                Objects.equals(createdRefactoringElement, that.createdRefactoringElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialElements, targetRefactoringElement, createdRefactoringElement, modelPath);
    }
}
