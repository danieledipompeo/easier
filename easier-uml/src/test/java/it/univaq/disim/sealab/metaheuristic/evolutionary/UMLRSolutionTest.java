package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.actions.UMLRefactoring;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLCloneNode;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvComponentToNN;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvOperationToNCToNN;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
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

    @AfterAll
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

        solution.init();
        Refactoring ref = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = ref.getEasierModel();
        RefactoringAction clone = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements());
        RefactoringAction mvopncnn = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements());
        RefactoringAction clone1 = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements());
        RefactoringAction mvcpnn = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements());
        ref.getActions().addAll(List.of(clone, mvopncnn, clone1, mvcpnn));
        solution.setRefactoring(ref);

        for (RefactoringAction refactoringAction : Arrays.asList(clone, mvopncnn, clone1, mvcpnn)) {
            refactoringAction.getCreatedElements().forEach((k, v) -> expectedCreatedElements.get(k).addAll(v));
        }

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
    public void evaluatePerformance() {
        solution.executeRefactoring();

        solution.evaluatePerformance();

        System.out.printf("target: %s \t initial: %s \t perfQ: %s\n", solution.getModelPath(),
                Configurator.eINSTANCE.getInitialModelPath(),
                solution.getPerfQ());
        assertNotEquals(Double.NaN, solution.getPerfQ(), "Perfq should not be NaN");
        assertNotEquals(0.0, solution.getPerfQ(), "Expected a perfq not equal to 0.0.");
    }

    @Test
    public void computeReliability() {
        solution.computeReliability();
        System.out.printf("Reliability \t %s\r",solution.getReliability());
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
