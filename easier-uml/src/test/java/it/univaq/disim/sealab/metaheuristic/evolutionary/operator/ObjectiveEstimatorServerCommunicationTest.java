package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;

import it.univaq.disim.sealab.metaheuristic.domain.EasierSolutionDAO;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;

public class ObjectiveEstimatorServerCommunicationTest {

    private static final String SERVER_ENDPOINT =
            System.getProperty("surrogate.endpoint", "http://127.0.0.1:5000/surrogate");

    /**
     * EasierSolutionDAO.ADDED_SOLUTION is a static set that is never cleared.
     * If a solution ID was seen in a previous test (or a previous iteration of the
     * real algorithm), EasierPopulationDAO silently skips it and produces an empty
     * solutions list, causing the server to receive {"solutions":[]} and return 400.
     * Clearing the set before each test guarantees the payload is always populated.
     */
    @BeforeEach
    void resetSolutionIds() throws Exception {
        Field f = EasierSolutionDAO.class.getDeclaredField("ADDED_SOLUTION");
        f.setAccessible(true);
        ((java.util.Set<?>) f.get(null)).clear();
    }

    @Test
    void directCall_reachesSurrogateServer() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        assertTrue(isServerReachable(client),
            "Surrogate server is not reachable at " + SERVER_ENDPOINT +
                " (wrong URL/port or server not running)");

        String body = "{\"caseStudy\":\"cocome\",\"solutions\":[],\"iteration\":1,\"k\":0}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_ENDPOINT))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Empty solutions are rejected by contract with HTTP 400.
        assertEquals(400, response.statusCode(),
            "Expected HTTP 400 for empty solutions payload");
        assertTrue(response.body().contains("'solutions' must contain at least one solution"),
            "Expected validation message for empty solutions payload");
        assertTrue(response.body() != null, "Expected a response body from the surrogate server");
    }

    @Test
    void directCall_withValidPayload_returnsSuccess() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        assertTrue(isServerReachable(client),
            "Surrogate server is not reachable at " + SERVER_ENDPOINT +
                " (wrong URL/port or server not running)");

        String body = "{" +
                "\"caseStudy\":\"cocome\"," +
                "\"iteration\":2," +
                "\"k\":1," +
                "\"solutions\":[{" +
                "\"solID\":1," +
                "\"markedForSurrogate\":true," +
                "\"objectives\":{" +
                "\"changes\":1.0," +
                "\"energyPerScenario__ProcessSale_job_class\":1.0," +
                "\"energyPerScenario__ReceivedOrderedProducts_job_class\":1.0," +
                "\"energyPerScenario__ShowDeliveryReports_job_class\":1.0," +
                "\"pas\":1.0," +
                "\"perfq\":1.0," +
                "\"power\":1.0," +
                "\"price\":1.0," +
                "\"pricePerScenario__ProcessSale_job_class\":1.0," +
                "\"pricePerScenario__ReceivedOrderedProducts_job_class\":1.0," +
                "\"pricePerScenario__ShowDeliveryReports_job_class\":1.0," +
                "\"reliability\":1.0" +
                "}," +
                "\"refactoring\":[{" +
                "\"name\":\"moc\"," +
                "\"target\":\"scanProduct\"," +
                "\"to\":\"GUIStore\"," +
                "\"where\":\"\"," +
                "\"taggedValue\":\"\"," +
                "\"scalingFactor\":\"\"" +
                "}]" +
                "}]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_ENDPOINT))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(),
            "Expected HTTP 200 for valid surrogate payload. Response was: " + response.body());
        assertTrue(response.body().contains("solutions"),
            "Expected response to include solutions array");
    }

    @Test
    void objectiveEstimator_callsRealServerEndpoint() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        assertTrue(isServerReachable(client),
            "Surrogate server is not reachable at " + SERVER_ENDPOINT +
                " (wrong URL/port or server not running)");

        Configurator originalConfigurator = Configurator.eINSTANCE;
        Configurator testConfigurator = new Configurator();

        try {
            setPrivateField(testConfigurator, "surrogateEndpoint", SERVER_ENDPOINT);
            Configurator.setInstance(testConfigurator);

            // Build one real solution so surrogateEvaluation does not short-circuit.
            Path modelPath = Paths.get(getClass().getResource(
                    "/easier-uml2lqnCaseStudy/cocome/simplified-cocome/cocome.uml").getPath());
            UMLRProblem<UMLRSolution> problem = new UMLRProblem<>(modelPath, "cocome__integration");
            UMLRSolution solution = problem.createSolution();
            solution.setMarkedForSurrogate(true);

            // Populate all objectives expected by the surrogate encoder/model.
            solution.getMapOfObjectives().put("changes", 1.0);
            solution.getMapOfObjectives().put("energyPerScenario__ProcessSale_job_class", 1.0);
            solution.getMapOfObjectives().put("energyPerScenario__ReceivedOrderedProducts_job_class", 1.0);
            solution.getMapOfObjectives().put("energyPerScenario__ShowDeliveryReports_job_class", 1.0);
            solution.getMapOfObjectives().put("pas", 1.0);
            solution.getMapOfObjectives().put("perfq", 1.0);
            solution.getMapOfObjectives().put("power", 1.0);
            solution.getMapOfObjectives().put("price", 1.0);
            solution.getMapOfObjectives().put("pricePerScenario__ProcessSale_job_class", 1.0);
            solution.getMapOfObjectives().put("pricePerScenario__ReceivedOrderedProducts_job_class", 1.0);
            solution.getMapOfObjectives().put("pricePerScenario__ShowDeliveryReports_job_class", 1.0);
            solution.getMapOfObjectives().put("reliability", 1.0);

            // This call should now perform an actual server interaction.
            ObjectiveEstimator.surrogateEvaluation(List.of(solution), 2, "cocome", 1);
        } finally {
            Configurator.setInstance(originalConfigurator);
        }
    }

    private static boolean isServerReachable(HttpClient client) {
        String healthEndpoint = SERVER_ENDPOINT.replaceFirst("/(surrogate|train)$", "/health");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(healthEndpoint))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }


    @Test
    void test_trainingEndpoint() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        // assertTrue(isServerReachable(client),
        //     "Surrogate server is not reachable at " + SERVER_ENDPOINT +
        //         " (wrong URL/port or server not running)");

        Path popFile = Paths.get(getClass()
                .getResource("/population_for_surrogate/population__2.json").toURI());
        String body = java.nio.file.Files.readString(popFile);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_ENDPOINT))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(),
            "Expected HTTP 200 for training payload. Response was: " + response.body());
        assertTrue(response.body().contains("success"),
            "Expected response to contain 'success'. Response was: " + response.body());

    }
}
