package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RefactoringActionFactoryTest {

    UMLRSolution sol;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        Files.createDirectories(Configurator.eINSTANCE.getTmpFolder());
        Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
    }

    @AfterAll
    static void tearDownAfterClass() throws IOException {
        Files.walk(Configurator.eINSTANCE.getTmpFolder())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @BeforeEach
    public void init() {
        String BASE_PATH = "/easier-uml2lqnCaseStudy/";
        String modelpath = getClass().getResource(BASE_PATH + "cocome/simplified-cocome/cocome.uml").getFile();
        UMLRProblem<RSolution<?>> p = new UMLRProblem<>(Paths.get(modelpath), "ccm__test");
        sol = (UMLRSolution) p.createSolution();
    }

    @Test
    public void getRandomActionTest() throws EasierException {
        RefactoringAction action =
                RefactoringActionFactory.getRandomAction(sol.getVariable(0).getEasierModel().getAvailableElements(),
                        sol.getVariable(0).getEasierModel().getInitialElements(), sol.getVariable(0).getEasierModel().getAllContents());
        assertNotNull(action, "The action should not be null");
        assertFalse(
                action.getTargetElements().values().stream().flatMap(Set::stream).collect(Collectors.toSet()).isEmpty(),
                "The refactoring action should have at least one target element");
    }

}
