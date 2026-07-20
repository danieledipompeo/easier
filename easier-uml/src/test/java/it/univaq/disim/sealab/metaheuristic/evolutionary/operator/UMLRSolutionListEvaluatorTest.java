package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UMLRSolutionListEvaluatorTest<S extends UMLRSolution> {

    S sol;

    UMLRProblem<S> problem;

    SolutionListEvaluator<S> solutionListEvaluator;

    @BeforeEach
    void setUp() throws IOException {

        problem = new UMLRProblem<>(Paths.get(getClass().getResource("/easier-uml2lqnCaseStudy/train-ticket/train-ticket.uml").getFile()),
                "simplied-cocome__test");
        sol = problem.createSolution();

        solutionListEvaluator = new RSolutionListEvaluator<>();

        Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(Configurator.eINSTANCE.getOutputFolder())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void evaluate() {
        markForSurrogate(sol, true);
        List<S> solutions = new ArrayList<>();
        IntStream.range(0, 10).forEach(i -> solutions.add(sol));

        solutionListEvaluator.evaluate(solutions, problem);

//        solutionListEvaluator.evaluate(new ArrayList<>() {{
//            add(sol);
//        }}, problem);

        assertTrue(Arrays.stream(sol.getObjectives()).allMatch(o -> o != 0));
//        assertNotEquals(0, sol.getPAs(), "Expected PAs != 0");
//        assertNotEquals(0, sol.getReliability(), "Expected reliability != 0");
//        assertNotEquals(0, sol.getArchitecturalChanges(), "Expected architectural changes != 0");
//        assertNotEquals(0, sol.getPerfQ(), "Expected perfq != 0");

    }

    @Test
    void testEvaluate() {
        List<S> solutions = new ArrayList<>();
        IntStream.range(0, 3).forEach(i -> solutions.add((S) problem.createSolution()));

        List<S> evaluated = solutionListEvaluator.evaluate(solutions, problem);

        assertEquals(solutions.size(), evaluated.size(),
                "Expected the evaluated population to have the same size as the input population");
        evaluated.forEach(s -> assertTrue(Arrays.stream(s.getObjectives()).allMatch(o -> o != 0),
                "Expected all objectives to be computed for solution " + s.getName()));
    }

    private static void markForSurrogate(Object solution, boolean value) {
        try {
            // Newer core versions expose an explicit setter.
            Method setter = solution.getClass().getMethod("setMarkedForSurrogate", boolean.class);
            setter.invoke(solution, value);
            return;
        } catch (Exception ignored) {
            // Fallback for classpaths where only the backing field is available.
        }

        try {
            Class<?> current = solution.getClass();
            while (current != null) {
                try {
                    Field f = current.getDeclaredField("markedForSurrogate");
                    f.setAccessible(true);
                    f.setBoolean(solution, value);
                    return;
                } catch (NoSuchFieldException e) {
                    current = current.getSuperclass();
                }
            }
            throw new RuntimeException("Could not set surrogate flag on solution");
        } catch (Exception e) {
            throw new RuntimeException("Could not set surrogate flag on solution", e);
        }
    }
}