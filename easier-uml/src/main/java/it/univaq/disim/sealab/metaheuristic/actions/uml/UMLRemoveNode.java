package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Node;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class UMLRemoveNode extends UMLRefactoringAction {

    private final static Path eolModulePath;

    static{
        eolModulePath = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-refactoringLibrary", "easier-ref-operations", "remove_node.eol");
    }

    public UMLRemoveNode() {
        name = "remove_node";
    }

    public UMLRemoveNode(Map<String, Set<String>> availableElements, Map<String, Set<String>> sourceElements, Collection<?> modelContents)
            throws EasierException {
        this();

        Set<String> availableNode = availableElements.get(Configurator.NODE_LABEL);
        Set<String> targetElement = new HashSet<>();
        targetElement.add(availableNode.stream()
                        .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                            Collections.shuffle(list);
                            return list;
                        })).stream()
                .findAny()
                .orElseThrow(() -> new EasierException("Cannot find a target node.")));

        targetElements.put(Configurator.NODE_LABEL, Set.copyOf(targetElement));

        // check whether the action is using an element created by another action
        setIndependent(sourceElements);

        refactoringCost = computeArchitecturalChanges(modelContents);
    }

    @Override
    public String getTargetType() {
        return Configurator.NODE_LABEL;
    }

    @Override
    public String toString() {
        return "Removing --> " + targetElements.get(Configurator.NODE_LABEL).iterator().next();
    }

    @Override
    public String toCSV() {
        return String.format("%s,%s,,,,,",name,
                targetElements.get(Configurator.NODE_LABEL).iterator().next());
    }

    private double computeArchitecturalChanges(Collection<?> modelContents) throws EasierException {
        Node targetObject = modelContents.stream().filter(Node.class::isInstance)
                .map(Node.class::cast)
                .filter(ne -> ne.getName()
                        .equals(targetElements.get(Configurator.NODE_LABEL).iterator().next()))
                .findFirst().orElseThrow(() -> new EasierException(
                        "Architectural changes of RemoveNode cannot be computed. Target element is empty"));

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

            executor.setParameter(targetElements.get(Configurator.NODE_LABEL).iterator().next(),
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

    @Override
    public String getDestination() {
        return "";
    }

}
