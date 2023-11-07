package it.univaq.disim.sealab.metaheuristic.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ObjectInputFilter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguratorTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getNodeCharacteristics() {
        Assertions.assertDoesNotThrow(() -> Configurator.eINSTANCE.getNodeCharacteristics());

        List<NodeType> nodeCharacteristics = Configurator.eINSTANCE.getNodeCharacteristics();

        Assertions.assertNotNull(nodeCharacteristics);
    }
}