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

class UMLChangePassiveResourceTest extends UMLRefactoringActionTest {



    @ParameterizedTest
    @ValueSource(strings = {"cocome/simplified-cocome/cocome.uml", "train-ticket/train-ticket.uml",
            "eshopper/eshopper.uml"})
    void execute(String mPath) throws EasierException, URISyntaxException, EolModelLoadingException {
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLChangePassiveResource(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());

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
    void toCSV() throws EasierException {
        numberOfCSVField = 6;
        actionName = "change_passive_resource";

        String mPath = "cocome/simplified-cocome/cocome.uml";
        modelpath = getClass().getResource(BASE_PATH + mPath).getPath();
        eModel = new UMLEasierModel(modelpath);
        action = new UMLChangePassiveResource(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
        super.testToCSV();
    }

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();

//        oldAction = new UMLChangePassiveResource(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
//        action = new UMLChangePassiveResource(eModel.getAvailableElements(), eModel.getInitialElements(), eModel.getAllContents());
    }

    @AfterEach
    void tearDown() {
    }
}