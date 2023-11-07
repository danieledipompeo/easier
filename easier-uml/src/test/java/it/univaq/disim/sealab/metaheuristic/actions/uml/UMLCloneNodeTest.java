package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.domain.UMLEasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class UMLCloneNodeTest extends UMLRefactoringActionTest {


    @BeforeEach
    void setUp() throws Exception {
        super.setUp();

//        oldAction = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
//        action = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
    }

    @Test
    void testConstructor() throws EasierException {
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());

        String targetNode = action.getTargetElements().get(Configurator.NODE_LABEL).iterator().next();
        assertFalse(eModel.getAvailableElements().values().stream().noneMatch(set -> set.contains(targetNode)), String.format("Expected target node %s belongs to the availableElements.", targetNode));

        String createdNode = action.getCreatedElements().get(Configurator.NODE_LABEL).iterator().next();
        assertTrue(eModel.getAvailableElements().values().stream().noneMatch(set -> set.contains(createdNode)), String.format("Expected created node %s does not belong to the availableElements.", createdNode));
    }

    @Test
    void testToCSV() throws EasierException {
        numberOfCSVField = 3;
        actionName = "UMLCloneNode";
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testToCSV();
    }

    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    void testExecute(String mPath) throws URISyntaxException, EolModelLoadingException, EasierException {
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLResourceScaling(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        assertDoesNotThrow(super::testExecute);
    }

    @Test
    void testGetTargetType() throws EasierException {
        expectedType = Configurator.NODE_LABEL;
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testGetTargetType();
    }

    @Test
    void testEquals() throws EasierException {
       String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
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
    void testGetTargetElement() throws EasierException {
       String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        expectedName = action.getTargetElements();
        super.testGetTargetElement();
    }

    @Test
    void testClone() throws EasierException {
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testClone();
    }

    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    void testComputeArchitecturalChanges(String mPath) throws URISyntaxException, EolModelLoadingException,
            EasierException {
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testComputeArchitecturalChanges();
    }
}