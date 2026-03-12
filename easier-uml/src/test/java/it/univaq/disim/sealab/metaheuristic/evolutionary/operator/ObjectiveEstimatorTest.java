package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLCloneNode;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvOperationToComp;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvOperationToNCToNN;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLRemoveNode;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import it.univaq.disim.sealab.metaheuristic.utils.EasierObjectiveNotFoundException;
import it.univaq.disim.sealab.metaheuristic.utils.LQNException;
import it.univaq.disim.sealab.metaheuristic.utils.WorkflowUtils;

class ObjectiveEstimatorTest {

    static String BASE_PATH = "/easier-uml2lqnCaseStudy/";
    private Path modelPath;

    @BeforeAll
    public static void setUp() {
        if (!Files.exists(Configurator.eINSTANCE.getOutputFolder()))
            Configurator.eINSTANCE.getOutputFolder().toFile().mkdirs();
    }

    @AfterAll
    public static void cleanUp() throws Exception {
        if (Files.exists(Configurator.eINSTANCE.getOutputFolder()))
            Files.walk(Configurator.eINSTANCE.getOutputFolder()).sorted(Comparator.reverseOrder())
                    .map(Path::toFile).forEach(File::delete);
    }

    @ParameterizedTest
    @CsvSource({"cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml", "eshopper, /eshopper/eshopper.uml",})
    public void countingPAs(String model, String mPath) throws EasierException {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        assertDoesNotThrow(() -> ObjectiveEstimator.countPerformanceAntipattern(modelPath, 0));
    }

    @ParameterizedTest
    @CsvSource({"cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml", "eshopper, /eshopper/eshopper.uml",})
    public void evaluatePerformance(String model, String mPath) throws EasierException {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        double perfQ = ObjectiveEstimator.perfQ(modelPath, modelPath);
        assertEquals(0d, perfQ, String.format("Expected perfQ 0 \t computed: %s.", perfQ));
    }

    @ParameterizedTest
    @CsvSource({"cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml", "eshopper, /eshopper/eshopper.uml",})
    void evaluate_systemResponseTime(String model, String mPath) throws EasierException {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        AtomicReference<Double> sysRespT = new AtomicReference<>((double) 0); // =
        // ObjectiveEstimator.systemResponseTime(modelPath);

        assertDoesNotThrow(() -> sysRespT.set(ObjectiveEstimator.systemResponseTime(modelPath)));

        assertNotEquals(Double.MIN_VALUE, sysRespT.get(), "Expected a valid system response time.");
    }

    @ParameterizedTest
    @CsvSource({"cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml", "eshopper, /eshopper/eshopper.uml",})
    void computeArchitecturalChanges(String model, String mPath) throws EasierException {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        UMLRSolution solution = new UMLRSolution(modelPath, model + "__test");
        solution.createRandomRefactoring();
        solution.executeRefactoring();

        assertTrue(
                ObjectiveEstimator.refactoringCost(solution) > Configurator.eINSTANCE
                        .getInitialChanges(),
                "Expected an" + "refactoring cost >= the initial one");

    }

    @ParameterizedTest
    @CsvSource({"cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml", "eshopper, /eshopper/eshopper.uml",})
    void architectural_changes_with_removing_actions(String model, String mPath)
            throws EasierException {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());
        UMLRSolution solution = new UMLRSolution(modelPath, model + "__test");
        solution.createRandomRefactoring();

        EasierModel eModel = solution.getVariable(0).getEasierModel();

        RefactoringAction deleteNode = new UMLRemoveNode(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());

        RefactoringAction clone = new UMLCloneNode(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());
        RefactoringAction mvopncnn = new UMLMvOperationToNCToNN(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());
        RefactoringAction movopc = new UMLMvOperationToComp(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());

        List.of(deleteNode, clone, mvopncnn, movopc)
                .forEach(solution.getVariable(0)::addRefactoringAction);

        solution.executeRefactoring();

        assertDoesNotThrow(() -> ObjectiveEstimator.refactoringCost(solution),
                "Refactoring Cost has thrown an exception");
    }

    @ParameterizedTest
    @CsvSource({"cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml", "eshopper, /eshopper/eshopper.uml",})
    void computeEconomicCost(String model, String mPath) {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());
        assertTrue(ObjectiveEstimator.economicCost(modelPath) != 0, "Expected a cost != 0");
        assertTrue(ObjectiveEstimator.economicCost(modelPath) != Double.MAX_VALUE,
                "Expected a cost not " + "equal to Double.MAX_VALUE");
    }

    @ParameterizedTest
    @CsvSource({"cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml", "eshopper, /eshopper/eshopper.uml",})
    void computeReliability(String model, String mPath) {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());
        assertDoesNotThrow(() -> ObjectiveEstimator.reliability(modelPath));
    }

    @ParameterizedTest
    @CsvSource({"cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml", "eshopper, /eshopper/eshopper.uml",})
    void computePower(String model, String mPath) {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        assertDoesNotThrow(() -> ObjectiveEstimator.powerEstimator(modelPath));
    }

    // TODO: using mockito to mock solution model path (both uml and lqn)
    @Test
    void setObjective() throws EasierException, LQNException, EasierObjectiveNotFoundException {
        String mPath = "/cocome/simplified-cocome/cocome.uml";
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        List<String> scenarios = List.of("ProcessSale_job_class", "ShowDeliveryReports_job_class",
                "ReceivedOrderedProducts_job_class");
        Configurator.eINSTANCE.updateObjectiveList(scenarios);

        RSolution<?> solution = new UMLRSolution(modelPath, "test");

        ObjectiveEstimator.computeObjectives(solution);

        ObjectiveEstimator.setConsideredObjectives(solution);
    }

    @Test
    void testSurrogateEvaluationHttpError() throws Exception {

        // Create test solution
        String model = "/cocome/simplified-cocome/cocome.uml";
        modelPath = Path.of(getClass().getResource(BASE_PATH + model).getPath());
        UMLRSolution solution = new UMLRSolution(modelPath, model + "__test");
        solution.createRandomRefactoring();
        UMLRSolution solution1 = new UMLRSolution(modelPath, model + "__test");
        solution1.createRandomRefactoring();
        solution1.setMarkedForSurrogate(true);

        new WorkflowUtils().executeFlow(solution);
        new WorkflowUtils().executeFlow(solution1);
        ObjectiveEstimator.computeObjectives(solution);
        ObjectiveEstimator.initObjectives(solution1);

        List<RSolution<?>> solutions = List.of(solution, solution1);

        // Run the method
        ObjectiveEstimator.surrogateEvaluation(solutions, 0, modelPath.getFileName().toString().replace(".uml", ""));

        // Verify solution was not updated due to error
        assertEquals(-0.5, solution.getMapOfObjectives().get("perfq"));
    }

}
