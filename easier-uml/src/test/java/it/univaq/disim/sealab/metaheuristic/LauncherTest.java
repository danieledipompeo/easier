package it.univaq.disim.sealab.metaheuristic;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRCrossover;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;

public class LauncherTest {

    Path modelPath;

    @BeforeEach
    public void setUp() {

        modelPath = Paths.get(getClass().getResource("/models/model/automatedGuidedVehicle.uml").getFile());

    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void createProblemTest() {
        int eval = 12;
        RProblem<UMLRSolution> rProblem = Launcher.createProblem(modelPath, eval);
        String expectedProblemName = "model__BRF_1.23__1.23__1.23__1.23__MaxEval_12__ProbPAs_0.95__sb_none_sbth_3600000__Algo_nsgaii";
        assertNotNull("Created a null problem.", rProblem);
        assertEquals(String.format("Exptected problem name %s \t generated %s", expectedProblemName, rProblem.getName()),
                expectedProblemName, rProblem.getName());
    }

    @Test
    public void configureAlgorithmListTest() {
        int eval = 12;
        List<ExperimentProblem<UMLRSolution>> problemList = new ArrayList<>();
        problemList.add(new ExperimentProblem<>(Launcher.createProblem(modelPath, eval)));

        UMLRCrossover<UMLRSolution> crossoverOperator = new UMLRCrossover<>(Configurator.eINSTANCE.getXoverProbabiliy());

        List<ExperimentAlgorithm<UMLRSolution, List<UMLRSolution>>> algoList = List.of(Launcher
                .configureAlgorithm(problemList.get(0), crossoverOperator, eval));

        assertEquals(String.format("Expected %s \t found %s", Configurator.eINSTANCE.getIndependentRuns(), algoList.size()),
                Configurator.eINSTANCE.getIndependentRuns(), algoList.size());

    }

}
