/**
 * It moves a component from a node to another one
 */
package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.NamedElement;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class UMLMvComponentToNN extends UMLRefactoringAction {

    private final static Path eolModulePath;

    static {
        eolModulePath = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-refactoringLibrary", "easier-ref-operations", "mv_comp_nn.eol");
    }

    public UMLMvComponentToNN() {
        name = "mcnn";
    }

    public UMLMvComponentToNN(Map<String, Set<String>> availableElements, Map<String,
            Set<String>> initialElements) throws EasierException {
        this();

        Set<String> availableComponents = availableElements.get(UMLRSolution.SupportedType.COMPONENT.toString());
        Set<String> targetElement = new HashSet<>();
        targetElement.add(availableComponents.stream().skip(new Random().nextInt(availableComponents.size()-1)).findFirst()
                .orElseThrow(() -> new EasierException("Error when extracting the target element in: " + this.getClass().getSimpleName())));
        targetElements.put(UMLRSolution.SupportedType.COMPONENT.toString(), targetElement);
        setIndependent(initialElements);
        Set<String> createdNodeElements = new HashSet<>();
        createdNodeElements.add("New-Node_" + generateHash());
        createdElements.put(UMLRSolution.SupportedType.NODE.toString(), createdNodeElements);
    }

    public double computeArchitecturalChanges(Collection<?> modelContents) throws EasierException {

        Component compToMove =
                (Component) modelContents.stream().filter(Component.class::isInstance)
                        .map(NamedElement.class::cast).filter(ne -> ne.getName()
                                .equals(targetElements.get(UMLRSolution.SupportedType.COMPONENT.toString()).iterator().next())).findFirst().orElse(null);

        if (compToMove == null)
            throw new EasierException("Error when computing the architectural changes of " + this.getName());


        int intUsage = compToMove.getUsedInterfaces().size();
        int intReal = compToMove.getInterfaceRealizations().size();
        int ops = compToMove.getOperations().size();

        return (intUsage + intReal + ops);
    }

    private String generateHash() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        return new Random().ints(leftLimit, rightLimit + 1).limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }


    @Override
    public String getTargetType() {
        return UMLRSolution.SupportedType.COMPONENT.toString();
    }

    @Override
    public void execute(EasierUmlModel contextModel) throws EasierException {
        EOLStandalone executor = new EOLStandalone();

        try {
            executor.setModel(contextModel);
            executor.setSource(eolModulePath);

            // fills variable within the eol module
            executor.setParameter(targetElements.get(UMLRSolution.SupportedType.COMPONENT.toString()).iterator().next(),
                    "String",
                    "targetComponentName");
            executor.setParameter(createdElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next(),
                    "String",
                    "newNodeName");
            executor.execute();
        } catch (EolRuntimeException e) {
            String message = String.format("Error in execution the eolmodule %s%n ", eolModulePath);
            message += e.getMessage();
            throw new EasierException(message);
        }

        executor.clearMemory();
    }

    @Override
    public String toString() {
        return "Moving --> " + targetElements.get(UMLRSolution.SupportedType.COMPONENT.toString()).iterator().next() +
                " to --> " + createdElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next();
    }

    public String toCSV() {
        return String.format("Move_Component_New_Node,%s,%s,",
                targetElements.get(UMLRSolution.SupportedType.COMPONENT.toString()).iterator().next(),
                createdElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UMLMvComponentToNN other = (UMLMvComponentToNN) obj;

        if (!targetElements.equals(other.targetElements))
            return false;

        for (String k : createdElements.keySet()) {
            for (String elemName : createdElements.get(k)) {
                if (!other.createdElements.get(k).contains(elemName))
                    return false;
            }
        }
        return true;
    }
}
