package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.univaq.disim.sealab.metaheuristic.utils.EasierObjectiveNotFoundException;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;

public class ObjectiveEstimatorMockTest {

    @Test
    void setObjective() throws EasierException, EasierObjectiveNotFoundException {
        // Mock the model path
        String mPath = "/cocome/simplified-cocome/cocome.uml";
        Path modelPath = Path.of(getClass().getResource("/easier-uml2lqnCaseStudy" + mPath).getPath());

        List<String> scenarios = List.of(
                "ProcessSale_job_class",
                "ShowDeliveryReports_job_class",
                "ReceivedOrderedProducts_job_class");
        Configurator.eINSTANCE.updateObjectiveList(scenarios);

        // Mock the RSolution and related behavior
        UMLRSolution spiedSolution = spy(new UMLRSolution(modelPath, "test"));
        doReturn(modelPath).when(spiedSolution).getModelPath();
        assertEquals(9, spiedSolution.getNumberOfObjectives(), "Number of objectives should be 9");

        // Mock the ObjectiveEstimator behavior
        ObjectiveEstimator estimator = spy(new ObjectiveEstimator());
        doNothing().when(estimator).setConsideredObjectives(spiedSolution);

        // Call the methods to verify behavior
        estimator.computeObjectives(spiedSolution);
        assertTrue(spiedSolution.getMapOfObjectives().keySet().stream().anyMatch(k -> k.contains(scenarios.get(0))),
                "The map of objectives should contain the scenario key: \n " + spiedSolution.getMapOfObjectives());
        estimator.setConsideredObjectives(spiedSolution);

        // Verify that the mocked methods were called
        verify(estimator).computeObjectives(spiedSolution);
        verify(estimator).setConsideredObjectives(spiedSolution);

    }

}
