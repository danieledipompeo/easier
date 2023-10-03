package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.uml2.uml.NamedElement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.URISyntaxException;
import java.util.stream.Collectors;


class UMLRemoveComponentTest extends UMLRefactoringActionTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();

        oldAction = new UMLRemoveComponent(eModel.getAvailableElements(), eModel.getInitialElements());
        action = new UMLRemoveComponent(eModel.getAvailableElements(), eModel.getInitialElements());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void execute_does_not_throw_exception() {
        Assertions.assertDoesNotThrow(super::testExecute);
    }

    @Test
    void target_node_should_be_removed() throws URISyntaxException, EolModelLoadingException, EasierException {
        //super.testExecute();
        EasierUmlModel model = EOLStandalone.createUmlModel(modelpath);
        model.setStoredOnDisposal(true);
        action.execute(model);

        String target =
                action.getTargetElements().get(UMLRSolution.SupportedType.COMPONENT.toString()).iterator().next();

        System.out.println(target);

        model = EOLStandalone.createUmlModel(modelpath);

        // collect name of all elements in the model
        Assertions.assertFalse(model.allContents().stream().filter(NamedElement.class::isInstance)
                        .map(NamedElement.class::cast)
                        .map(NamedElement::getName)
                        .collect(Collectors.toList()).contains(target),
                String.format("Expected target node: %s has been removed from the model.", target));
    }

    @Test
    void computeArchitecturalChanges_should_not_throw_easier_exception()
            throws EasierException, URISyntaxException, EolModelLoadingException {
        super.testComputeArchitecturalChanges();
    }
}