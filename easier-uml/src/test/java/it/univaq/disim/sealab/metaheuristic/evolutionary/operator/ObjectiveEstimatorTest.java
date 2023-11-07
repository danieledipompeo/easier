package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLCloneNode;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvOperationToComp;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvOperationToNCToNN;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLRemoveNode;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

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
            Files.walk(Configurator.eINSTANCE.getOutputFolder())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
    }

    @ParameterizedTest
    @CsvSource({
            "cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml",
            "eshopper, /eshopper/eshopper.uml",
    })
    public void countingPAs(String model, String mPath) throws EasierException {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        assertDoesNotThrow(() -> ObjectiveEstimator.countPerformanceAntipattern(modelPath, 0));
    }

    @ParameterizedTest
    @CsvSource({
            "cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml",
            "eshopper, /eshopper/eshopper.uml",
    })
    public void evaluatePerformance(String model, String mPath) throws EasierException {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        double perfQ = ObjectiveEstimator.perfQ(modelPath, modelPath);
        assertEquals(0d, perfQ, String.format("Expected perfQ 0 \t computed: %s.", perfQ));
    }

    @ParameterizedTest
    @CsvSource({
            "cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml",
            "eshopper, /eshopper/eshopper.uml",
    })
    void evaluate_systemResponseTime(String model, String mPath) throws EasierException {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        AtomicReference<Double> sysRespT =
                new AtomicReference<>((double) 0); //= ObjectiveEstimator.systemResponseTime(modelPath);

        assertDoesNotThrow(() -> sysRespT.set(ObjectiveEstimator.systemResponseTime(modelPath)));

        assertNotEquals(Double.MIN_VALUE, sysRespT.get(), "Expected a valid system response time.");
    }

    @ParameterizedTest
    @CsvSource({
            "cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml",
            "eshopper, /eshopper/eshopper.uml",
    })
    void computeArchitecturalChanges(String model, String mPath) throws EasierException {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        UMLRSolution solution = new UMLRSolution(modelPath, model + "__test");
        solution.createRandomRefactoring();
        solution.executeRefactoring();

        assertTrue(ObjectiveEstimator.refactoringCost(solution) > Configurator.eINSTANCE.getInitialChanges(),
                "Expected an" +
                        "refactoring cost >= the initial one");

    }

    @ParameterizedTest
    @CsvSource({
            "cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml",
            "eshopper, /eshopper/eshopper.uml",
    })
    void architectural_changes_with_removing_actions(String model, String mPath) throws EasierException {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());
        UMLRSolution solution = new UMLRSolution(modelPath, model + "__test");
        solution.createRandomRefactoring();

        EasierModel eModel = solution.getVariable(0).getEasierModel();

        RefactoringAction deleteNode = new UMLRemoveNode(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());

        RefactoringAction clone =
                new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        RefactoringAction mvopncnn =
                new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                        eModel.getAllContents());
        RefactoringAction movopc = new UMLMvOperationToComp(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());

        List.of(deleteNode, clone, mvopncnn, movopc).forEach(solution.getVariable(0)::addRefactoringAction);

        solution.executeRefactoring();

        assertDoesNotThrow(() -> ObjectiveEstimator.refactoringCost(solution),
                "Refactoring Cost has thrown an exception");
    }

    @ParameterizedTest
    @CsvSource({
            "cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml",
            "eshopper, /eshopper/eshopper.uml",
    })
    void computeEconomicCost(String model, String mPath) {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());
        assertTrue(ObjectiveEstimator.economicCost(modelPath) != 0, "Expected a cost != 0");
        assertTrue(ObjectiveEstimator.economicCost(modelPath) != Double.MAX_VALUE, "Expected a cost not " +
                "equal to Double.MAX_VALUE");
    }

    @ParameterizedTest
    @CsvSource({
            "cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml",
            "eshopper, /eshopper/eshopper.uml",
    })
    void computeReliability(String model, String mPath) {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());
        assertDoesNotThrow(() -> ObjectiveEstimator.reliability(modelPath));
    }

    @ParameterizedTest
    @CsvSource({
            "cocome, /cocome/simplified-cocome/cocome.uml",
            "train-ticket, /train-ticket/train-ticket.uml",
            "eshopper, /eshopper/eshopper.uml",
    })
    void computePower(String model, String mPath) {
        modelPath = Path.of(getClass().getResource(BASE_PATH + mPath).getPath());

        assertDoesNotThrow(() -> ObjectiveEstimator.powerEstimator(modelPath));
    }
}