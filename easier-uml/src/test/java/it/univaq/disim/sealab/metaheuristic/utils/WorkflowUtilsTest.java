package it.univaq.disim.sealab.metaheuristic.utils;

import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.actions.uml.*;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WorkflowUtilsTest {

    final static String BASE_PATH = "/easier-uml2lqnCaseStudy/";
    private Path modelPath;

    @BeforeEach
    public void setUp() throws Exception { }

    @AfterEach
    public void tearDown() throws Exception {
        Files.walk(Configurator.eINSTANCE.getOutputFolder())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @ParameterizedTest
    @CsvSource({
            "cocome, /simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml",
            "eshopper, /eshopper/eshopper.uml",
    })
    public void applyTransformation(String model, String mPath) throws EasierException {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());
        WorkflowUtils.applyTransformation(modelPath);
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

    @ParameterizedTest
    @CsvSource({
            "cocome, /simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml",
            "eshopper, /eshopper/eshopper.uml",
    })
    void apply_transformation_with_remove_actions(String model, String mPath) throws EasierException {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        UMLRSolution solution = new UMLRSolution(modelPath, model + "__test");

        EasierModel eModel = solution.getVariable(0).getEasierModel();

        RefactoringAction deleteNode = new UMLRemoveNode(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());

        RefactoringAction clone =
                new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());

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

    @ParameterizedTest
    @CsvSource({
            "cocome, /simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml",
            "eshopper, /eshopper/eshopper.uml",
    })
    void check_transformation_with_refactoring_removecompoent_moc_moc_moncnn(String model, String mPath)
            throws EasierException {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        UMLRSolution solution = new UMLRSolution(modelPath, model + "__test");

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

    @ParameterizedTest
    @CsvSource({
            "cocome, cocome/simplified-cocome/cocome.uml",
            "train-ticket, train-ticket/train-ticket.uml",
            "eshopper, eshopper/eshopper.uml",
    })
    public void invokeSolver(String model, String mPath) {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        // assert that the transformation as well as the solver invocation do not throw any exception
        assertDoesNotThrow(() -> WorkflowUtils.applyTransformation(modelPath),
                "Transformation failed.");
        assertDoesNotThrow(() -> WorkflowUtils.invokeSolver(modelPath.getParent()), "Solver invocation failed.");

        // check whether the file output .lqxo exists
        Path solverOutcome = modelPath.getParent().resolve("output.lqxo");
        assertTrue(Files.exists(solverOutcome), "It's expected that the file: " + solverOutcome + "exists."); //

        try (BufferedReader br = new BufferedReader(new FileReader(solverOutcome.toFile()))) {
            // check whether the file output.lqxo is not empty
            assertNotEquals(br.readLine(), null, String.format("Expected not empty %s file. ", solverOutcome));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "cocome, cocome/simplified-cocome/cocome.uml",
            "train-ticket, train-ticket/train-ticket.uml",
            "eshopper, eshopper/eshopper.uml",
    })
    public void backAnnotation(String model, String mPath) throws Exception {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        WorkflowUtils.applyTransformation(modelPath);
        WorkflowUtils.invokeSolver(modelPath.getParent());
        WorkflowUtils.backAnnotation(modelPath);
    }

}