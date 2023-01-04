package it.univaq.disim.sealab.metaheuristic.actions;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLCloneNode;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvComponentToNN;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvOperationToComp;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvOperationToNCToNN;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.domain.UMLEasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.uml2.uml.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class UMLRefactoringTest {

    Refactoring refactoring;
    UMLRSolution solution;


    @BeforeAll
    static void setClass() throws IOException {
        Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
    }

    @BeforeEach
    public void setUp() {
        String modelPath = getClass().getResource("/simplified-cocome/cocome.uml").getFile();
        UMLRProblem<UMLRSolution> p = new UMLRProblem<>(Paths.get(modelPath), "simplied-cocome__test");

        solution = p.createSolution();
        refactoring = solution.getVariable(0);
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(solution.getModelPath());
    }

    @Test
    public void testExecute() throws URISyntaxException, EolModelLoadingException {
        Refactoring refactoring = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = refactoring.getEasierModel();

        RefactoringAction clone = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements());

        RefactoringAction mvopncnn = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements());

        RefactoringAction movopc = new UMLMvOperationToComp(eModel.getAvailableElements(), eModel.getInitialElements());

        RefactoringAction mvcpnn = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements());

        refactoring.getActions().addAll(List.of(clone, mvopncnn, movopc, mvcpnn));
        refactoring.execute();

        EasierUmlModel model = EOLStandalone.createUmlModel(solution.getModelPath().toString());

        Object result = model.allContents().stream().filter(Node.class::isInstance).map(Node.class::cast).filter(ne -> ne.getName().equals(clone.getCreatedElements().get(UMLRSolution.SupportedType.NODE.toString()).iterator().next())).findFirst().orElse(null);
        assertNotNull(result, "The refactored model should contain the created element of the action");
    }


    @Test
    public void copy_constructor_should_create_an_equals_refactoring() {
        Refactoring cloned = new UMLRefactoring(refactoring);
        assertEquals(refactoring, cloned);
    }

    @Test
    public void equals_should_return_true_when_comparing_identical_refactoring() {
        assertEquals(refactoring, refactoring);
    }

    @Test
    void equals_should_return_false_when_comparing_different_refactorings() {
        Refactoring otherRefactoring = new UMLRefactoring(solution.getModelPath().toString());

        RefactoringAction[] actions = new RefactoringAction[4];

        int i = 0;
        for (RefactoringAction action : refactoring.getActions()) {
            actions[i] = action;
            i++;
        }

        otherRefactoring.getActions().addAll(List.of(actions[0], actions[2], actions[1], actions[3]));
        assertNotEquals(refactoring, otherRefactoring, "Expected two refactorings with different action order");
    }

    @Test
    void equals_return_false_with_different_models(){
        Refactoring otherRefactoring = new UMLRefactoring(refactoring);

        String otherModel = getClass().getResource("/train-ticket/train-ticket.uml").getFile();
        otherRefactoring.easierModel = new UMLEasierModel(otherModel);

        assertNotEquals(refactoring, otherRefactoring);
    }

    @Test
    /*
      It should find a multiple occurrence of  the first
      refactoring action.
      The refactoring has been built synthetically.
     */
    public void refactoring_has_multiple_occurrence_of_the_first_action() {
        Refactoring refactoring = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = refactoring.getEasierModel();

        RefactoringAction clone = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements());
        RefactoringAction clone1 = clone.copy();
        RefactoringAction mvopncnn = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements());
        RefactoringAction movopc = new UMLMvOperationToComp(eModel.getAvailableElements(), eModel.getInitialElements());
        RefactoringAction mvcpnn = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements());

        refactoring.getActions().addAll(List.of(clone, clone1, movopc, mvcpnn));
        assertTrue(refactoring.hasMultipleOccurrence(), String.format("Expected a multiple occurrence"));
    }


}
