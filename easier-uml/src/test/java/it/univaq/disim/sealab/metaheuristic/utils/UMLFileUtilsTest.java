package it.univaq.disim.sealab.metaheuristic.utils;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;



public class UMLFileUtilsTest {

    UMLRSolution sol;
    UMLRProblem<RSolution<?>> p;

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
    public void setup() {
        String modelpath = getClass().getResource("/models/simplified-cocome/cocome.uml").getFile();
        UMLRProblem<RSolution<?>> p = new UMLRProblem<>(Paths.get(modelpath), "simplied-cocome__test");
        sol = (UMLRSolution) p.createSolution();
    }

    @Test
    public void backAnnotationErrorLogToCSVTest() throws IOException {

        String expectedLine = "1,error_message," + sol.getVariable(0).toString();
        FileUtils.backAnnotationErrorLogToCSV(expectedLine);

        String line = "";
        String header = "";
        Path file = Configurator.eINSTANCE.getOutputFolder().resolve("back_annotation_error_log.csv");

        // Check the correct header
        String EXPECTED_HEADER = "solID,message,actions";
        try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {

            header = br.readLine();
            line = br.readLine(); // Read the first line, and it should be the header
        }

        assertNotEquals("", header);
        assertEquals(EXPECTED_HEADER, header);
        assertEquals(3, header.split(",").length);

        assertNotEquals("", line);

        LineNumberReader lnr = new LineNumberReader(new FileReader(
                Configurator.eINSTANCE.getOutputFolder().resolve("back_annotation_error_log.csv").toString()));
        lnr.lines().count();
        assertEquals(5, lnr.getLineNumber());
    }

}
