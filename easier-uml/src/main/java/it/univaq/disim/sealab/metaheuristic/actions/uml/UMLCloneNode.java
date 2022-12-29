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
import org.eclipse.uml2.uml.Node;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class UMLCloneNode extends UMLRefactoringAction {

    private final static Path eolModulePath;


    static {
        eolModulePath = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-refactoringLibrary", "easier-ref-operations", "clone_node.eol");
    }


    public UMLCloneNode(Map<String, Set<String>> availableElements, Map<String, Set<String>> sourceElements) {
        this();

        Set<String> availableNode = availableElements.get(UMLRSolution.SupportedType.NODE.toString());
        Set<String> targetElement = new HashSet<>();
        targetElement.add(availableNode.stream().skip(new Random().nextInt(availableNode.size())).findFirst().orElse(null));
        targetElements.put(UMLRSolution.SupportedType.NODE.toString(), targetElement);
        // check whether the action is using an element created by another action
        setIndependent(sourceElements);
        String clonedNode = targetElement.iterator().next() + "_" + generateHash();
        createdElements.put(UMLRSolution.SupportedType.NODE.toString(),
                Set.of(clonedNode));
    }

    private UMLCloneNode() {
        super("clone");
    }

    public UMLCloneNode(UMLRefactoringAction other){
        super(other);
    }

    public double computeArchitecturalChanges(Collection<?> modelContents) throws EasierException {

        Node targetObject = modelContents.stream().filter(Node.class::isInstance).
                map(Node.class::cast).
                filter(ne -> ne.getName().equals(targetElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next())).findFirst().orElse(null);

        if (targetObject == null)
            throw new EasierException("Error when computing the architectural changes of " + this.getName());

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

            executor.setParameter(targetElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next(), "String", "targetNodeName");
            executor.setParameter(createdElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next(), "String", "clonedNodeName");

            executor.execute();
        } catch (EolRuntimeException e) {
            String message = String.format("Error in execution the eolmodule %s%n ", eolModulePath);
            message += e.getMessage();
            throw new EasierException(message);
        }
        executor.clearMemory();
    }

    @Override
    public String getTargetType() {
        return UMLRSolution.SupportedType.NODE.toString();
    }

    @Override
    public String toString() {
        return "Cloning --> " + targetElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next() + " " +
                "with -->  " + createdElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next();
    }

    public String toCSV() {
        return String.format("UMLCloneNode,%s,%s,",
                targetElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next(),
                createdElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next());
    }

    public RefactoringAction copy(){
        UMLRefactoringAction action = new UMLCloneNode(this);

        return action;
    }

}
