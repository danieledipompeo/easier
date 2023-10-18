package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
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
    void setUp() throws Exception {
        super.setUp();

        oldAction = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        action = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());


    }

    @Test
    void testConstructor() {
        String targetNode = action.getTargetElements().get(Configurator.NODE_LABEL).iterator().next();
        assertFalse(eModel.getAvailableElements().values().stream().noneMatch(set -> set.contains(targetNode)), String.format("Expected target node %s belongs to the availableElements.", targetNode));

        String createdNode = action.getCreatedElements().get(Configurator.NODE_LABEL).iterator().next();
        assertTrue(eModel.getAvailableElements().values().stream().noneMatch(set -> set.contains(createdNode)), String.format("Expected created node %s does not belong to the availableElements.", createdNode));
    }

    @Test
    void testToCSV() {
//        String generatedCSV = action.toCSV();
//        System.out.println(generatedCSV);
        numberOfCSVField = 3;
        actionName = "UMLCloneNode";
        super.testToCSV();
    }

    @Test
    void testExecute() throws URISyntaxException, EolModelLoadingException, EasierException {
        super.testExecute();
    }

    @Test
    void testGetTargetType() {
        expectedType = Configurator.NODE_LABEL;
        super.testGetTargetType();
    }

    @Test
    void testEquals() {
        super.testEquals();
    }

    @Test
    @Disabled
    void testMapEquals(){
        Map<String, Set<String>> map1 = new HashMap<>();
        Map<String, Set<String>> map2 = new HashMap<>();

        map1.put(Configurator.NODE_LABEL, Set.of("cloned_node"));
        map2.put(Configurator.NODE_LABEL, Set.of("clned_node"));
        map2.put(Configurator.COMPONENT_LABEL, Set.of("test_component"));

        assertTrue(map2.equals(map1));

    }


    @Test
    void testGetTargetElement() {
        expectedName = action.getTargetElements();
        super.testGetTargetElement();
    }

    @Test
    void testClone() {
        super.testClone();
    }

    @Test
    void testComputeArchitecturalChanges() throws URISyntaxException, EolModelLoadingException, EasierException {
        super.testComputeArchitecturalChanges();
    }
}