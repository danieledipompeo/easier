package it.univaq.disim.sealab.metaheuristic.utils;

import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.actions.uml.*;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WorkflowUtilsTest {

    private Path modelPath;

    @BeforeEach
    public void setUp() throws Exception {
        modelPath = Path.of(getClass().getResource("/simplified-cocome/cocome.uml").getPath());
    }

    @AfterEach
    public void tearDown() throws Exception {
//        Files.deleteIfExists(modelPath.getParent().resolve("output.xml"));
//        Files.deleteIfExists(modelPath.getParent().resolve("output.xml.bak"));
//        Files.deleteIfExists(modelPath.getParent().resolve("output.lqxo"));
//        Files.deleteIfExists(modelPath.getParent().resolve("output.out"));
    }

    @Test
    public void applyTransformation() throws EasierException {
        new WorkflowUtils().applyTransformation(modelPath);
        Path lqnModelPath = modelPath.getParent().resolve("output.xml");
        assertTrue(Files.exists(lqnModelPath));

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(lqnModelPath.toFile()));
            assertNotEquals(br.readLine(), null, String.format("Expected not empty %s file. ", lqnModelPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void apply_transformation_with_remove_actions() throws EasierException {

        UMLRSolution solution = new UMLRSolution(modelPath, "simplied-cocome__test");

        EasierModel eModel = solution.getVariable(0).getEasierModel();

        RefactoringAction deleteNode = new UMLRemoveNode(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());

        RefactoringAction clone = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());

        RefactoringAction removeComponent = new UMLRemoveComponent(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());

        RefactoringAction movopc = new UMLMvOperationToComp(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());

        solution.getVariable(0).getActions().clear();

        List.of(deleteNode, clone, removeComponent, movopc).forEach(solution.getVariable(0)::addRefactoringAction);

        solution.executeRefactoring();

        assertDoesNotThrow(() -> WorkflowUtils.applyTransformation(modelPath), "Refactoring with remove actions " +
                "failed.");
    }

    @Test
    void check_transformation_with_refactoring_removecompoent_moc_moc_moncnn() throws EasierException {
        UMLRSolution solution = new UMLRSolution(modelPath, "simplied-cocome__test");

        EasierModel eModel = solution.getVariable(0).getEasierModel();

        RefactoringAction removeComponent = new UMLRemoveComponent(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());

        RefactoringAction moc = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());

        RefactoringAction moc1 = new UMLMvOperationToComp(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());

        RefactoringAction moncnn =
                new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                        eModel.getAllContents());

        List.of(removeComponent, moc, moc1, moncnn).forEach(solution.getVariable(0)::addRefactoringAction);
        solution.executeRefactoring();
        assertDoesNotThrow(() -> WorkflowUtils.applyTransformation(modelPath), "Refactoring with remove actions " +
                "failed.");
    }

    @Test
    public void invokeSolver() throws Exception {
        WorkflowUtils.applyTransformation(modelPath);
        Path solverOutcome = modelPath.getParent().resolve("output.lqxo");
        WorkflowUtils.invokeSolver(modelPath.getParent());
        assertTrue(Files.exists(solverOutcome)); // check whether the file output.lqxo exists
        try (BufferedReader br = new BufferedReader(new FileReader(solverOutcome.toFile()))) {
            // check whether the file output.lqxo is not empty
            assertNotEquals(br.readLine(), null, String.format("Expected not empty %s file. ", solverOutcome));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Test
    public void backAnnotation() throws Exception {
        WorkflowUtils.applyTransformation(modelPath);
        WorkflowUtils.invokeSolver(modelPath.getParent());
        WorkflowUtils.backAnnotation(modelPath);
    }

    @Test
    public void countingPAs() throws EasierException {
        int pas = WorkflowUtils.countPerformanceAntipattern(modelPath, 0);

        assertEquals(1d, pas, 1, String.format("Expected 1 PAs \t found: %s.", pas));
    }

    @Test
    public void evaluatePerformance() throws EasierException {
        modelPath = Paths.get(getClass().getResource("/models/simplified-cocome/cocome.uml").getFile());
        double perfQ = WorkflowUtils.perfQ(modelPath, modelPath);
        assertEquals(0d, perfQ, String.format("Expected perfQ 0 \t computed: %s.", perfQ));
    }

    @Test
    void evaluate_systemResponseTime() throws EasierException {
        modelPath = Path.of(getClass().getResource("/simplified-cocome/cocome.uml").getPath());
        double sysRespT = WorkflowUtils.systemResponseTime(modelPath);

        assertDoesNotThrow(() -> WorkflowUtils.systemResponseTime(modelPath));

        assertNotEquals(Double.MIN_VALUE, sysRespT, "Expected a valid system response time.");
    }

    @Test
    void computeArchitecturalChanges() throws EasierException {
        UMLRSolution solution = new UMLRSolution(modelPath, "simplied-cocome__test");
        solution.createRandomRefactoring();
        solution.executeRefactoring();

        assertTrue(WorkflowUtils.refactoringCost(solution) > Configurator.eINSTANCE.getInitialChanges(), "Expected an" +
                "refactoring cost >= the initial one");

    }

    @Test
    void architectural_changes_with_removing_actions() throws EasierException {
        UMLRSolution solution = new UMLRSolution(modelPath, "simplied-cocome__test");
        solution.createRandomRefactoring();

        EasierModel eModel = solution.getVariable(0).getEasierModel();

        RefactoringAction deleteNode = new UMLRemoveNode(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());

        RefactoringAction clone = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        RefactoringAction mvopncnn =
                new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        RefactoringAction movopc = new UMLMvOperationToComp(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());

        List.of(deleteNode, clone, mvopncnn, movopc).forEach(solution.getVariable(0)::addRefactoringAction);

        solution.executeRefactoring();

        assertDoesNotThrow(() -> WorkflowUtils.refactoringCost(solution), "Refactoring Cost has thrown an exception");



    }
}