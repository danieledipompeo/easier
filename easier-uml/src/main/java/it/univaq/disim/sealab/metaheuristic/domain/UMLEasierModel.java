package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.UMLUtil;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.UMLPackage;

import java.net.URISyntaxException;
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
        for (String k : List.of(Configurator.NODE_LABEL, Configurator.COMPONENT_LABEL,
                Configurator.OPERATION_LABEL)) {
            this.targetRefactoringElement.put(k, new HashSet<>());
            this.createdRefactoringElement.put(k, new HashSet<>());
        }

        // retrieve nodes, components, and operations from the model path
        Set<String> nodes = UMLUtil.getElementsInPackage(modelPath, UMLPackage.Literals.NODE)
                .stream().map(NamedElement.class::cast).map(NamedElement::getName).collect(Collectors.toSet());
        Set<String> components = UMLUtil.getElementsInPackage(modelPath, UMLPackage.Literals.COMPONENT)
                .stream().map(NamedElement.class::cast).map(NamedElement::getName).collect(Collectors.toSet());
        Set<String> operations = UMLUtil.getElementsInPackage(modelPath, UMLPackage.Literals.MESSAGE)
                .stream().map(Message.class::cast).filter(msg -> !msg.getMessageSort().toString().equals("reply"))
                .map(Message::getSignature).map(NamedElement::getName).collect(Collectors.toSet());

        // the immutable map of initial model elements
        initialElements.put(Configurator.NODE_LABEL, Collections.unmodifiableSet(nodes));
        initialElements.put(Configurator.COMPONENT_LABEL, Collections.unmodifiableSet(components));
        initialElements.put(Configurator.OPERATION_LABEL, Collections.unmodifiableSet(operations));

        // fill the element of the model path as candidates for next refactoring actions
        targetRefactoringElement.put(Configurator.NODE_LABEL, new HashSet<>(nodes));
        targetRefactoringElement.put(Configurator.COMPONENT_LABEL, new HashSet<>(components));
        targetRefactoringElement.put(Configurator.OPERATION_LABEL, new HashSet<>(operations));
    }


    public java.util.Collection<?> getAllContents(){

        try (EasierUmlModel model = EOLStandalone.createUmlModel(modelPath.toString())) {

            return model.allContents();

        } catch (URISyntaxException | EolModelLoadingException e) {
            throw new RuntimeException(e);
        }
    }

}
