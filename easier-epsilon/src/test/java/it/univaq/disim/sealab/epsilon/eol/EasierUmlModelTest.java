package it.univaq.disim.sealab.epsilon.eol;

import it.univaq.disim.sealab.epsilon.EasierStereotypeNotPropertlyAppliedException;
import it.univaq.disim.sealab.epsilon.EpsilonStandalone;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class EasierUmlModelTest {

    EasierUmlModel eModel;

    @BeforeEach
    void setUp() throws URISyntaxException, EolModelLoadingException {
        Path sourceModelPath = Path.of(getClass().getResource("/cocome/simplified-cocome/cocome.uml").getPath());
        eModel = EpsilonStandalone.createUmlModel(sourceModelPath.toString());
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void computeSystemResponseTime()
            throws EolModelElementTypeNotFoundException, EasierStereotypeNotPropertlyAppliedException {

        assertDoesNotThrow(eModel::computeSystemResponseTime);

        double sysRespT = eModel.computeSystemResponseTime();
        assertNotEquals(Double.MIN_VALUE, sysRespT);
        System.out.printf("[TEST] system response time for %s is %s", eModel.getModelFile(), sysRespT);

    }

}