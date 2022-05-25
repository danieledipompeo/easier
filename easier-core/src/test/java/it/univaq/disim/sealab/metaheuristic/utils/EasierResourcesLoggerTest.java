package it.univaq.disim.sealab.metaheuristic.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class EasierResourcesLoggerTest {

    EasierResourcesLogger eLogger;

    @BeforeAll
    public static void setUpClass() throws IOException {
        Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
    }

    @BeforeEach
    void setUp() {
        eLogger = new EasierResourcesLogger("aTest", "pTest");
    }

    @AfterEach
    void tearDown() {
    }

//    @Test
    void checkpoint() {
    }

    @Test
    void toCSV() {
        IntStream.range(1,5).forEach( i -> {
            eLogger.checkpoint();
            try {
                Thread.sleep(i * 100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        eLogger.toCSV();
    }
}