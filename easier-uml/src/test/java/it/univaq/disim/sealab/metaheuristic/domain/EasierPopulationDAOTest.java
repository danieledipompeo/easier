package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class EasierPopulationDAOTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getSolutions() {
    }

 @Test
    void constructor_With_Different_Solution() {
        String modelPath = getClass().getResource("/models/simplified-cocome/cocome.uml").getFile();
        List<RSolution<?>> solutions = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            UMLRSolution sol = new UMLRSolution(Path.of(modelPath), "problem__test");
            solutions.add(sol);
        }
        EasierPopulationDAO popDao = new EasierPopulationDAO(solutions);

        assertTrue(popDao.getSolutions().size()==10, "Expected population size of 10 elements.");

    }
    @Test
    void constructor_With_A_Repeated_Solution() {
        String modelPath = getClass().getResource("/models/simplified-cocome/cocome.uml").getFile();
        List<RSolution<?>> solutions = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            UMLRSolution sol = new UMLRSolution(Path.of(modelPath), "problem__test");
            // Add the first solution twice
            if(i==0)
                solutions.add(sol);
            solutions.add(sol);
        }
        EasierPopulationDAO popDao = new EasierPopulationDAO(solutions);

        assertTrue(popDao.getSolutions().size()==10, "Expected population size of 10 elements.");

    }
}