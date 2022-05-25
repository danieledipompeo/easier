package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.UMLUtil;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.UMLPackage;

import java.util.*;
import java.util.stream.Collectors;

public class UMLEasierModel extends EasierModel {


    public UMLEasierModel(String mPath) {
        super(mPath);
    }

    @Override
    protected void initMap() {
        targetRefactoringElement = new HashMap<>();
        createdRefactoringElement = new HashMap<>();
        initialElements = new HashMap<>();
        for (String k : List.of(UMLRSolution.SupportedType.NODE.toString(), UMLRSolution.SupportedType.COMPONENT.toString(),
                UMLRSolution.SupportedType.OPERATION.toString())) {
            this.targetRefactoringElement.put(k, new HashSet<>());
            this.createdRefactoringElement.put(k, new HashSet<>());
        }

        // retrieve nodes, components, and operation from the model path
        Set<String> nodes = UMLUtil.getElementsInPackage(modelPath, UMLPackage.Literals.NODE)
                .stream().map(NamedElement.class::cast).map(NamedElement::getName).collect(Collectors.toSet());
        Set<String> components = UMLUtil.getElementsInPackage(modelPath, UMLPackage.Literals.COMPONENT)
                .stream().map(NamedElement.class::cast).map(NamedElement::getName).collect(Collectors.toSet());
        Set<String> operations = UMLUtil.getElementsInPackage(modelPath, UMLPackage.Literals.MESSAGE)
                .stream().map(Message.class::cast).filter(msg -> !msg.getMessageSort().toString().equals("reply"))
                .map(Message::getSignature).map(NamedElement::getName).collect(Collectors.toSet());

        // the immutable map of initial model elements
        initialElements.put(UMLRSolution.SupportedType.NODE.toString(), Collections.unmodifiableSet(nodes));
        initialElements.put(UMLRSolution.SupportedType.COMPONENT.toString(), Collections.unmodifiableSet(components));
        initialElements.put(UMLRSolution.SupportedType.OPERATION.toString(), Collections.unmodifiableSet(operations));

        // fill the element of the model path as candidates for next refactoring actions
        targetRefactoringElement.put(UMLRSolution.SupportedType.NODE.toString(), new HashSet<>(nodes));
        targetRefactoringElement.put(UMLRSolution.SupportedType.COMPONENT.toString(), new HashSet<>(components));
        targetRefactoringElement.put(UMLRSolution.SupportedType.OPERATION.toString(), new HashSet<>(operations));
    }


}
