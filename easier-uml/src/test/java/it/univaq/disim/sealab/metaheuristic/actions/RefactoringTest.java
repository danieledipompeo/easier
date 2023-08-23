package it.univaq.disim.sealab.metaheuristic.actions;

import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLCloneNode;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvComponentToNN;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvOperationToComp;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLMvOperationToNCToNN;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.UnexpectedException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class RefactoringTest {

    Refactoring refactoring;
    UMLRSolution solution;

    @BeforeAll
    static void beforeClass() throws IOException {
        Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
    }

    @BeforeEach
    void setUp() {
        int allowedFailures = 100;
        int desired_length = 4;
        int populationSize = 4;

        String modelpath = getClass().getResource("/models/simplified-cocome/cocome.uml").getFile();
        UMLRProblem<UMLRSolution> p = new UMLRProblem<>(Paths.get(modelpath), "simplied-cocome__test");

        solution = p.createSolution();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(solution.getModelPath());
    }

    @Test
    void testExecute() throws EasierException {
        Refactoring refactoring = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel easierModel = refactoring.getEasierModel();

        RefactoringAction clone = new UMLCloneNode(easierModel.getAvailableElements(), easierModel.getInitialElements());
        RefactoringAction mvopncnn = new UMLMvOperationToNCToNN(easierModel.getAvailableElements(), easierModel.getInitialElements());
        RefactoringAction movopc = new UMLMvOperationToComp(easierModel.getAvailableElements(), easierModel.getInitialElements());
        RefactoringAction mvcpnn = new UMLMvComponentToNN(easierModel.getAvailableElements(), easierModel.getInitialElements());
        refactoring.getActions().addAll(List.of(clone, mvopncnn, movopc, mvcpnn));
        refactoring.execute();
    }



    @Test
    void testClone(){
        Refactoring cloned = refactoring.clone();
        assertEquals(refactoring, cloned);
    }

    @Test
    void testCloneDeprecated() {
        Refactoring cloneRefactoring = refactoring.clone();
        assertEquals(refactoring, cloneRefactoring);
    }

    @Test
    void testEquals() {
        assertEquals(refactoring, refactoring);
        Refactoring otherRefactoring = new UMLRefactoring(solution.getModelPath().toString());

        RefactoringAction[] actions = new RefactoringAction[4];

        int i =0;
        for(RefactoringAction action : refactoring.getActions()){
            actions[i] = action;
            i++;
        }

        otherRefactoring.getActions().addAll(List.of(actions[0], actions[2], actions[1],actions[3]));
        assertNotEquals(refactoring, otherRefactoring, "Expected two refactorings with different action order");


    }
    @Test
    /*
      It should find a multiple occurrence of  the first
      refactoring action.
      The refactoring has been built synthetically.
     */
    void testFindMultipleOccurrenceWithMultiOccurrences() throws EasierException {
        Refactoring refactoring = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel easierModel = refactoring.getEasierModel();
        RefactoringAction clone = new UMLCloneNode(easierModel.getAvailableElements(), easierModel.getInitialElements());
        RefactoringAction clone1 = clone.clone();
        RefactoringAction mvopncnn = new UMLMvOperationToNCToNN(easierModel.getAvailableElements(), easierModel.getInitialElements());
        RefactoringAction movopc = new UMLMvOperationToComp(easierModel.getAvailableElements(), easierModel.getInitialElements());
        RefactoringAction mvcpnn = new UMLMvComponentToNN(easierModel.getAvailableElements(), easierModel.getInitialElements());
        refactoring.getActions().addAll(List.of(clone, clone1, movopc, mvcpnn));
        assertTrue(refactoring.hasMultipleOccurrence(), String.format("Expected a multiple occurrence"));
    }

    @Test
    void testTryRandomPush() throws UnexpectedException, EolRuntimeException, EasierException {

        Refactoring ref = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = ref.getEasierModel();
        RefactoringAction clone = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements());
        RefactoringAction mvopncnn = new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements());
        RefactoringAction clone1 = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements());
        RefactoringAction mvcpnn = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements());
        ref.getActions().add(clone);//, mvopncnn, clone1, mvcpnn));
        solution.setVariable(0, ref);

        eModel.getTargetRefactoringElement().get(UMLRSolution.SupportedType.NODE.toString()).add(clone.getCreatedElements().get(UMLRSolution.SupportedType.NODE.toString()).iterator().next());

        ref.tryRandomPush();

        assertTrue(eModel.getAvailableElements().values().stream().flatMap(Set::stream).anyMatch(clone.getCreatedElements().get(UMLRSolution.SupportedType.NODE.toString())::contains));
    }

}
