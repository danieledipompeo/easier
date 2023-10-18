package it.univaq.disim.sealab.metaheuristic.actions;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.actions.uml.*;
import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.uml2.uml.Node;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


public class UMLRefactoringTest {

    Refactoring refactoring;
    UMLRSolution solution;
    private RefactoringAction deleteNode;
    private RefactoringAction clone;
    private RefactoringAction mvopncnn;
    private RefactoringAction movopc;
    private RefactoringAction mvcpnn;


    @BeforeAll
    static void setClass() throws IOException {
        Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
    }

    @BeforeEach
    public void setUp() throws EasierException {
        String modelPath = getClass().getResource("/models/simplified-cocome/cocome.uml").getFile();
        UMLRProblem<UMLRSolution> p = new UMLRProblem<>(Paths.get(modelPath), "simplied-cocome__test");

        solution = p.createSolution();
        refactoring = solution.getVariable(0);

        EasierModel eModel = refactoring.getEasierModel();

        // Refactoring actions used in some tests
        deleteNode =
                new UMLRemoveNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());

        clone = new UMLCloneNode(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        mvopncnn =
                new UMLMvOperationToNCToNN(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        movopc = new UMLMvOperationToComp(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());
        mvcpnn = new UMLMvComponentToNN(eModel.getAvailableElements(), eModel.getInitialElements(),
                eModel.getAllContents());
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(solution.getModelPath());
    }

    @Test
    public void testExecute() throws URISyntaxException, EolModelLoadingException, EasierException {
        Refactoring refactoring = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = refactoring.getEasierModel();

        refactoring.getActions().addAll(List.of(clone, mvopncnn, movopc, mvcpnn));
        refactoring.execute();

        EasierUmlModel model = EOLStandalone.createUmlModel(solution.getModelPath().toString());

        Object result = model.allContents().stream().filter(Node.class::isInstance).map(Node.class::cast)
                .filter(ne -> ne.getName()
                        .equals(clone.getCreatedElements().get(Configurator.NODE_LABEL).iterator()
                                .next())).findFirst().orElse(null);
        assertNotNull(result, "The refactored model should contain the created element of the action");
    }


    @Test
    public void testClone() {
        Refactoring cloned = refactoring.clone();
        assertEquals(refactoring, cloned);
    }

    @Test
    public void testCloneDeprecated() {
        Refactoring cloneRefactoring = refactoring.clone();
        assertEquals(refactoring, cloneRefactoring);
    }

    @Test
    public void testEquals() {
        assertEquals(refactoring, refactoring);
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
    /*
      It should find a multiple occurrence of  the first
      refactoring action.
      The refactoring has been built synthetically.
     */
    public void testHasMultipleOccurrence() throws EasierException {
        Refactoring refactoring = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = refactoring.getEasierModel();

        RefactoringAction clone1 = clone.clone();

        refactoring.getActions().addAll(List.of(clone, clone1, movopc, mvcpnn));
        assertTrue(refactoring.hasMultipleOccurrence(), "Expected a multiple occurrence");
    }

    @Test
    void csv_file_with_all_actions() throws EasierException {
        Refactoring refactoring = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = refactoring.getEasierModel();

        RefactoringAction resource_scaling = new UMLResourceScaling(eModel.getAvailableElements(),
                eModel.getInitialElements(), eModel.getAllContents());

        refactoring.getActions().addAll(List.of(clone, movopc, mvopncnn, resource_scaling));

        // Print to console
        int header_fields = "solID,operation,target,to,where,tagged_value,factor".split(",").length;
        System.out.println(refactoring.toCSV());

    }

    @Test
    void refactoring_should_fail_when_using_deleted_target() throws EasierException {
        Refactoring refactoring = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = refactoring.getEasierModel();

        String deletedNode =
                deleteNode.getTargetElements().get(Configurator.NODE_LABEL).stream().findFirst()
                        .orElseThrow(() -> {
                            return new EasierException(
                                    "Error when extracting the target element in: " + this.getClass().getSimpleName());
                        });

        clone.getTargetElements().get(Configurator.NODE_LABEL).clear();
        clone.getTargetElements().get(Configurator.NODE_LABEL).add(deletedNode);

        refactoring.getActions().addAll(List.of(deleteNode, clone, movopc, mvopncnn));
        Assertions.assertFalse(refactoring.execute(),
                "Expected refactoring to fail when using a deleted target element");
    }

    @Test
    void refactoring_should_be_feasible() {
       Refactoring refactoring = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = refactoring.getEasierModel();

        assertTrue(Stream.of(deleteNode, clone, movopc, mvopncnn).allMatch(refactoring::addRefactoringAction),
                "Expected refactoring to be feasible");
    }

    @Test
    void refactoring_should_be_unfeasible() throws EasierException {
        Refactoring refactoring = new UMLRefactoring(solution.getModelPath().toString());
        EasierModel eModel = refactoring.getEasierModel();

        String deletedNode =
                deleteNode.getTargetElements().get(Configurator.NODE_LABEL).stream().findFirst()
                        .orElseThrow(() -> new EasierException(
                                "Error when extracting the target element in: " + this.getClass().getSimpleName()));

        clone.getTargetElements().get(Configurator.NODE_LABEL).clear();
        clone.getTargetElements().get(Configurator.NODE_LABEL).add(deletedNode);

        assertFalse(Stream.of(deleteNode, clone, movopc, mvopncnn).allMatch(refactoring::addRefactoringAction)
                , "Expected refactoring to be unfeasible");
    }
}
