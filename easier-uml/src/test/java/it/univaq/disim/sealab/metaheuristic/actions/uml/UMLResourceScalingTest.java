package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.metaheuristic.domain.UMLEasierModel;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class UMLResourceScalingTest extends UMLRefactoringActionTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();

//        oldAction = new UMLResourceScaling(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
//        action = new UMLResourceScaling(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
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
        action = new UMLResourceScaling(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        assertDoesNotThrow(super::testExecute);
    }

    @Test
    void testToString() {
    }

    @Test
    void toCSV() throws EasierException {
        numberOfCSVField = 6;
        actionName = "resource_scaling";
        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLResourceScaling(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testToCSV();
    }

    @Test
    void computeArchitecturalChanges() {
    }

    @Test
    void testEquals() {
    }
}