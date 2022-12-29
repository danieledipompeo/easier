package it.univaq.disim.sealab.metaheuristic.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class WorkflowUtilsTest {

    private Path modelPath;

    @BeforeEach
    public void setUp() throws Exception {
        modelPath = Paths.get(getClass().getResource("/train-ticket/train-ticket.uml").getFile());
    }

    @AfterEach
    public void tearDown() throws Exception {
        FileUtils.removeOutputFolder();
    }

    @Test
    public void applyTransformation() {
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
        assertTrue(Files.exists(solverOutcome)); // check whether exists the file output.lqxo
        try (BufferedReader br = new BufferedReader(new FileReader(solverOutcome.toFile()))) {
            // check whether the file output.lqxo is not empty
            assertNotEquals(String.format("Expected not empty %s file. ", solverOutcome), br.readLine(), null);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Test
    public void backAnnotation() throws Exception {
        new WorkflowUtils().applyTransformation(modelPath);
        new WorkflowUtils().invokeSolver(modelPath.getParent());
        new WorkflowUtils().backAnnotation(modelPath);
    }
}