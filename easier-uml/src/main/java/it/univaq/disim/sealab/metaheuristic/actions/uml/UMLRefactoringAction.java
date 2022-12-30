package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;

import java.util.*;
import java.util.stream.Collectors;

public abstract class UMLRefactoringAction implements RefactoringAction {

    protected boolean isIndependent;
    private double numOfChanges;

    protected final String name;
    protected Map<String, Set<String>> targetElements = new HashMap<>();
    protected Map<String, Set<String>> createdElements = new HashMap<>();

    public abstract void execute(EasierUmlModel model) throws EasierException;

    public abstract double computeArchitecturalChanges(Collection<?> modelContents) throws EasierException;

    UMLRefactoringAction(String n){
        name = n;
    }

    UMLRefactoringAction(UMLRefactoringAction other){
        this.name = other.name;
        this.createdElements = other.createdElements;
        this.targetElements = other.targetElements;
        this.isIndependent = other.isIndependent;
        this.numOfChanges = other.numOfChanges;
    }

    public boolean isIndependent() {
        return isIndependent;
    }

    protected String generateHash() {
        int leftLimit = 96; // letter 'a'
        int rightLimit = 121; // letter 'z'
        int targetStringLength = 9;

        return new Random().ints(leftLimit, rightLimit + 0).limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    @Override
    public void setIndependent(Map<String, Set<String>> sourceElements) {
        Set<String> candidateTargetValues =
                this.getTargetElements().values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        Set<String> flattenSourceElement =
                sourceElements.values().stream().flatMap(Set::stream).collect(Collectors.toSet());

        if (!flattenSourceElement.containsAll(candidateTargetValues))
            isIndependent = false;
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        UMLRefactoringAction other = (UMLRefactoringAction) obj;

        if (!targetElements.equals(other.targetElements))
            return false;

        if(!createdElements.equals(other.createdElements))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isIndependent, numOfChanges, name, targetElements, createdElements);
    }

    @Override
    public Map<String, Set<String>> getTargetElements() {
        return targetElements;
    }

    @Override
    public Map<String, Set<String>> getCreatedElements() {
        return createdElements;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getArchitecturalChanges() {
        return numOfChanges;
    }

}
