package it.univaq.disim.sealab.metaheuristic.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class UMLEasierModelTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void initMap() {
        String mPath = getClass().getResource("/easier-uml2lqnCaseStudy/cocome/simplified-cocome/cocome.uml").getPath();
        UMLEasierModel model = new UMLEasierModel(mPath);


        assertNotNull(model);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Path out = Paths.get("/", "tmp", "cocome_initial_elements.json");
        try {
            // ensure target directory exists
            if (out.getParent() != null) {
                Files.createDirectories(out.getParent());
            }
            // write pretty JSON directly to file (uses UTF-8)
            try (var writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
                mapper.writeValue(writer, model.getInitialElements());
            }
            System.out.println("Saved JSON to " + out.toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to serialize and save model to JSON: " + e.getMessage());
        }
    }
}