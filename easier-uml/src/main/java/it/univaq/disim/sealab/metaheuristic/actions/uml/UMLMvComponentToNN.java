/**
 * It moves a component from a node to another one
 */
package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import it.univaq.disim.sealab.metaheuristic.utils.NodeType;
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
            Set<String>> initialElements, Collection<?> modelContents) throws EasierException {
        this();

        Set<String> availableComponents = availableElements.get(Configurator.COMPONENT_LABEL);
        Set<String> targetElement = new HashSet<>();
        targetElement.add(availableComponents.stream().skip(new Random().nextInt(availableComponents.size()-1)).findFirst()
                .orElseThrow(() -> new EasierException("Error when extracting the target element in: " + this.getClass().getSimpleName())));
        targetElements.put(Configurator.COMPONENT_LABEL, targetElement);
        setIndependent(initialElements);
        Set<String> createdNodeElements = new HashSet<>();

        // random select a nodetype from the list of available node types
        NodeType nodeType = Configurator.eINSTANCE.getNodeCharacteristics().stream()
                .skip(new Random().nextInt(Configurator.eINSTANCE.getNodeCharacteristics().size() - 1)).findFirst()
                .orElseThrow(() -> new EasierException(
                        "Error when extracting the node type in: " + this.getClass().getSimpleName()));

        createdNodeElements.add("New-Node_" + nodeType.getLabel() + "_" + generateHash());
        createdElements.put(Configurator.NODE_LABEL, createdNodeElements);

        // Set the node characteristics. Randomly select a node type from the list of available node types
        speedFactor = nodeType.getPerformance();
        energyFactor = nodeType.getEnergy();
        costFactor = nodeType.getCost();

        refactoringCost = computeArchitecturalChanges(modelContents);
    }

    private double computeArchitecturalChanges(Collection<?> modelContents) throws EasierException {

        Component compToMove =
                (Component) modelContents.stream().filter(Component.class::isInstance)
                        .map(NamedElement.class::cast).filter(ne -> ne.getName()
                                .equals(targetElements.get(Configurator.COMPONENT_LABEL).iterator().next())).findFirst().orElse(null);

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
        return Configurator.COMPONENT_LABEL;
    }

    @Override
    public void execute(EasierUmlModel contextModel) throws EasierException {
        EOLStandalone executor = new EOLStandalone();

        try {
            executor.setModel(contextModel);
            executor.setSource(eolModulePath);

            // fills variable within the eol module
            executor.setParameter(targetElements.get(Configurator.COMPONENT_LABEL).iterator().next(),
                    "String",
                    "targetComponentName");
            executor.setParameter(createdElements.get(Configurator.NODE_LABEL).iterator().next(),
                    "String",
                    "newNodeName");

            executor.setParameter(String.valueOf(speedFactor), "Real", "speed");
            executor.setParameter(String.valueOf(energyFactor), "Real", "energy");
            executor.setParameter(String.valueOf(costFactor), "Real", "cost");

            executor.execute();
        } catch (EolRuntimeException e) {
            String message = String.format("Error in execution the eolmodule %s on: %s. The reason is:" +
                            " %s %n", eolModulePath, this, e.getMessage());
            throw new EasierException(message);
        }

        executor.clearMemory();
    }

    @Override
    public String toString() {
        return "Moving --> " + targetElements.get(Configurator.COMPONENT_LABEL).iterator().next() +
                " to --> " + createdElements.get(Configurator.NODE_LABEL).iterator().next();
    }

    public String toCSV() {
        return String.format("Move_Component_New_Node,%s,%s,",
                targetElements.get(Configurator.COMPONENT_LABEL).iterator().next(),
                createdElements.get(Configurator.NODE_LABEL).iterator().next());
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

    @Override
    public String getDestination() {
    	return createdElements.get(Configurator.NODE_LABEL).iterator().next();
    }
}
