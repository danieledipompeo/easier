package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.domain.UMLEasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class UMLRefactoringAction implements RefactoringAction {

    protected String name;
    protected boolean isIndependent = true;
    Map<String, Set<String>> targetElements = new HashMap<>();
    Map<String, Set<String>> createdElements = new HashMap<>();

    protected double refactoringCost;

    abstract public void execute(EasierUmlModel model) throws EasierException;

    @Override
    public boolean isIndependent() {
        return isIndependent;
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
    public RefactoringAction clone() {
        try {
            return (RefactoringAction) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
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

    public void updateAvailableElements(EasierModel easierModel){
        easierModel.addElements(createdElements);
    }

    public void restoreAvailableElements(EasierModel easierModel){
        easierModel.removeElements(createdElements);
    }

    public double getRefactoringCost() {
    	return refactoringCost;
    }

    public String getTargetElement() {
        return targetElements.get(getTargetType()).iterator().next();
    }

    public String getDestination() {
        return createdElements.get(getTargetType()).iterator().next();
    }

    public String getDeployment() {
    	return "";
    }

    public String getTaggedValue() {
    	return "";
    }

    public String getScalingFactor() {
    	return "";
    }

}
