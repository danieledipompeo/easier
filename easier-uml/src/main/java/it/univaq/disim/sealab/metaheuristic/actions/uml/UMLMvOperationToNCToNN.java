package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
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

    private long msgs;

    public UMLMvOperationToNCToNN() {
        super("moncnn");
    }

    public UMLMvOperationToNCToNN(UMLRefactoringAction other){
        super(other);
    }

    public UMLMvOperationToNCToNN(Map<String, Set<String>> availableElements, Map<String,
            Set<String>> initialElements) {
        this();

        Set<String> availableOperations = availableElements.get(UMLRSolution.SupportedType.OPERATION.toString());

        Set<String> targetElement = new HashSet<>();
        targetElement.add(availableOperations.stream().skip(new Random().nextInt(availableOperations.size())).findFirst().orElse(null));
        targetElements.put(UMLRSolution.SupportedType.OPERATION.toString(), targetElement);

        setIndependent(initialElements);
        this.createdElements.put(UMLRSolution.SupportedType.NODE.toString(), Set.of("New-Node_" + generateHash()));
        this.createdElements.put(UMLRSolution.SupportedType.COMPONENT.toString(), Set.of("New-Component_" + generateHash()));

    }

    public double computeArchitecturalChanges(Collection<?> modelContents) {
        String opToMove = targetElements.get(UMLRSolution.SupportedType.OPERATION.toString()).iterator().next();
        long msgs = modelContents.stream().filter(Message.class::isInstance)
                .map(Message.class::cast).filter(m -> !m.getMessageSort().toString().equals("reply")).filter(m -> opToMove.equals(m.getSignature().getName())).count();

        return msgs;
    }

    @Override
    public void execute(EasierUmlModel contextModel) throws RuntimeException, EasierException {

        EOLStandalone executor = new EOLStandalone();

        try {
            executor.setModel(contextModel);
            executor.setSource(Paths.get(eolModulePath));

            executor.setParameter(targetElements.get(UMLRSolution.SupportedType.OPERATION.toString()).iterator().next(),
                    "String",
                    "targetOperationName");
            executor.setParameter(createdElements.get(UMLRSolution.SupportedType.COMPONENT.toString()).iterator().next(),
                    "String",
                    "newComponentName");
            executor.setParameter(createdElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next(),
                    "String",
                    "newNodeName");

            executor.execute();
        } catch (EolRuntimeException e) {
            String message = String.format("Error in execution the eolmodule %s%n", eolModulePath);
            message += e.getMessage();
            throw new EasierException(message);
        }

        executor.clearMemory();
    }

    @Override
    public String getTargetType() {
        return UMLRSolution.SupportedType.OPERATION.toString();
    }

    public String toString() {
        return "Move Operation --> " + targetElements.get(UMLRSolution.SupportedType.OPERATION.toString()).iterator().next() + " " +
                "to " +
                "New Component --> " + createdElements.get(UMLRSolution.SupportedType.COMPONENT.toString()).iterator().next()
                + " deployed to a New Node -->" + createdElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next();
    }

    public String toCSV() {
        return String.format("Move_Operation_New_Component_New_Node,%s,%s,%s",
                targetElements.get(UMLRSolution.SupportedType.OPERATION.toString()).iterator().next(),
                createdElements.get(UMLRSolution.SupportedType.COMPONENT.toString()).iterator().next(),
                createdElements.get(UMLRSolution.SupportedType.NODE.toString()).iterator().next());
    }

    public RefactoringAction copy(){
        return new UMLMvOperationToNCToNN(this);
    }
}
