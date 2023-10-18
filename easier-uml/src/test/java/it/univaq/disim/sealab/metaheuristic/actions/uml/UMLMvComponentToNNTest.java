package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class UMLMvComponentToNNTest extends UMLRefactoringActionTest {

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        oldAction = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
        action = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
    }

    @Test
    public void testConstructor() {
        String targetComponent = action.getTargetElements().get(Configurator.COMPONENT_LABEL).iterator().next();
        String createdElement = action.getCreatedElements().get(Configurator.NODE_LABEL).iterator().next();
        String availableElements = eModel.getAvailableElements().get(Configurator.NODE_LABEL).toString();
        System.out.printf("Expected %s \t found %s", availableElements, createdElement);
        assertTrue(eModel.getAvailableElements().values().stream().noneMatch(set -> set.contains(createdElement)), "Expected created node not in the available elements");
    }

    @Test
    public void testToCSV() {
        numberOfCSVField = 3;
        actionName = "Move_Component_New_Node";
        super.testToCSV();

    }

    @Test
    public void testExecute() throws URISyntaxException, EolModelLoadingException, EasierException {
        super.testExecute();
    }

    @Test
    public void testGetTargetType() {
        expectedType = Configurator.COMPONENT_LABEL;
        super.testGetTargetType();
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