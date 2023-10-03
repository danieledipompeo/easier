package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.uml2.uml.Node;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class UMLRemoveNode extends UMLRefactoringAction {

    private final static Path eolModulePath;

    static{
        eolModulePath = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-refactoringLibrary", "easier-ref-operations", "remove_node.eol");
    }

    public UMLRemoveNode() {
        name = "remove_node";
    }

    public UMLRemoveNode(Map<String, Set<String>> availableElements, Map<String, Set<String>> sourceElements)
            throws EasierException {
        this();

        Set<String> availableNode = availableElements.get(UMLRSolution.SupportedType.NODE.toString());
        Set<String> targetElement = new HashSet<>();
        targetElement.add(availableNode.stream().skip(new Random().nextInt(availableNode.size()-1)).findFirst()
                .orElseThrow(() -> new EasierException("Error when extracting the target element in: " + this.getClass().getSimpleName())));
        targetElements.put(UMLRSolution.SupportedType.NODE.toString(), targetElement);

        // check whether the action is using an element created by another action
        setIndependent(sourceElements);
    }

    @Override
    public String getTargetType() {
        return UMLRSolution.SupportedType.NODE.toString();
    }

    @Override
    public String toString() {
        return "Removing --> " + targetElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next();
    }

    @Override
    public String toCSV() {
        return String.format("%s,%s,,,,,",name,
                targetElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next());
    }

    @Override
    public double computeArchitecturalChanges(Collection<?> modelContents) throws EasierException {
        Node targetObject = modelContents.stream().filter(Node.class::isInstance)
                .map(Node.class::cast)
                .filter(ne -> ne.getName()
                        .equals(targetElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next()))
                .findFirst().orElseThrow(() -> new EasierException(
                        "Architectural changes of RemoveComponent cannot be computed. Target elements is empty"));

        int cpSize = targetObject.getCommunicationPaths().size();

        int artSize = (int) targetObject.getDeployments().stream().mapToLong(d -> d.getDeployedArtifacts().size()).sum();

        return (cpSize + artSize);
    }

    @Override
    public void execute(EasierUmlModel contextModel) throws EasierException {
        EOLStandalone executor = new EOLStandalone();

        try {
            executor.setModel(contextModel);
            executor.setSource(eolModulePath);

            executor.setParameter(targetElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next(),
                    "String", "targetNodeName");

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
        UMLRemoveNode other = (UMLRemoveNode) obj;

        return targetElements.equals(other.targetElements);
    }

    @Override
    public void updateAvailableElements(EasierModel easierModel){
        easierModel.removeElements(targetElements);
    }

    @Override
    public void restoreAvailableElements(EasierModel easierModel){
        easierModel.addElements(targetElements);
    }
}
