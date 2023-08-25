package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class UMLResourceScalingTest extends UMLRefactoringActionTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();

        oldAction = new UMLResourceScaling(eModel.getAvailableElements(), eModel.getInitialElements());
        action = new UMLResourceScaling(eModel.getAvailableElements(), eModel.getInitialElements());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void execute() throws EasierException, URISyntaxException, EolModelLoadingException {
        super.testExecute();
    }

    @Test
    void testToString() {
    }

    @Test
    void toCSV() {
        numberOfCSVField = 6;
        actionName = "resource_scaling";
        super.testToCSV();
    }

    @Test
    void computeArchitecturalChanges() {
    }

    @Test
    void testEquals() {
    }
}