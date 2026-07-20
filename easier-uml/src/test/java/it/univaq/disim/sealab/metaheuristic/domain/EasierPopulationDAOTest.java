package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class EasierPopulationDAOTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getSolutions() {
        String modelPath = getClass().getResource("/models/simplified-cocome/cocome.uml").getFile();
        UMLRSolution sol1 = new UMLRSolution(Path.of(modelPath), "problem__test");
        UMLRSolution sol2 = new UMLRSolution(Path.of(modelPath), "problem__test");
        List<RSolution<?>> solutions = List.of(sol1, sol2);

        EasierPopulationDAO popDao = new EasierPopulationDAO(solutions);

        List<Integer> storedIds = popDao.getSolutions().stream().map(EasierSolutionDAO::getSolID).collect(Collectors.toList());
        assertEquals(List.of(sol1.getName(), sol2.getName()), storedIds,
                "Expected getSolutions() to return the DAOs for exactly the given solutions, in order");
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