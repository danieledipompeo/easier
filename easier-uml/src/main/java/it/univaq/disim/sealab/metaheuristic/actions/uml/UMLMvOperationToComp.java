package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class UMLMvOperationToComp extends UMLRefactoringAction {

    private final static String eolModulePath;


    static {
        eolModulePath = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-refactoringLibrary", "easier-ref-operations", "mv_op_comp.eol").toString();
    }

    public UMLMvOperationToComp() {
        this.name = "moc";
    }

    public UMLMvOperationToComp(Map<String, Set<String>> availableElements, Map<String, Set<String>> initialElements,
                                Collection<?> modelContents)
            throws EasierException {
        this();

        String OPERATION_LABEL = Configurator.OPERATION_LABEL;
        String COMPONENT_LABEL = Configurator.COMPONENT_LABEL;

        Set<String> availableOperations =
                availableElements.get(OPERATION_LABEL);
        String targetOperationName =
                availableOperations.stream().skip(new Random().nextInt(availableOperations.size()-1)).findFirst()
                .orElseThrow(() -> new EasierException("Error when extracting the target element in: " + this.getClass().getSimpleName()));

        targetElements.put(OPERATION_LABEL, Set.of(targetOperationName));

        setIndependent(initialElements);

        Set<String> availableComponents = availableElements.get(Configurator.COMPONENT_LABEL);

        // Extract the name of the operation's owner
        String componentOwner = modelContents.stream().filter(Operation.class::isInstance)
                .map(Operation.class::cast)
                .filter(op -> op.getName().equals(targetOperationName))
                .findFirst().map(Operation::getOwner).map(NamedElement.class::cast).map(NamedElement::getName)
                .orElseThrow(() -> new EasierException("Error when extracting the target element in: " + this.getClass().getSimpleName()));

        // Randomly select a component where to move the operation by excluding the owner of the operation
        targetElements.put(COMPONENT_LABEL,
                Set.of(availableComponents.stream().filter(s -> !s.equals(componentOwner))
                        .skip(new Random().nextInt(availableComponents.size() - 2)).findFirst().orElseThrow(
                                () -> new EasierException("Cannot find a component where to move the operation."))));

        refactoringCost = computeArchitecturalChanges(modelContents);
    }

    private double computeArchitecturalChanges(Collection<?> modelContents) {
        String opToMove = targetElements.get(Configurator.OPERATION_LABEL).iterator().next();

        long msgs = modelContents.stream().filter(Message.class::isInstance)
                .map(Message.class::cast).filter(msg -> !msg.getMessageSort().toString().equals("reply"))
                .filter(m -> opToMove.equals(m.getSignature().getName())).count();

        return msgs;
    }



    @Override
    public void execute(EasierUmlModel contextModel) throws EasierException {

        EOLStandalone executor = new EOLStandalone();
        String OPERATION_LABEL = Configurator.OPERATION_LABEL;
        String COMPONENT_LABEL = Configurator.COMPONENT_LABEL;

        try {
//            EasierUmlModel contextModel = EpsilonStandalone.createUmlModel(sourceModelPath);
//            contextModel.setStoredOnDisposal(true);

            executor.setModel(contextModel);
            executor.setSource(Paths.get(eolModulePath));
            executor.setParameter(getTargetElements().get(OPERATION_LABEL).iterator().next()
                    , "String",
                    "targetOperationName");
            executor.setParameter(getTargetElements().get(COMPONENT_LABEL).iterator().next()
                    , "String",
                    "targetComponentName");

            executor.execute();
        } catch (EolRuntimeException e) {
            String message = String.format("Error in execution the eolmodule %s%n", eolModulePath);
//            message += String.format("No Node called \t %s %n", targetObject.getName());
            message += e.getMessage();
            throw new RuntimeException(message);
        }

        executor.clearMemory();
    }


    @Override
    public String getTargetType() {
        return Configurator.OPERATION_LABEL;
    }


    @Override
    public String toString() {
        return "Move Operation --> " + targetElements.get(Configurator.OPERATION_LABEL).iterator().next() +
                " to" +
                " Component " +
                "-->  " + targetElements.get(Configurator.COMPONENT_LABEL).iterator().next();
    }

    public String toCSV() {
        return String.format("Move_Operation_Component,%s,%s,",
                targetElements.get(Configurator.OPERATION_LABEL).iterator().next(),
                targetElements.get(Configurator.COMPONENT_LABEL).iterator().next());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UMLMvOperationToComp other = (UMLMvOperationToComp) obj;

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
    	return targetElements.get(Configurator.COMPONENT_LABEL).iterator().next();
    }

}
