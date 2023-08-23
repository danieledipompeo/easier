package it.univaq.disim.sealab.metaheuristic.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkflowUtilsTest {

    private Path modelPath;

    @BeforeEach
    public void setUp() throws Exception {
//        modelPath = Paths.get(getClass().getResource("/models/simplified-cocome/cocome.uml").getFile());
        modelPath = Paths.get(getClass().getResource("/models/train-ticket/train-ticket.uml").getFile());
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
            assertNotEquals(String.format("Expected not empty %s file. ", lqnModelPath), br.readLine(), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void invokeSolver() throws Exception {
        new WorkflowUtils().applyTransformation(modelPath);
        Path solverOutcome = modelPath.getParent().resolve("output.lqxo");
        new WorkflowUtils().invokeSolver(modelPath.getParent());
        assertTrue(Files.exists(solverOutcome)); // check whether the file output.lqxo exists
        try (BufferedReader br = new BufferedReader(new FileReader(solverOutcome.toFile()))) {
            // check whether the file output.lqxo is not empty
            assertNotEquals(String.format("Expected not empty %s file. ", solverOutcome), br.readLine(), null);
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
    public void countingPAs() {
        int pas = WorkflowUtils.countPerformanceAntipattern(modelPath, 0);

        assertEquals(1d, pas, 1, String.format("Expected 1 PAs \t found: %s.", pas));
    }

    @Test
    public void evaluatePerformance() throws EasierException {
        modelPath = Paths.get(getClass().getResource("/models/simplified-cocome/cocome.uml").getFile());
        double perfQ = WorkflowUtils.perfQ(modelPath, modelPath);
        assertEquals(0d, perfQ, String.format("Expected perfQ 0 \t computed: %s.", perfQ));
    }
}