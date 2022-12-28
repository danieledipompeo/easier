package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class UMLCloneNodeTest extends UMLRefactoringActionTest {


    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        oldAction = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements());
        action = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements());


    }

    @Test
    public void testConstructor() {
        String targetNode = action.getTargetElements().get(UMLRSolution.SupportedType.NODE.toString()).iterator().next();
        assertFalse(eModel.getAvailableElements().values().stream().noneMatch(set -> set.contains(targetNode)), String.format("Expected target node %s belongs to the availableElements.", targetNode));

        String createdNode = action.getCreatedElements().get(UMLRSolution.SupportedType.NODE.toString()).iterator().next();
        assertTrue(eModel.getAvailableElements().values().stream().noneMatch(set -> set.contains(createdNode)), String.format("Expected created node %s does not belong to the availableElements.", createdNode));
    }

    @Test
    public void testToCSV() {
        numberOfCSVField = 3;
        actionName = "UMLCloneNode";
        super.testToCSV();
    }

    @Test
    public void testExecute() throws URISyntaxException, EolModelLoadingException, EasierException {
        super.testExecute();
    }

    @Test
    public void testGetTargetType() {
        expectedType = UMLRSolution.SupportedType.NODE.toString();
        super.testGetTargetType();
    }

    @Test
    public void testEquals() {
        super.testEquals();
    }

    @Test
    @Disabled
    public void testMapEquals(){
        Map<String, Set<String>> map1 = new HashMap<>();
        Map<String, Set<String>> map2 = new HashMap<>();

        map1.put(UMLRSolution.SupportedType.NODE.toString(), Set.of("cloned_node"));
        map2.put(UMLRSolution.SupportedType.NODE.toString(), Set.of("clned_node"));
        map2.put(UMLRSolution.SupportedType.COMPONENT.toString(), Set.of("test_component"));

        assertTrue(map2.equals(map1));
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