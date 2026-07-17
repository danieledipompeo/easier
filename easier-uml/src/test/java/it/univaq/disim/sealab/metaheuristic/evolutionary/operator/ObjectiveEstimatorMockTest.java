package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.univaq.disim.sealab.metaheuristic.utils.EasierObjectiveNotFoundException;
import org.junit.jupiter.api.Test;

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
        UMLRSolution solution = new UMLRSolution(modelPath, "test");
        assertEquals(9, solution.getNumberOfObjectives(), "Number of objectives should be 9");

        // Call the static methods under test
        ObjectiveEstimator.computeObjectives(solution);
        assertTrue(solution.getMapOfObjectives().keySet().stream().anyMatch(k -> k.contains(scenarios.get(0))),
            "The map of objectives should contain the scenario key: \n " + solution.getMapOfObjectives());
        ObjectiveEstimator.setConsideredObjectives(solution);

    }

    @Test
    void surrogateEvaluation_sendsRetrainIntervalInBodyAndUsesEndpointAsIs() throws Exception {
        Configurator originalConfigurator = Configurator.eINSTANCE;
        Configurator testConfigurator = new Configurator();

        AtomicReference<String> requestedPathWithQuery = new AtomicReference<>();
        AtomicReference<String> requestedBody = new AtomicReference<>();
        CountDownLatch requestReceived = new CountDownLatch(1);

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        try {
            server.createContext("/surrogate", exchange -> {
                handleSurrogateRequest(exchange, requestedPathWithQuery, requestedBody, requestReceived);
            });
            server.start();

            String endpoint = "http://localhost:" + server.getAddress().getPort() + "/surrogate";
            System.out.println("[TEST] Using mock surrogate endpoint: " + endpoint);
            setPrivateField(testConfigurator, "surrogateEndpoint", endpoint);
            Configurator.setInstance(testConfigurator);

            ObjectiveEstimator.surrogateEvaluation(List.of(), 12, "cocome", 5);

            assertTrue(requestReceived.await(3, TimeUnit.SECONDS), "Expected a request to the surrogate server");
            assertEquals("/surrogate", requestedPathWithQuery.get());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode body = mapper.readTree(requestedBody.get());
            assertEquals(12, body.get("iteration").asInt());
            assertEquals(5, body.get("k").asInt());
            assertTrue(body.has("solutions") && body.get("solutions").isArray());
        } finally {
            server.stop(0);
            Configurator.setInstance(originalConfigurator);
        }
    }

    @Test
    void surrogateEvaluation_preservesExistingQueryStringInEndpoint() throws Exception {
        Configurator originalConfigurator = Configurator.eINSTANCE;
        Configurator testConfigurator = new Configurator();

        AtomicReference<String> requestedPathWithQuery = new AtomicReference<>();
        AtomicReference<String> requestedBody = new AtomicReference<>();
        CountDownLatch requestReceived = new CountDownLatch(1);

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        try {
            server.createContext("/surrogate", exchange -> {
                handleSurrogateRequest(exchange, requestedPathWithQuery, requestedBody, requestReceived);
            });
            server.start();

            String endpoint = "http://127.0.0.1:" + server.getAddress().getPort() + "/surrogate?apiVersion=v1";
            System.out.println("[TEST] Using mock surrogate endpoint: " + endpoint);
            setPrivateField(testConfigurator, "surrogateEndpoint", endpoint);
            Configurator.setInstance(testConfigurator);

            ObjectiveEstimator.surrogateEvaluation(List.of(), 7, "cocome", 3);

            assertTrue(requestReceived.await(3, TimeUnit.SECONDS), "Expected a request to the surrogate server");
            assertEquals("/surrogate?apiVersion=v1", requestedPathWithQuery.get());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode body = mapper.readTree(requestedBody.get());
            assertEquals(7, body.get("iteration").asInt());
            assertEquals(3, body.get("k").asInt());
        } finally {
            server.stop(0);
            Configurator.setInstance(originalConfigurator);
        }
    }

    private static void handleSurrogateRequest(HttpExchange exchange,
                                               AtomicReference<String> requestedPathWithQuery,
                                               AtomicReference<String> requestedBody,
                                               CountDownLatch requestReceived) throws IOException {
        requestedPathWithQuery.set(exchange.getRequestURI().toString());
        requestedBody.set(new String(exchange.getRequestBody().readAllBytes()));

        System.out.println("[TEST] Mock server received request: " + exchange.getRequestMethod() + " "
            + exchange.getRequestURI());
        System.out.println("[TEST] Mock server request body: " + requestedBody.get());

        byte[] response = "{\"solutions\":[]}".getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();

        requestReceived.countDown();
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

}
