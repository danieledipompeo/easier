package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.actions.UMLRefactoring;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLCloneNode;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvComponentToNN;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvOperationToNCToNN;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.junit.jupiter.api.*;

import java.io.IOException;
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
        FileUtils.removeOutputFolder();
        Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
    }

    @AfterAll
    public static void tearDownClass() throws IOException {
        FileUtils.removeOutputFolder();
    }

    @BeforeEach
    public void setUp() throws URISyntaxException {
        String modelPath = getClass().getResource("/simplified-cocome/cocome.uml").getFile();
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
    public void testIsFeasibleShouldReturnTrue() {
        Refactoring refactoring = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = refactoring.getEasierModel();
        RefactoringAction action1 = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements());
        RefactoringAction action2 = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements());
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
    public void testIsFeasibleShouldFail() {
        solution.createRandomRefactoring();
        EasierModel eModel = solution.getVariable(0).getEasierModel();
        RefactoringAction failedAction = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements());
        failedAction.getTargetElements().get(UMLRSolution.SupportedType.NODE.toString()).clear();
        failedAction.getTargetElements().get(UMLRSolution.SupportedType.NODE.toString()).add("FailedNode");
        solution.getVariable(0).getActions().set(0, failedAction);
        assertFalse(solution.isFeasible(), "Expected a unfeasible solution.");
    }

    @Test
    public void testSetRefactoring() {
        Map<String, Set<String>> expectedCreatedElements = new HashMap<>();
        expectedCreatedElements.put(UMLRSolution.SupportedType.NODE.toString(), new HashSet<>());
        expectedCreatedElements.put(UMLRSolution.SupportedType.COMPONENT.toString(), new HashSet<>());
        expectedCreatedElements.put(UMLRSolution.SupportedType.OPERATION.toString(), new HashSet<>());

        solution.createRandomRefactoring();
        Refactoring ref = solution.getVariable(0);

        // get created element by each refactoringaction
        for (RefactoringAction refactoringAction : ref.getActions()) {
            refactoringAction.getCreatedElements().forEach((k, v) -> expectedCreatedElements.get(k).addAll(v));
        }
        // apply the refactoring and then store the created element
        solution.executeRefactoring();

        EasierModel eModel = ref.getEasierModel();

        // check if the created elements by each ref action are the same of the ones stored in easier model
        assertEquals(expectedCreatedElements, eModel.getCreatedRefactoringElement(), "Expected the same created element map");
    }

    @Disabled
    @Test
    public void isLocalOptmimalPointTrueTest() {
        solution2 = (UMLRSolution) p.createSolution();
        solution.setPerfQ(0);
        solution2.setPerfQ(0);

        solution.reliability = 0;
        solution2.reliability = 0;

        solution.numPAs = 0;
        solution2.numPAs = 0;

        assertTrue(solution.isLocalOptmimalPoint(solution2));
    }

    /**
     * PerfQ of solution2 is greater than the perfQ of solution The test should
     * return FALSE
     */
    @Disabled
    @Test
    public void isLocalOptmimalPointPerfQOutOfRangeShouldReturnFalseTest() {
        solution2 = (UMLRSolution) p.createSolution();
        solution.setPerfQ(0);
        solution2.setPerfQ(0);

        solution.reliability = 0;
        solution2.reliability = 0;

        solution.numPAs = 0;
        solution2.numPAs = 4;

        assertFalse(solution.isLocalOptmimalPoint(solution2));
    }

    @Disabled
    @Test
    public void isLocalOptmimalPointSolutionWithinSolution2ShouldReturnTrueTest() {
        solution2 = (UMLRSolution) p.createSolution();
        solution.setPerfQ(-10);
        solution2.setPerfQ(-10);

        solution.reliability = -10;
        solution2.reliability = -10;

        solution.numPAs = 1;
        solution2.numPAs = 0;

        assertTrue(solution.isLocalOptmimalPoint(solution2));
    }

    @Test
    public void countingPAs() {
        solution.countingPAs();
        System.out.printf("PAs \t %s\r",solution.getPAs());
        assertEquals(13d, solution.getPAs(), 1, String.format("Expected 12 PAs \t found: %s.", solution.getPAs()));
    }

    @Test
    public void createRandomRefactoring() {
        solution.createRandomRefactoring();

        assertFalse(solution.getVariable(0).hasMultipleOccurrence());
    }

    @Test
    public void testTryRandomPush() throws UnexpectedException, EolRuntimeException {

        Refactoring ref = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = ref.getEasierModel();
        RefactoringAction clone = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements());
        RefactoringAction mvopncnn = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements());
        RefactoringAction clone1 = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements());
        RefactoringAction mvcpnn = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements());
        ref.getActions().add(clone);//, mvopncnn, clone1, mvcpnn));
        solution.setVariable(0, ref);

        eModel.getTargetRefactoringElement().get(UMLRSolution.SupportedType.NODE.toString()).add(clone.getCreatedElements().get(UMLRSolution.SupportedType.NODE.toString()).iterator().next());

        solution.tryRandomPush();

        assertTrue(eModel.getAvailableElements().values().stream().flatMap(Set::stream).anyMatch(clone.getCreatedElements().get(UMLRSolution.SupportedType.NODE.toString())::contains));
    }

    @Test
    public void empty_refactoring_should_return_perfq_0() {
        solution.evaluatePerformance();
        System.out.printf("target: %s \t initial: %s \t perfQ: %s\n", solution.getModelPath(),
                Configurator.eINSTANCE.getInitialModelPath(),
                solution.getPerfQ());
        assertNotEquals(Double.NaN, solution.getPerfQ(), "Perfq should not be NaN");
        assertEquals(0.0, solution.getPerfQ(), "Expected a perfq equal to 0.0.");
    }

    @Test
    void random_refactoring_should_not_return_perfq_0() {
        // execute the workflow
        solution.createRandomRefactoring();
        solution.executeRefactoring();
        solution.applyTransformation();
        solution.invokeSolver();

        solution.evaluatePerformance();
        System.out.printf("target: %s \t initial: %s \t perfQ: %s\n", solution.getModelPath(),
                Configurator.eINSTANCE.getInitialModelPath(),
                solution.getPerfQ());

        assertNotEquals(0.0, solution.getPerfQ(), "Expected a perfq not equal to 0.0.");
    }


    @Test
    public void computeReliability() {
        solution.computeReliability();
        System.out.printf("Reliability \t %s\r", solution.getReliability());
    }

    @Test
    public void refactoringToCSV_should_create_file_containing_ref_actions() throws IOException {
        solution.createRandomRefactoring();
        solution.refactoringToCSV();

        List<String> csvLines = Files.readAllLines(Configurator.eINSTANCE.getOutputFolder().resolve("refactoring_composition.csv"));

        assertEquals(Configurator.eINSTANCE.getLength() + 1, csvLines.size(), String.format("Expected %s lines \t found: " +
                        "%s.", Configurator.eINSTANCE.getLength(), csvLines.size()));

        String header = "solID,operation,target,to,where";
        assertEquals(header, csvLines.get(0));

        assertEquals(header.split(",").length, csvLines.get(0).split(",").length, "Line of a refactoring action must " +
                "contain number of fields as the header");
    }

    @Test
    public void testExecuteRefactoring() throws IOException {
        solution.createRandomRefactoring();
        solution.executeRefactoring();
        assertTrue(solution.refactored);
    }

    @Test
    public void equals_should_return_true_with_two_identical_solution() {
        assertEquals(solution, solution, "Expected true when comparing two identical solutions");
    }

    @Test
    void equals_should_return_false_with_different_solution() {
        String modelPath = getClass().getResource("/simplified-cocome/cocome.uml").getFile();
        solution2 = new UMLRSolution(Paths.get(modelPath), "simplied-cocome__test");
        solution2.createRandomRefactoring();

        assertNotEquals(solution, solution2, "Expected not equals solutions");
    }

    @Test
    void equals_should_be_true_when_solutions_have_same_parents() {
        String modelPath = getClass().getResource("/simplified-cocome/cocome.uml").getFile();
        Path modelP = Path.of(modelPath);
        UMLRSolution parent1 = new UMLRSolution(modelP, "simplied-cocome__test");
        UMLRSolution parent2 = new UMLRSolution(modelP, "simplied-cocome__test");

        solution.setParents(parent1, parent2);
        solution2 = (UMLRSolution) solution.copy();

        assertEquals(solution, solution2, "Expected equals solutions when both have same parents");

    }


    @Test
    void equals_should_be_false_when_solutions_have_different_parents() {
        String modelPath = getClass().getResource("/simplified-cocome/cocome.uml").getFile();
        Path modelP = Path.of(modelPath);
        solution2 = new UMLRSolution(modelP, "simplied-cocome__test");
        solution2.createRandomRefactoring();

        solution.setParents(new UMLRSolution(modelP, "simplied-cocome__test"),
                new UMLRSolution(modelP, "simplied-cocome__test"));

        assertNotEquals(solution, solution2, "Expected not equals solutions when one has null parents");

        solution2.setParents(new UMLRSolution(modelP, "simplied-cocome__test"),
                new UMLRSolution(modelP, "simplied-cocome__test"));

        assertNotEquals(solution, solution2, "Expected not equals solutions when both have different parents");
    }

    @Test
    void copy_should_produce_equal_solution() {
        solution2 = (UMLRSolution) solution.copy();
        assertEquals(solution, solution2, "The copy method should return an equal solution");
    }

    @Test
    public void testIsIndependent() {
        EasierModel eModel = solution.getVariable(0).getEasierModel();
        RefactoringAction a1 = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements());
        assertTrue(solution.isIndependent(List.of(a1)), "Expected that MvOpNCNN is independent");

        RefactoringAction a2 = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements());
        assertTrue(solution.isIndependent(List.of(a1, a2)), "Expected that 2 MvOpNCNN are independent");

        RefactoringAction a3 = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements());
        assertTrue(solution.isIndependent(List.of(a1, a2, a3)), "Expected that 3 MvOpNCNN are independent");

        RefactoringAction a4 = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements());
        assertTrue(solution.isIndependent(List.of(a1, a2, a3, a4)), "Expected that 4 MvOpNCNN are independent");
    }

    @Test
    void computeArchitecturalChanges() {
        solution.createRandomRefactoring();
        solution.executeRefactoring();

        solution.computeArchitecturalChanges();

        assertTrue(solution.getArchitecturalChanges() > Configurator.eINSTANCE.getInitialChanges(), "Expected an " +
                "architectural changes >= the initial one");

    }

}
