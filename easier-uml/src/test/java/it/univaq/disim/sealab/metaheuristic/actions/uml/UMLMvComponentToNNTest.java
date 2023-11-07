package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.domain.UMLEasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;


public class UMLMvComponentToNNTest extends UMLRefactoringActionTest {

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

//        oldAction = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
//                eModel.getAllContents());
//        action = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
//                eModel.getAllContents());
    }

    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    void testConstructor(String mPath) throws EasierException {
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        assertDoesNotThrow(() -> action = new UMLMvComponentToNN(eModel.getAvailableElements(),
                eModel.getInitialElements(),
                eModel.getAllContents()));
        String createdElement = action.getCreatedElements().get(Configurator.NODE_LABEL).iterator().next();

        assertTrue(eModel.getAvailableElements().values().stream().noneMatch(set -> set.contains(createdElement)), "Expected created node not in the available elements");
    }

    @Test
    public void testToCSV() throws EasierException {
        numberOfCSVField = 3;
        actionName = "Move_Component_New_Node";
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testToCSV();

    }

    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    public void testExecute(String mPath) throws URISyntaxException, EolModelLoadingException, EasierException {
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        assertDoesNotThrow(super::testExecute);
    }

    @Test
    public void testGetTargetType() throws EasierException {
        expectedType = Configurator.COMPONENT_LABEL;
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
        super.testGetTargetType();
    }

    @Test
    public void testGetTargetElement() throws EasierException {
       String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
        expectedName = action.getTargetElements();
        super.testGetTargetElement();
    }

    @Test
    public void testClone() throws EasierException {
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testClone();
    }

    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    public void testComputeArchitecturalChanges(String mPath) throws URISyntaxException, EolModelLoadingException,
            EasierException {
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
        super.testComputeArchitecturalChanges();
    }
}