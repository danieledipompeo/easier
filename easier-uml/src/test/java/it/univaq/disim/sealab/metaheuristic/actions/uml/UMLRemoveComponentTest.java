package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.domain.UMLEasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.uml2.uml.NamedElement;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URISyntaxException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Disabled
class UMLRemoveComponentTest extends UMLRefactoringActionTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();

//        oldAction = new UMLRemoveComponent(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
//        action = new UMLRemoveComponent(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
    }

    @AfterEach
    void tearDown() {
    }

    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    void execute_does_not_throw_exception(String mPath) throws EasierException {
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLResourceScaling(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        assertDoesNotThrow(super::testExecute);
    }

    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    void target_node_should_be_removed(String mPath) throws URISyntaxException, EolModelLoadingException,
            EasierException {
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLRemoveComponent(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());

        EasierUmlModel model = EOLStandalone.createUmlModel(modelpath);
        model.setStoredOnDisposal(true);
        action.execute(model);

        String target =
                action.getTargetElements().get(Configurator.COMPONENT_LABEL).iterator().next();

        System.out.println(target);

        model = EOLStandalone.createUmlModel(modelpath);

        // collect name of all elements in the model
        Assertions.assertFalse(model.allContents().stream().filter(NamedElement.class::isInstance)
                        .map(NamedElement.class::cast)
                        .map(NamedElement::getName)
                        .collect(Collectors.toList()).contains(target),
                String.format("Expected target node: %s has been removed from the model.", target));
    }

    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    void computeArchitecturalChanges_should_not_throw_easier_exception(String mPath)
            throws EasierException, URISyntaxException, EolModelLoadingException {
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLResourceScaling(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testComputeArchitecturalChanges();
    }
}