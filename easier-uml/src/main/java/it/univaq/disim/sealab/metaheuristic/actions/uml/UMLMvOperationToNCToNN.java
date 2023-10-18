package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.uml2.uml.Message;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.*;

public class UMLMvOperationToNCToNN extends UMLRefactoringAction {

    private final static String eolModulePath;

    static {
        eolModulePath = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-refactoringLibrary", "easier-ref-operations", "mv_op_nc_nn.eol").toString();
    }

    public UMLMvOperationToNCToNN() {
        name = "moncnn";
    }

    public UMLMvOperationToNCToNN(Map<String, Set<String>> availableElements, Map<String,
            Set<String>> initialElements, Collection<?> modelContents) throws EasierException {
        this();

        Set<String> availableOperations = availableElements.get(Configurator.OPERATION_LABEL);

        Set<String> targetElement = new HashSet<>();
        targetElement.add(availableOperations.stream().skip(new Random().nextInt(availableOperations.size()-1)).findFirst()
                .orElseThrow(() -> new EasierException("Error when extracting the target element in: " + this.getClass().getSimpleName())));
        targetElements.put(Configurator.OPERATION_LABEL, targetElement);

        setIndependent(initialElements);
        Set<String> createdElements = new HashSet<>();
        createdElements.add("New-Node_" + generateHash());
        this.createdElements.put(Configurator.NODE_LABEL, Set.copyOf(createdElements));
        createdElements.clear();
        createdElements.add("New-Component_" + generateHash());
        this.createdElements.put(Configurator.COMPONENT_LABEL, Set.copyOf(createdElements));

        refactoringCost = computeArchitecturalChanges(modelContents);

    }

    private double computeArchitecturalChanges(Collection<?> modelContents) {
        String opToMove = targetElements.get(Configurator.OPERATION_LABEL).iterator().next();
        long msgs = modelContents.stream().filter(Message.class::isInstance)
                .map(Message.class::cast).filter(m -> !m.getMessageSort().toString().equals("reply")).filter(m -> opToMove.equals(m.getSignature().getName())).count();

        return msgs;
    }

    private String generateHash() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        return new Random().ints(leftLimit, rightLimit + 1).limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    @Override
    public void execute(EasierUmlModel contextModel) throws RuntimeException, EasierException {

        EOLStandalone executor = new EOLStandalone();

        try {

            executor.setModel(contextModel);
            executor.setSource(Paths.get(eolModulePath));

            executor.setParameter(targetElements.get(Configurator.OPERATION_LABEL).iterator().next(),
                    "String",
                    "targetOperationName");
            executor.setParameter(createdElements.get(Configurator.COMPONENT_LABEL).iterator().next(),
                    "String",
                    "newComponentName");
            executor.setParameter(createdElements.get(Configurator.NODE_LABEL).iterator().next(),
                    "String",
                    "newNodeName");

            executor.execute();
        } catch (EolRuntimeException e) {
            String message = String.format("Error in execution the eolmodule %s%n", eolModulePath);
//            message += String.format("No Node called \t %s %n", targetObject.getName());
            message += e.getMessage();
            throw new EasierException(message);
        }

        executor.clearMemory();
    }

    @Override
    public String getTargetType() {
        return Configurator.OPERATION_LABEL;
    }

    public String toString() {
        return "Move Operation --> " + targetElements.get(Configurator.OPERATION_LABEL).iterator().next() + " " +
                "to " +
                "New Component --> " + createdElements.get(Configurator.COMPONENT_LABEL).iterator().next()
                + " deployed to a New Node -->" + createdElements.get(Configurator.NODE_LABEL).iterator().next();
    }

    public String toCSV() {
        return String.format("Move_Operation_New_Component_New_Node,%s,%s,%s",
                targetElements.get(Configurator.OPERATION_LABEL).iterator().next(),
                createdElements.get(Configurator.COMPONENT_LABEL).iterator().next(),
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
        UMLMvOperationToNCToNN other = (UMLMvOperationToNCToNN) obj;

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

    public String getDestination() {
    	return createdElements.get(Configurator.COMPONENT_LABEL).iterator().next();
    }

    @Override
    public String getDeployment() {
    	return createdElements.get(Configurator.NODE_LABEL).iterator().next();
    }

}
