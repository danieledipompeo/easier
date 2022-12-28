package it.univaq.disim.sealab.metaheuristic.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.IntStream;

class EasierResourcesLoggerTest {

    EasierResourcesLogger eLogger;

    @BeforeAll
    public static void setUpClass() throws IOException {
        Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
    }


    @AfterEach
    void tearDown() {
    }

    @Test
    void toCSV() {
        IntStream.range(1,5).forEach( i -> {
            eLogger.checkpoint("aTest","step_"+i);
            try {
                Thread.sleep(i * 100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        eLogger.dumpToCSV();
    }
}