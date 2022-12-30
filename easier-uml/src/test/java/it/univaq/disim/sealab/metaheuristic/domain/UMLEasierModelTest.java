package it.univaq.disim.sealab.metaheuristic.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UMLEasierModelTest {

    private EasierModel eModel;

    @BeforeEach
    void setUp() {
        String eModelPath = getClass().getResource("/train-ticket/train-ticket.uml").getFile();
        eModel = new UMLEasierModel(eModelPath);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void store() {
        Map<String, Set<String>> fakeMap = new HashMap<>();
        fakeMap.put("component", Set.of("fakeValue"));
        eModel.store(fakeMap);

        assertTrue(eModel.createdRefactoringElement.values().stream().anyMatch(s -> s.contains("fakeValue")));
    }

    @Test
    void equals_should_return_true_when_comparing_identical_easier_model() {
        assertEquals(eModel, eModel);
    }

    @Test
    void equals_should_return_false_when_comparing_two_easier_models(){
        String eOtherModelPath = getClass().getResource("/simplified-cocome/cocome.uml").getFile();
        EasierModel otherModel = new UMLEasierModel(eOtherModelPath);

        assertNotEquals(eModel, otherModel);
    }

    @Test
    void copy_constructor_should_create_a_carbon_copy(){
        EasierModel otherModel = new UMLEasierModel(eModel);

        assertEquals(eModel, otherModel);
    }
}