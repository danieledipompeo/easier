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
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class UMLRefactoringActionTest {

    //    UMLRProblem<UMLRSolution> p;
    protected UMLRefactoringAction action, oldAction;
    //    protected UMLRSolution solution;

    protected String generatedCSV;

    protected int numberOfCSVField;
    protected String actionName;
    protected String expectedType;
    protected Map<String, Set<String>> expectedName;

    protected EasierModel eModel;
    protected String modelpath;

    void setUp() throws Exception {
        int allowedFailures = 100;
        int desired_length = 4;
        int populationSize = 4;

        modelpath = getClass().getResource("/simplified-cocome/cocome.uml").getFile();
        //        p = new UMLRProblem<>(Paths.get(modelpath), "simplied-cocome__test");

        //        solution = p.createSolution();
        //        solution = new UMLRSolution(Paths.get(modelpath), "simplied-cocome__test");

        eModel = new UMLEasierModel(modelpath);
    }

    void testToCSV() {
        generatedCSV = action.toCSV();
        // Header hardcoded in FileUtils.refactoringDumpToCSV. It has been removed the SOLUTION_ID field
        System.out.println("operation,target,to,where,tagged_value,factor");
        System.out.println(generatedCSV);
        assertEquals(numberOfCSVField, generatedCSV.split(",").length,
                String.format("Expected length %s \t generated %s", numberOfCSVField, generatedCSV.split(",").length));
        assertEquals(actionName, generatedCSV.split(",")[0],
                String.format("Expected first entry %s \t generated %s", actionName, action.getName()));
    }

    void testEquals() {
        RefactoringAction action2 = action;
        assertEquals(action, action2);

        action2 = action.clone();
        assertEquals(action, action2);

    }

    void testExecute() throws URISyntaxException, EolModelLoadingException, EasierException {
        EasierUmlModel model = EOLStandalone.createUmlModel(modelpath);
        action.execute(model);
    }


    void testGetTargetType() {
        assertEquals(expectedType, action.getTargetType(), String.format("Expected target type %s \t found %s",
                expectedType, action.getTargetType()));
    }

    void testGetTargetElement() {
        assertEquals(expectedName, action.getTargetElements(), String.format("Expected target name %s \t found %s",
                expectedName, action.getTargetType()));
    }

    void testClone() {
        //        RefactoringAction clonedAction = action.clone(solution);
        RefactoringAction clonedAction = (RefactoringAction) action.clone();
        assertEquals(action, clonedAction);
    }

    void testComputeArchitecturalChanges() throws URISyntaxException, EolModelLoadingException, EasierException {

        Collection<?> modelContents =
                EOLStandalone.createUmlModel(modelpath).allContents();

        double archChanges = action.computeArchitecturalChanges(modelContents);
        EasierLogger.logger_.info(
                String.format("[TEST] architectural changes %s of the action %s target %s", archChanges,
                        action.getName(),
                        action.getTargetElements().get(action.getTargetType()).iterator().next()));

        assertDoesNotThrow(() -> action.computeArchitecturalChanges(modelContents), "Expected no exception");
        assertNotEquals(0, archChanges, "Expected arcChanges != 0");
    }
}
