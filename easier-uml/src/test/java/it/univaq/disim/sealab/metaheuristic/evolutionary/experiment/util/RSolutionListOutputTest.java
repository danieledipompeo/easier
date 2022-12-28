package it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.util;

import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class RSolutionListOutputTest {

    RSolutionListOutput rSolutionListOutput;

    @BeforeEach
    void setUp() {
        String varFile = "VAR";
        String funFile = "FUN";

        Path modelPath = Path.of(getClass().getResource("/simplified-cocome/cocome.uml").getPath());

        List<UMLRSolution> population = new ArrayList<>();

        for(int i=0; i<3;i++){
            UMLRSolution sol = new UMLRSolution(modelPath, "problem-test");
            sol.setObjective(0,10);
            sol.setObjective(1,10);
            sol.setObjective(2,10);
            sol.setObjective(3,10);
            sol.createRandomRefactoring();
            population.add(sol);
        }

        rSolutionListOutput =
                new RSolutionListOutput(population).setVarFileOutputContext(new DefaultFileOutputContext(varFile, ","))
                    .setFunFileOutputContext(new DefaultFileOutputContext(funFile, ","));
    }

    @AfterEach
    void tearDown() {
    }

    // TODO improve the test below
    @Test
    void print() {
        rSolutionListOutput.print();
    }
}