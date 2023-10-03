package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.NamedElement;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class UMLRemoveComponent extends UMLRefactoringAction {

    private final static Path eolModulePath;

    static {
        eolModulePath = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-refactoringLibrary", "easier-ref-operations", "remove_comp.eol");
    }

    public UMLRemoveComponent() {
        name = "remove_component";
    }

    public UMLRemoveComponent(Map<String, Set<String>> availableElements, Map<String, Set<String>> sourceElements)
            throws EasierException {
        this();

        Set<String> availableComponent = availableElements.get(UMLRSolution.SupportedType.COMPONENT.toString());
        Set<String> targetElement = new HashSet<>();
        targetElement.add(
                availableComponent.stream().skip(new Random().nextInt(availableComponent.size() - 1)).findFirst()
                        .orElseThrow(() -> new EasierException(
                                "Error when extracting the target element in: " + this.getClass().getSimpleName())));
        targetElements.put(UMLRSolution.SupportedType.COMPONENT.toString(), targetElement);

        // check whether the action is using an element created by another action
        setIndependent(sourceElements);
    }

    @Override
    public String getTargetType() {
        return UMLRSolution.SupportedType.COMPONENT.toString();
    }

    @Override
    public String toString() {
        return "Removing --> " + targetElements.get(UMLRSolution.SupportedType.COMPONENT.toString()).iterator().next();
    }

    @Override
    public String toCSV() {
        return String.format("%s,%s,,,,,", name,
                targetElements.get(UMLRSolution.SupportedType.COMPONENT.toString()).iterator().next());
    }

    /**
     * Compute the architectural changes of the refactoring action.
     * It uses the same cost of the move component action.
     *
     * @param modelContents is the collection of elements in the model
     * @return (intUsage + intReal + ops) the cost of the refactoring action
     * @throws EasierException if the target element is empty
     */
    @Override
    public double computeArchitecturalChanges(Collection<?> modelContents) throws EasierException {

        Component compToMove =
                (Component) modelContents.stream().filter(Component.class::isInstance)
                        .map(NamedElement.class::cast).filter(ne -> ne.getName()
                                .equals(targetElements.get(UMLRSolution.SupportedType.COMPONENT.toString()).iterator()
                                        .next())).findFirst().orElseThrow(() -> new EasierException(
                                "Architectural changes of RemoveComponent cannot be computed. Target elements is empty"));

        int intUsage = compToMove.getUsedInterfaces().size();
        int intReal = compToMove.getInterfaceRealizations().size();
        int ops = compToMove.getOperations().size();

        return (intUsage + intReal + ops);
    }

    @Override
    public void execute(EasierUmlModel contextModel) throws EasierException {
        EOLStandalone executor = new EOLStandalone();

        try {
            executor.setModel(contextModel);
            executor.setSource(eolModulePath);

            executor.setParameter(targetElements.get(UMLRSolution.SupportedType.COMPONENT.toString()).iterator().next(),
                    "String", "targetComponentName");

            executor.execute();
        } catch (EolRuntimeException e) {
            String message = String.format("Error in execution the eolmodule %s%n ", eolModulePath);
            message += e.getMessage();
            throw new EasierException(message);
        }
        executor.clearMemory();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UMLRemoveComponent other = (UMLRemoveComponent) obj;

        return targetElements.equals(other.targetElements);
    }

    @Override
    public void updateAvailableElements(EasierModel easierModel) {
        easierModel.removeElements(targetElements);
    }

    @Override
    public void restoreAvailableElements(EasierModel easierModel) {
        easierModel.addElements(targetElements);
    }
}
