package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.domain.UMLEasierModel;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class UMLEncryptCommTest extends UMLRefactoringActionTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    void tearDown() {
    }

    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    void execute(String mPath) throws EasierException {
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLEncryptComm(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        assertDoesNotThrow(super::testExecute);
    }

    @Test
    void testToString() throws EasierException {
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLEncryptComm(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());

        String targetNode = action.getTargetElements().get(Configurator.NODE_LABEL).iterator().next();
        String result = action.toString();

        assertTrue(result.startsWith("Encrypt communication:"), "Expected toString to start with the action label");
        assertTrue(result.contains(targetNode), "Expected toString to contain the target node: " + targetNode);
    }

    @Test
    void toCSV() throws EasierException {
        numberOfCSVField = 6;
        actionName = "encrypt_comm";
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLEncryptComm(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testToCSV();
    }

    @Test
    void computeArchitecturalChanges() throws EasierException, URISyntaxException, EolModelLoadingException {
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLEncryptComm(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testComputeArchitecturalChanges();
    }

    @Test
    void testEquals() throws EasierException {
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLEncryptComm(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testEquals();
    }
}
