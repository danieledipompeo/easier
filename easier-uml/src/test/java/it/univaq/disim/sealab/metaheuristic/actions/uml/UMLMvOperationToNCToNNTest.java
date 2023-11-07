package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
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


public class UMLMvOperationToNCToNNTest extends UMLRefactoringActionTest {

//    EasierModel eModel;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

//        eModel = solution.getVariable(0).getEasierModel();

//        oldAction = new UMLMvOperationToNCToNN(eModel.getAvailableElements(),
//                eModel.getInitialElements(), eModel.getAllContents());
//        action = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
    }

    @Test
    public void testConstructor() throws EasierException {
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        String targetOperation =
                action.getTargetElements().get(Configurator.OPERATION_LABEL).iterator().next();
        assertFalse(eModel.getAvailableElements().values().stream().noneMatch(set -> set.contains(targetOperation)),
                String.format("Expected target node %s belongs to the availableElements.", targetOperation));

        String createdNode =
                action.getCreatedElements().get(Configurator.NODE_LABEL).iterator().next();
        assertTrue(eModel.getAvailableElements().values().stream().noneMatch(set -> set.contains(createdNode)),
                String.format("Expected created node %s does not belong to the availableElements.", createdNode));

        String createdComponent =
                action.getCreatedElements().get(Configurator.COMPONENT_LABEL).iterator().next();
        assertTrue(eModel.getAvailableElements().values().stream().noneMatch(set -> set.contains(createdComponent)),
                String.format("Expected created node %s does not belong to the availableElements.", createdComponent));
    }


    @Test
    public void testToCSV() throws EasierException {
        numberOfCSVField = 4;
        actionName = "Move_Operation_New_Component_New_Node";

        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testToCSV();
    }

    @Test
    public void testGetTargetType() throws EasierException {
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        expectedType = Configurator.OPERATION_LABEL;
        super.testGetTargetType();
    }

    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    public void testExecute(String mPath) throws URISyntaxException, EolModelLoadingException, EasierException {
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        assertDoesNotThrow(super::testExecute);
    }

    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    public void testGetTargetElement(String mPath) throws EasierException {
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        expectedName = action.getTargetElements();
        super.testGetTargetElement();
    }

    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    public void testClone(String mPath) throws EasierException {

        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());

        super.testClone();
    }

    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    public void testComputeArchitecturalChanges(String mPath) throws URISyntaxException, EolModelLoadingException,
            EasierException {
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testComputeArchitecturalChanges();
    }
}