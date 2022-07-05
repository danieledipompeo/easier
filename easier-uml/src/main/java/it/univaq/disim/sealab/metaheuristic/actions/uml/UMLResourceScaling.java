package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class UMLResourceScaling extends UMLRefactoringAction {

    private final static Path eolModulePath;

    static {
        eolModulePath = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-refactoringLibrary", "easier-ref-operations", "change_passive_resource.eol");
    }

    private String taggedValue;
    private double scaledFactor;

    public UMLResourceScaling() {
        name = "resource_scaling";
    }

    public UMLResourceScaling(Map<String, Set<String>> availableElements, Map<String, Set<String>> sourceElements) {
        this();

        Set<String> availableNode = availableElements.get(UMLRSolution.SupportedType.NODE.toString());
        Set<String> targetElement = new HashSet<>();
        targetElement.add(availableNode.stream().skip(new Random().nextInt(availableNode.size())).findFirst().orElse(null));
        targetElements.put(UMLRSolution.SupportedType.NODE.toString(), targetElement);

        // check whether the action is using an element created by another action
        setIndependent(sourceElements);

        // Extract a random tagged value for the action
        taggedValue = "speedFactor";

        // Random taggedValue value
        scaledFactor = JMetalRandom.getInstance().nextDouble(0.5, 1.5);

    }

    @Override
    public void execute(EasierUmlModel contextModel) throws EasierException {
        EOLStandalone executor = new EOLStandalone();

        try {
            executor.setModel(contextModel);
            executor.setSource(eolModulePath);

            executor.setParameter(targetElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next(), "String", "targetNodeName");

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
        return String.format("Change Passive Resource: %s of: %s with: %s",
                taggedValue,
                targetElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next(),
                scaledFactor);
    }

    public String toCSV() {
        return String.format("%s,%s,,",
                name,
                targetElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next());
    }

    @Override
    public double computeArchitecturalChanges(Collection<?> modelContents) throws EasierException {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UMLResourceScaling other = (UMLResourceScaling) obj;

        if (!targetElements.equals(other.targetElements))
            return false;
        if (!taggedValue.equals(other.taggedValue))
            return false;
        if (scaledFactor != other.scaledFactor)
            return false;
        return true;
    }


}
