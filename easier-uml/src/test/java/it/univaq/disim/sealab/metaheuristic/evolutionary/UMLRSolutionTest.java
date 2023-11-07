package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.actions.UMLRefactoring;
import it.univaq.disim.sealab.metaheuristic.actions.uml.*;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import it.univaq.disim.sealab.metaheuristic.utils.WorkflowUtils;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.junit.Assert;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.UnexpectedException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class UMLRSolutionTest {

    UMLRProblem<RSolution<?>> p;
    private UMLRSolution solution, solution2;

    @BeforeAll
    public static void beforeClass() throws IOException {
        Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
    }

//    @AfterAll
    public static void tearDownClass() throws IOException {
        Files.walk(Configurator.eINSTANCE.getOutputFolder()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @BeforeEach
    public void setUp() throws URISyntaxException {
        String modelPath = getClass().getResource("/models/simplified-cocome/cocome.uml").getFile();
        solution = new UMLRSolution(Paths.get(modelPath), "simplied-cocome__test");
    }

    @AfterEach
    public void tearDown() throws IOException {
        solution = null;

    }

    /*
      The tested refactoring has been built synthetically.
      It has 2 feasible refactoring actions
     */
    @Test
    public void testIsFeasibleShouldReturnTrue() throws EasierException {
        Refactoring refactoring = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = refactoring.getEasierModel();
        RefactoringAction action1 = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
        RefactoringAction action2 = new UMLMvOperationToNCToNN(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());
        refactoring.getActions().add(action1);
        refactoring.getActions().add(action2);
        solution.setVariable(0, refactoring);
        assertTrue(solution.isFeasible(), "It is expected a feasible refactoring.");

    }

    @Test
    public void testIsFeasibleOfCopy() {
        UMLRSolution solutionCopy = new UMLRSolution(solution);
        assertEquals(solution.isFeasible(), solutionCopy.isFeasible(), "Expected the same result of isFeasibleNew of the solution and its copy");
    }

    @Test
    public void testIsFeasibleShouldFail() throws EasierException {
        EasierModel eModel = solution.getVariable(0).getEasierModel();
        RefactoringAction failedAction = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
        failedAction.getTargetElements().get(Configurator.NODE_LABEL).clear();
        failedAction.getTargetElements().get(Configurator.NODE_LABEL).add("FailedNode");
        solution.getVariable(0).getActions().set(0, failedAction);
        assertFalse(solution.isFeasible(), "Expected a unfeasible solution.");
    }

    @Test
    public void testSetRefactoring() throws EasierException {
        Map<String, Set<String>> expectedCreatedElements = new HashMap<>();
        expectedCreatedElements.put(Configurator.NODE_LABEL, new HashSet<>());
        expectedCreatedElements.put(Configurator.COMPONENT_LABEL, new HashSet<>());
        expectedCreatedElements.put(Configurator.OPERATION_LABEL, new HashSet<>());

        solution.init();
        Refactoring ref = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = ref.getEasierModel();
        RefactoringAction clone = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
        RefactoringAction mvopncnn = new UMLMvOperationToNCToNN(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());
        RefactoringAction clone1 = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        RefactoringAction mvcpnn = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
        ref.getActions().addAll(List.of(clone, mvopncnn, clone1, mvcpnn));
        solution.setRefactoring(ref);

        for (RefactoringAction refactoringAction : Arrays.asList(clone, mvopncnn, clone1, mvcpnn)) {
            refactoringAction.getCreatedElements().forEach((k, v) -> expectedCreatedElements.get(k).addAll(v));
        }

        assertEquals(expectedCreatedElements, eModel.getCreatedRefactoringElement(), "Expected the same created element map");

    }

//    @ParameterizedTest
//    @CsvSource({"true,1", "true,2", "true,3", "false,0", "false,2", "false,3"})
//    void testConstructorForXover(boolean left, int point) {
//
//        UMLRSolution solution1 = (UMLRSolution) p.createSolution();
//
//        List<UMLRSolution> children = solution.createChildren(solution1, point);
//        children.forEach(c -> assertTrue(c.isFeasible(), "It is expected a feasible child solution"));
//
//    }

    @Test
    public void createRandomRefactoring() {
        solution.createRandomRefactoring();

        assertFalse(solution.getVariable(0).hasMultipleOccurrence());
    }

    @Test
    public void refactoringToCSV() throws IOException {
        solution.refactoringToCSV();
        LineNumberReader lnr = new LineNumberReader(new FileReader(Configurator.eINSTANCE.getOutputFolder().resolve("refactoring_composition.csv").toString()));
        long readLine = lnr.lines().count();
        //number of refactoring action + the header
        assertEquals(Configurator.eINSTANCE.getLength() + 1, readLine, String.format("Expected %s lines \t found: %s.", Configurator.eINSTANCE.getLength(), readLine));
    }

    @Test
    public void testExecuteRefactoring() throws IOException {
        solution.executeRefactoring();
        LineNumberReader lnr = new LineNumberReader(new FileReader(Configurator.eINSTANCE.getOutputFolder().resolve("refactoring_stats.csv").toString()));
        long readLine = lnr.lines().count();
        //number of refactoring action + the header
        assertEquals(Configurator.eINSTANCE.getLength() + 1, readLine);
    }

    @Test
    public void testEquals() {

        assertEquals(solution, solution);

        solution2 = (UMLRSolution) p.createSolution();
        assertNotEquals(solution, solution2);

        solution2 = (UMLRSolution) solution.copy();
        assertEquals(solution, solution2);
    }

    @Test
    public void copyRefactoringVariable() {
        UMLRSolution cloneSolution = (UMLRSolution) solution.copy();
        assertEquals(solution, cloneSolution);

        cloneSolution.copyRefactoringVariable(solution.getVariable(0));
        assertEquals(solution.getVariable(0), cloneSolution.getVariable(0));
    }


//    @ParameterizedTest
//    @ValueSource(ints = {1, 2, 3, 0, 4})
//    public void createChild(int point) {
//        UMLRSolution solution2 = (UMLRSolution) p.createSolution();
//        UMLRSolution childSolution = solution.createChild(solution2, point);
//
//        childSolution.setVariable(0, new Refactoring()); // clear the old refactoring
//        // clear created element and target element maps
//        childSolution.createdRefactoringElement.clear();
//        childSolution.targetRefactoringElement.clear();
//        childSolution.createChild(solution, solution2, point);
//
//        assertFalse(childSolution.isFeasible());
//
//        for (int i = 0; i < point; i++) {
//            assertEquals(childSolution.getActionAt(i), solution.getActionAt(i));
//        }
//        for (int i = point; i < solution.refactoringLength; i++) {
//            assertEquals(childSolution.getActionAt(i), solution2.getActionAt(i));
//        }
//    }

//    @RepeatedTest(5)
//    void alter(TestInfo testInfo) {
//        int alterPoint = 2;
//        solution.alter(alterPoint);
//
//        assertTrue(solution.isFeasible(), "Expected a feasible solution after the alter operation.");
//    }

    @Test
    public void testIsIndependent() throws EasierException {
        EasierModel eModel = solution.getVariable(0).getEasierModel();
        RefactoringAction a1 = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
        assertTrue(solution.isIndependent(List.of(a1)), "Expected that MvOpNCNN is independent");

        RefactoringAction a2 = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
        assertTrue(solution.isIndependent(List.of(a1, a2)), "Expected that 2 MvOpNCNN are independent");

        RefactoringAction a3 = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
        assertTrue(solution.isIndependent(List.of(a1, a2, a3)), "Expected that 3 MvOpNCNN are independent");

        RefactoringAction a4 = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
        assertTrue(solution.isIndependent(List.of(a1, a2, a3, a4)), "Expected that 4 MvOpNCNN are independent");
    }


    @Test
    void compute_objectives_should_not_fail_when_lqxo_does_not_exist(){
        assertDoesNotThrow(solution::computeObjectives);
    }


    //    @ParameterizedTest
//    @ValueSource(ints = {0, 1, 2, 3})
//    public void doAlter(int point) {
//        RefactoringAction candidate = ((point == 0) ? solution.getActionAt(point + 1) : solution.getActionAt(point - 1));
//        assertFalse(solution.doAlter(point, candidate), String.format("Expected unfeasible solution %s%n", solution.toString()));
//
//        assertTrue(solution.isFeasible());
//    }

}
