package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class UMLChangePassiveResourceTest extends UMLRefactoringActionTest {


    @Test
    void execute() throws EasierException, URISyntaxException, EolModelLoadingException {
        ((UMLChangePassiveResource)action).setTaggedValue("queueSize");
        super.testExecute();
        ((UMLChangePassiveResource)action).setTaggedValue("memorySize");
        super.testExecute();
        ((UMLChangePassiveResource)action).setTaggedValue("srPoolSize");
        super.testExecute();
    }

    @Test
    void testToString() {

    }

    @Test
    void toCSV() {
        numberOfCSVField = 6;
        actionName = "change_passive_resource";
        super.testToCSV();
    }

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();

        oldAction = new UMLChangePassiveResource(eModel.getAvailableElements(), eModel.getInitialElements());
        action = new UMLChangePassiveResource(eModel.getAvailableElements(), eModel.getInitialElements());
    }

    @AfterEach
    void tearDown() {
    }
}