package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.domain.UMLEasierModel;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import it.univaq.disim.sealab.metaheuristic.utils.EasierLogger;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;


public class UMLRefactoringActionTest {

    //    UMLRProblem<UMLRSolution> p;
    protected UMLRefactoringAction action, oldAction;
    //    protected UMLRSolution solution;

    protected String generatedCSV;

    protected int numberOfCSVField;
    protected String actionName;
    protected String expectedType;

    protected EasierModel eModel;
    protected String modelpath;

    final static String BASE_PATH = "/easier-uml2lqnCaseStudy/";

    void setUp() throws Exception {
//        eModel = new UMLEasierModel(modelpath);
    }

    void testToCSV() throws EasierException {
        generatedCSV = action.toCSV();
        // Header hardcoded in FileUtils.refactoringDumpToCSV. It has been removed the SOLUTION_ID field
        assertEquals(numberOfCSVField, generatedCSV.split(",").length,
                String.format("Expected length %s \t generated %s", numberOfCSVField, generatedCSV.split(",").length));
        assertEquals(actionName, generatedCSV.split(",")[0],
                String.format("Expected first entry %s \t generated %s", actionName, action.getName()));
    }

    void testEquals() throws EasierException {
        RefactoringAction action2 = action.clone();
        assertEquals(action, action2, "Expected a cloned action to be equal to the original");
    }

    void testExecute() throws URISyntaxException, EolModelLoadingException, EasierException {
        EasierUmlModel model = EOLStandalone.createUmlModel(modelpath);
        assertDoesNotThrow(() -> action.execute(model), String.format("Expected no exceptions when executing the " +
                "action %s on the model %s", action.getName(), modelpath));
    }


    void testGetTargetType() throws EasierException {
        assertEquals(expectedType, action.getTargetType(), String.format("Expected target type %s \t found %s",
                expectedType, action.getTargetType()));
    }

    void testGetTargetElement() throws EasierException {
        Map<String, Set<String>> targetElements = action.getTargetElements();
        assertFalse(targetElements.isEmpty(), "Expected at least one target element type");
        targetElements.forEach((label, names) -> {
            assertFalse(names.isEmpty(), String.format("Expected at least one target element for label %s", label));
            names.forEach(elementName -> assertTrue(
                    eModel.getAvailableElements().getOrDefault(label, Set.of()).contains(elementName),
                    String.format("Expected target element %s (label %s) to be an available element of the model",
                            elementName, label)));
        });
    }

    void testClone() throws EasierException {
        RefactoringAction clonedAction = action.clone();
        assertEquals(action, clonedAction);
    }

    void testComputeArchitecturalChanges() throws URISyntaxException, EolModelLoadingException, EasierException {

        AtomicReference<Double> archChanges = new AtomicReference<>(0d);

        assertDoesNotThrow(() -> archChanges.set(action.getRefactoringCost()), "Expected no exception");
        assertNotEquals(0, archChanges.get(), String.format("The action: %s on: %s should have arcChanges != 0",
                action.getName(), action.getTargetElements().get(action.getTargetType()).iterator().next()));

        EasierLogger.logger_.info(
                String.format("Architectural changes %s of the action %s target %s", archChanges,
                        action.getName(), action.getTargetElements().get(action.getTargetType()).iterator().next()));
    }
}
