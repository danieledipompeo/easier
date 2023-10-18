package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class UMLMvOperationToNCToNNTest extends UMLRefactoringActionTest {

//    EasierModel eModel;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

//        eModel = solution.getVariable(0).getEasierModel();

        oldAction = new UMLMvOperationToNCToNN(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());
        action = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
    }

    @Test
    public void testConstructor() {
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
    public void testToCSV() {
        numberOfCSVField = 4;
        actionName = "Move_Operation_New_Component_New_Node";
        super.testToCSV();
    }

    @Test
    public void testGetTargetType() {
        expectedType = Configurator.OPERATION_LABEL;
        super.testGetTargetType();
    }

    @Test
    public void testExecute() throws URISyntaxException, EolModelLoadingException, EasierException {
        super.testExecute();
    }

    @Test
    public void testGetTargetElement() {
        expectedName = action.getTargetElements();
        super.testGetTargetElement();
    }

    @Test
    public void testClone() {
        super.testClone();
    }

    @Test
    public void testComputeArchitecturalChanges() throws URISyntaxException, EolModelLoadingException, EasierException {
        super.testComputeArchitecturalChanges();
    }
}