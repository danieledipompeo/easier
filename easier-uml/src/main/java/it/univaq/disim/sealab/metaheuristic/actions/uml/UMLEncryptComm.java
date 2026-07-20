package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Prototype security-hardening action: models "encrypt this node's outbound communication" as a
 * performance-costed refactoring, reusing the existing GaExecHost::speedFactor mechanism
 * (UMLResourceScaling's tag) so the effect flows through the unmodified easier-uml2lqn pipeline.
 * See docs/security-refactoring-proposal.md for the full design rationale.
 */
public class UMLEncryptComm extends UMLRefactoringAction {

    private final static Path eolModulePath;

    static {
        eolModulePath = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-refactoringLibrary", "easier-ref-operations", "encrypt_comm.eol");
    }

    private String taggedValue;
    private double overheadFactor;

    public UMLEncryptComm() {
        name = "encrypt_comm";
    }

    public UMLEncryptComm(Map<String, Set<String>> availableElements, Map<String, Set<String>> sourceElements, Collection<?> modelContents)
            throws EasierException {
        this();

        Set<String> availableNode = availableElements.get(Configurator.NODE_LABEL);
        Set<String> targetElement = new HashSet<>();
        targetElement.add(availableNode.stream().skip(new Random().nextInt(availableNode.size()-1)).findFirst()
                .orElseThrow(() -> new EasierException("Error when extracting the target element in: " + this.getClass().getSimpleName())));
        targetElements.put(Configurator.NODE_LABEL, targetElement);

        // check whether the action is using an element created by another action
        setIndependent(sourceElements);

        // Extract a random tagged value for the action
        taggedValue = "speedFactor";

        // Encryption always costs performance: narrower, always-degrading range than
        // UMLResourceScaling's general [0.5, 1.5] rescaling.
        overheadFactor = JMetalRandom.getInstance().nextDouble(0.7, 0.95);
        refactoringCost = computeArchitecturalChanges();
    }

    @Override
    public void execute(EasierUmlModel contextModel) throws EasierException {
        EOLStandalone executor = new EOLStandalone();

        try {
            executor.setModel(contextModel);
            executor.setSource(eolModulePath);

            String targetNodeName = targetElements.get(Configurator.NODE_LABEL).iterator().next();
            executor.setParameter(targetNodeName, "String", "targetNodeName");
            executor.setParameter(String.valueOf(overheadFactor), "Real", "overheadFactor");
            executor.setParameter(
                    String.format("EASIER-SECURITY: encryption enabled (overhead factor %s)", overheadFactor),
                    "String", "securityComment");

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
        return Configurator.NODE_LABEL;
    }

    @Override
    public String toString() {
        return String.format("Encrypt communication: %s of: %s with: %s",
                taggedValue,
                targetElements.get(Configurator.NODE_LABEL).iterator().next(),
                overheadFactor);
    }

    public String toCSV() {
        return String.format("%s,%s,,,%s,%s",
                name,
                targetElements.get(Configurator.NODE_LABEL).iterator().next(),
                taggedValue,
                overheadFactor);
    }

    private double computeArchitecturalChanges() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UMLEncryptComm other = (UMLEncryptComm) obj;

        if (!targetElements.equals(other.targetElements))
            return false;
        if (!taggedValue.equals(other.taggedValue))
            return false;
        if (overheadFactor != other.overheadFactor)
            return false;
        return true;
    }

    public String getTaggedValue() {
        return taggedValue;
    }

    public String getOverheadFactor() {
        return String.valueOf(overheadFactor);
    }

}
