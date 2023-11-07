package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class UMLRProblemTest {

    UMLRProblem<UMLRSolution> problem;

    @BeforeAll
    static void setUpBeforeClass() throws IOException {
         Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
    }

    @AfterAll
    static void tearDownAfterClass() throws IOException {
//        Files.walk(Configurator.eINSTANCE.getOutputFolder()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @BeforeEach
    void setUp() {
        problem =
                new UMLRProblem<>(Paths.get(getClass().getResource("/simplified-cocome/cocome.uml").getFile())
                        , "simplied-cocome__test");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void createSolution() {
        UMLRSolution sol = problem.createSolution();

        assertNotNull(sol, "Expected non null solution");
        assertTrue(sol.isFeasible(), "Expected a feasible solution");
    }

    @Test
    void evaluate() {
        UMLRSolution sol = problem.createSolution();
        problem.evaluate(sol);

        assertTrue(Arrays.stream(sol.getObjectives()).allMatch(o -> o != 0));
    }
}