package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.univaq.disim.sealab.epsilon.EasierModelElementNotFoundException;
import it.univaq.disim.sealab.epsilon.EasierStereotypeNotPropertlyAppliedException;
import it.univaq.disim.sealab.epsilon.EpsilonStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.epsilon.evl.EVLStandalone;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.domain.EasierPopulationDAO;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import it.univaq.disim.sealab.metaheuristic.utils.EasierLogger;
import it.univaq.disim.sealab.metaheuristic.utils.EasierObjectiveNotFoundException;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import it.univaq.disim.sealab.metaheuristic.utils.Energy;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
import it.univaq.disim.sealab.metaheuristic.utils.UMLMemoryOptimizer;
import it.univaq.disim.sealab.metaheuristic.utils.WorkflowUtils;
import it.univaq.easier.LQN;
import it.univaq.easier.Profiler;
import it.univaq.easier.Scenario;
import it.univaq.easier.UML;
import it.univaq.sealab.umlreliability.MissingTagException;
import it.univaq.sealab.umlreliability.Reliability;
import it.univaq.sealab.umlreliability.UMLReliability;
import it.univaq.sealab.umlreliability.model.UMLModelPapyrus;

public class ObjectiveEstimator {

    /**
     * This method counts the number of Performance Antipatterns (PAs) invoking the
     * PADRE perf-detection file
     */
    public static double countPerformanceAntipattern(Path sourceModelPath, int solutionID) {
        EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "countingPAs_start");

        String refactoringLibraryModule = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(),
                "..", "easier-refactoringLibrary", "evl", "AP-UML-MARTE.evl").toString();

        Map<String, Map<String, Double>> extractFuzzyValues = new HashMap<>();
        double numPAs = 0d;

        EVLStandalone pasCounter = new EVLStandalone();
        try (EasierUmlModel uml = EpsilonStandalone.createUmlModel(sourceModelPath.toString())) {
            pasCounter.setModel(uml);

            pasCounter.setSource(Paths.get(refactoringLibraryModule));

            // set the prob to be perf antipatterns
            double fuzzyThreshold = Configurator.eINSTANCE.getProbPas();
            pasCounter.setParameter(fuzzyThreshold, "float", "prob_to_be_pa");

            extractFuzzyValues = pasCounter.extractFuzzyValues();
        } catch (EolModelLoadingException | URISyntaxException e) {
            // throw new EasierException(e);
            EasierLogger.logger_.severe(
                    String.format("Solution: #%s has thrown an error when computing the pas on: %s " + "because of: %s",
                            solutionID, sourceModelPath, e.getMessage()));
            EasierLogger.logger_.info("PAs is set to Double.MAX_VALUE");
            return Double.MAX_VALUE;
        }

        // Count performance antipatterns and build a string for the next csv storing
        for (String pas : extractFuzzyValues.keySet()) {
            Map<String, Double> mPaf = extractFuzzyValues.get(pas);
            numPAs += mPaf.keySet().size();
            for (Map.Entry<String, Double> targetElement : mPaf.entrySet()) {
                double fuzzy = targetElement.getValue();
                new FileUtils().performanceAntipatternDumpToCSV(
                        String.format("%s,%s,%s,%.4f", solutionID, pas, targetElement.getKey(), fuzzy));
            }
        }

        pasCounter.clearMemory();
        new UMLMemoryOptimizer().cleanup();

        EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "countingPAs_end");
        EasierLogger.logger_.info(String.format("Performance antipatterns : %s", numPAs));
        return numPAs;

    }

    /**
     * @return the performance quality indicator as described in <a href=
     *         "https://doi.org/10.1109/ICSA.2018.00020">https://doi.org/10.1109/ICSA.2018.00020</a>
     */
    public static double perfQ(Path sourceModelPath, Path modelPath) throws EasierException {

        /*
         * The updated model can have more nodes than the source node since original
         * nodes can be cloned. The benefits of cloning nodes is taken into account by
         * the performance model. For this reason, the perfQ analyzes only the
         * performance metrics of the nodes common among the models
         */
        EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "evaluatePerformance_start");
        try (EasierUmlModel source = EpsilonStandalone.createUmlModel(sourceModelPath.toString());
                EasierUmlModel uml = EpsilonStandalone.createUmlModel(modelPath.toString())) {

            double perfQ = source.computePerfQ(uml);
            // if (perfQ == Double.MAX_VALUE) {
            // EasierLogger.logger_.severe(String.format("PerfQ is %s because no performance
            // index has been " +
            // "computed.", Double.MAX_VALUE));
            // throw new EasierException();
            // return -1 * Double.MAX_VALUE;
            // }

            new UMLMemoryOptimizer().cleanup();
            EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "evaluatePerformance_end");
            EasierLogger.logger_.info(String.format("PerfQ : %s", perfQ));
            // It must be minimized
            return -1 * perfQ;
        } catch (URISyntaxException | EolModelLoadingException | EolModelElementTypeNotFoundException
                | EasierModelElementNotFoundException e) {
            EasierLogger.logger_
                    .severe(String.format("PerfQ cannot be computed on %s because of: %s", modelPath, e.getMessage()));
            EasierLogger.logger_.info("PerfQ is set to -1 * Double.MAX_VALUE");
            throw new EasierException(
                    String.format("PerfQ cannot be computed on %s because of: %s", modelPath, e.getMessage()));
            // return -1 * Double.MAX_VALUE;
        }
    }

    /**
     * It computes the system response time of the model. The system response time
     * is the sum of scenarios response time It uses the
     * computeResponseTimePerScenario() method to compute the response time
     *
     * @param modelPath the path of the UML model
     * @return the system response time
     */
    public static double systemResponseTime(Path modelPath) throws EasierException {
        EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "evaluatePerformance_start");
        double sysRespT = responseTimePerScenario(modelPath).values().stream().mapToDouble(Double::doubleValue).sum();
        // try (EasierUmlModel model =
        // EpsilonStandalone.createUmlModel(modelPath.toString())) {
        // double sysRespT = model.computeSystemResponseTime();
        // new UMLMemoryOptimizer().cleanup();
        EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "evaluatePerformance_end");
        EasierLogger.logger_.info(String.format("System RespT : %s", sysRespT));

        return sysRespT;
        // } catch (URISyntaxException | EolModelLoadingException |
        // EolModelElementTypeNotFoundException |
        // EasierStereotypeNotPropertlyAppliedException e) {
        // EasierLogger.logger_.severe(String.format("Error while computing the System
        // RespT on: %s for the reason: %s",
        // modelPath, e.getMessage()));
        // EasierLogger.logger_.info("System response time is set to Double.MAX_VALUE");
        // throw new EasierException(String.format("Error while computing the System
        // RespT on: %s for the reason: %s",
        // modelPath, e.getMessage()));
        // }
    }

    /**
     * It computes the response time of each scenario. It appends "_job_class" to
     * the name of the scenario
     *
     * @param modelPath the path of the UML model
     * @return A map containing the name of the scenario and its response time
     */
    public static Map<String, Double> responseTimePerScenario(Path modelPath) throws EasierException {
        EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "evaluatePerformance_start");
        try (EasierUmlModel model = EpsilonStandalone.createUmlModel(modelPath.toString())) {
            Map<String, Double> respTXScenario = model.computeResponseTimePerScenario();
            new UMLMemoryOptimizer().cleanup();
            EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "evaluatePerformance_end");
            EasierLogger.logger_.info(String.format("RespT of each use case: %s", respTXScenario));

            return respTXScenario;
        } catch (URISyntaxException | EolModelLoadingException | EolModelElementTypeNotFoundException
                | EasierStereotypeNotPropertlyAppliedException e) {
            EasierLogger.logger_.severe(String.format(
                    "Error while computing the System RespT on: %s for the reason: %s", modelPath, e.getMessage()));
            EasierLogger.logger_.info("System response time is set to Double.MAX_VALUE");
            throw new EasierException(String.format("Error while computing the System RespT on: %s for the reason: %s",
                    modelPath, e.getMessage()));
        }

    }

    /**
     * Estimate the system energy consumption as defined in
     * <p>
     * Stoico, V., Cortellessa, V., Malavolta, I., Di Pompeo, D., Pomante, L., Lago,
     * P. (2023). An Approach Using Performance Models for Supporting Energy
     * Analysis of Software Systems. In: Computer Performance Engineering and
     * Stochastic Modelling. EPEW ASMTA 2023. Lecture Notes in Computer Science, vol
     * 14231. Springer, Cham. https://doi.org/10.1007/978-3-031-43185-2_17
     * </p>
     *
     * @param modelPath the path of the UML model
     * @return the system energy consumption
     * @throws EasierException when the system energy cannot be computed
     */
    public static double energyEstimation(Path modelPath) throws EasierException {
        double energy;
        EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "evaluateEnergy_start");

        String umlFile = modelPath.toString();
        String lqxoFile = modelPath.getParent().resolve("output.lqxo").toString();
        energy = Energy.computeSystemEnergy(umlFile, lqxoFile);

        EasierLogger.logger_.info(String.format("System Energy : %s", energy));
        EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "evaluateEnergy_end");
        return energy;
    }

    /**
     * Computes the reliability of the system. It uses the closed form model defined
     * in:
     * <p>
     * Cortellessa, V., Grassi, V. (2007). A Modeling Approach to Analyze the Impact
     * of Error Propagation on Reliability of Component-Based Systems. In:
     * Component-Based Software Engineering. CBSE 2007. Lecture Notes in Computer
     * Science, vol 4608. Springer, Berlin, Heidelberg.
     * https://doi.org/10.1007/978-3-540-73551-9_10
     * </p>
     *
     * @param modelPath the path of the UML model
     * @return the system reliability
     * @throws MissingTagException when the reliability cannot be computed due to a
     *                             not well-formed UML model
     */
    public static double reliability(Path modelPath) throws EasierException {
        EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "computeReliability_start");
        // stores the in memory model to a file
        UMLReliability uml = null;
        try {
            uml = new UMLReliability(new UMLModelPapyrus(modelPath.toString()).getModel());
            double reliability = new Reliability(uml.getScenarios()).compute();

            ResourceSet rs = uml.getModel().eResource().getResourceSet();
            while (!rs.getResources().isEmpty()) {
                Resource res = rs.getResources().get(0);
                res.eAdapters().clear();
                res.unload();
                rs.getResources().remove(res);
            }
            new UMLMemoryOptimizer().cleanup();
            EasierLogger.logger_.info(String.format("Reliability : %s", reliability));
            EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "computeReliability_end");
            // It must be minimized
            return -1 * reliability;
        } catch (MissingTagException e) {
            EasierLogger.logger_.severe(
                    "Error in computing the reliability on " + modelPath + ". The reason is: " + e.getMessage());
            throw new EasierException(
                    "Error in computing the reliability on " + modelPath + ". The reason is: " + e.getMessage());
        }
    }

    /**
     * Compute the refactoring cost of the solution. The refactoring cost is
     * computed as the sum of the refactoring cost of each action multiplied by the
     * BRF of the action.
     *
     * <p>
     * Cortellessa, Vittorio, Daniele Di Pompeo, Vincenzo Stoico, and Michele Tucci.
     * "Many-objective optimization of non-functional attributes based on
     * refactoring of software models." Information and Software Technology 157
     * (2023): 107159.
     * <a href="https://doi.org/10.1016/j.infsof.2023.107159">doi</a>
     * </p>
     *
     * @param solution
     * @return
     */
    public static double refactoringCost(RSolution<?> solution) {
        EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "computeArchitecturalChanges_start");

        double refactoringCost = 0d;

        refactoringCost += Configurator.eINSTANCE.getInitialChanges();

        for (RefactoringAction action : solution.getVariable(RSolution.VARIABLE_INDEX).getActions()) {

            double brf = Configurator.eINSTANCE.getBRF(action.getName());
            double aw = action.getRefactoringCost();

            refactoringCost += brf * aw;
        }

        EasierLogger.logger_.info(String.format("Refactoring Cost : %s", refactoringCost));
        EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "computeArchitecturalChanges_end");
        return refactoringCost;
    }

    /**
     * Estimate the system power consumption. K is Power_idle / Power_max. The
     * default value is 0.66 as suggested in
     * <p>
     * Gong Chen, Wenbo He, Jie Liu, Suman Nath, Leonidas Rigas, Lin Xiao, and Feng
     * Zhao. 2008. Energy-aware server provisioning and load dispatching for
     * connection-intensive internet services. In Proceedings of the 5th USENIX
     * Symposium on Networked Systems Design and Implementation (NSDI'08). USENIX
     * Association, USA, 337–350.
     * <a hfre="https://dl.acm.org/doi/10.5555/1387589.1387613">doi</a>
     * </p>
     *
     * @param modelPath the path of the UML model
     * @return the system power consumption
     */
    public static double powerEstimator(Path modelPath) throws EasierException {
        double power;
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluatePower_start");

        String umlFile = modelPath.toString();
        String lqxoFile = modelPath.getParent().resolve("output.lqxo").toString();
        power = Energy.computeSystemPower(umlFile, lqxoFile, Configurator.eINSTANCE.getPowerRatioIdleMax());

        EasierLogger.logger_.info(String.format("System Power : %s", power));
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluatePower_end");
        return power;
    }

    /**
     * Compute the economic cost of the system. The economic cost is computed as the
     * sum of the price of the processors multiplied by the utilization of the
     * processors.
     *
     * <p>
     * Cortellessa, Vittorio, Daniele Di Pompeo, and Michele Tucci. "Exploring
     * sustainable alternatives for the deployment of microservices architectures in
     * the cloud." In 2024 IEEE 21st International Conference on Software
     * Architecture (ICSA), pp. 34-45. IEEE, 2024.
     * <a href="https://doi.org/10.1109/ICSA59870.2024.00012">doi</a>
     * </p>
     *
     * @param modelPath
     * @return
     */
    public static double economicCost(Path modelPath) {
        double cost;
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluateEconomicCost_start");
        try (EasierUmlModel uml = EpsilonStandalone.createUmlModel(modelPath.toString())) {
            cost = uml.computeEconomicCost();
            EasierLogger.logger_.info(String.format("Economic Cost : %s", cost));
            EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluateEconomicCost_end");
            return cost;
        } catch (EolModelElementTypeNotFoundException | EolModelLoadingException | URISyntaxException e) {
            EasierLogger.logger_.severe(String.format("Error in computing the economic cost on %s because of: %s",
                    modelPath, e.getMessage()));
            EasierLogger.logger_.info("The cost is set to Double.MAX_VALUE");
            return Double.MAX_VALUE;
        }
    }

    public static List<String> getScenarios(Path modelPath) {
        Path lqxoFile = modelPath.getParent().resolve("output.lqxo");

        LQN lqn = new LQN(lqxoFile.toString());
        Map<String, Scenario> entriesByScenario = lqn.getEntriesByScenario();

        return new ArrayList<>(entriesByScenario.keySet());
    }

    public static Map<String, Double> pricePerScenario(Path modelPath) {
        Path lqxoFile = modelPath.getParent().resolve("output.lqxo");

        LQN lqn = new LQN(lqxoFile.toString());
        UML uml = new UML(modelPath.toString());

        return Profiler.computePriceProfile(uml, lqn);
    }

    public static Map<String, Double> energyPerScenario(Path modelPath) {
        Path lqxoFile = modelPath.getParent().resolve("output.lqxo");

        LQN lqn = new LQN(lqxoFile.toString());
        UML uml = new UML(modelPath.toString());

        return Profiler.computeEnergyProfile(uml, lqn, Configurator.eINSTANCE.getPowerRatioIdleMax());
    }

    public static void initObjectives(RSolution<?> solution) {
        solution.getMapOfObjectives().put(Configurator.PAS_LABEL, 0d);
        solution.getMapOfObjectives().put(Configurator.RELIABILITY_LABEL, 0d);
        solution.getMapOfObjectives().put(Configurator.CHANGES_LABEL, 0d);
        solution.getMapOfObjectives().put(Configurator.PERF_Q_LABEL, 0d);
        solution.getMapOfObjectives().put(Configurator.SYS_RESP_T_LABEL, 0d);
        solution.getMapOfObjectives().put(Configurator.ENERGY_LABEL, 0d);
        solution.getMapOfObjectives().put(Configurator.POWER_LABEL, 0d);
        solution.getMapOfObjectives().put(Configurator.ECONOMIC_COST_LABEL, 0d);
    }

    /**
     * Compute all available objectives of the solution. Then, the Problem::evaluate
     * will select the ones to be used. The objectives that should be maximized are
     * negated by the proper method.
     *
     * @param solution: the solution to be evaluated
     */
    public static void computeObjectives(RSolution<?> solution) throws EasierException {

        solution.getMapOfObjectives().put(Configurator.PAS_LABEL,
                ObjectiveEstimator.countPerformanceAntipattern(solution.getModelPath(), solution.getName()));
        solution.getMapOfObjectives().put(Configurator.RELIABILITY_LABEL,
                ObjectiveEstimator.reliability(solution.getModelPath()));
        solution.getMapOfObjectives().put(Configurator.CHANGES_LABEL, ObjectiveEstimator.refactoringCost(solution));
        solution.getMapOfObjectives().put(Configurator.PERF_Q_LABEL,
                ObjectiveEstimator.perfQ(solution.getSourceModelPath(), solution.getModelPath()));
        solution.getMapOfObjectives().put(Configurator.SYS_RESP_T_LABEL,
                ObjectiveEstimator.systemResponseTime(solution.getModelPath()));
        solution.getMapOfObjectives().put(Configurator.ENERGY_LABEL,
                ObjectiveEstimator.energyEstimation(solution.getModelPath()));
        solution.getMapOfObjectives().put(Configurator.POWER_LABEL,
                ObjectiveEstimator.powerEstimator(solution.getModelPath()));
        solution.getMapOfObjectives().put(Configurator.ECONOMIC_COST_LABEL,
                ObjectiveEstimator.economicCost(solution.getModelPath()));

        // Dynamically append the energy and price per scenario to the objectives
        Map<String, Double> energyPerScenario = ObjectiveEstimator.energyPerScenario(solution.getModelPath());
        Map<String, Double> pricePerScenario = ObjectiveEstimator.pricePerScenario(solution.getModelPath());
        Map<String, Double> responseTimePerScenario = ObjectiveEstimator
                .responseTimePerScenario(solution.getModelPath());

        for (String scenario : getScenarios(solution.getModelPath())) {
            solution.getMapOfObjectives().put(Configurator.ENERGY_PER_SCENARIO_LABEL + "__" + scenario,
                    energyPerScenario.get(scenario));
            solution.getMapOfObjectives().put(Configurator.ECONOMIC_COST_PER_SCENARIO_LABEL + "__" + scenario,
                    pricePerScenario.get(scenario));
            solution.getMapOfObjectives().put(Configurator.RESP_T_PER_SCENARIO_LABEL + "__" + scenario,
                    responseTimePerScenario.get(scenario));
        }

        EasierLogger.logger_.info(String.format("Solution id: # %d has been evaluated: %s", solution.getName(),
                solution.getMapOfObjectives()));
    }

    /**
     * Set the objectives of the solution. The objectives are set according to the
     * order defined in the configuration file.
     *
     * @param solution: the solution to set the objectives
     */
    public static void setConsideredObjectives(RSolution<?> solution) throws EasierObjectiveNotFoundException {
        List<String> objectives = Configurator.eINSTANCE.getObjectivesList();
        Map<String, Double> mapOfObjectives = solution.getMapOfObjectives();

        // Exact match labels
        Set<String> knownExactLabels = Set.of(Configurator.PERF_Q_LABEL, Configurator.SYS_RESP_T_LABEL,
                Configurator.CHANGES_LABEL, Configurator.RELIABILITY_LABEL, Configurator.ENERGY_LABEL,
                Configurator.PAS_LABEL, Configurator.POWER_LABEL, Configurator.ECONOMIC_COST_LABEL);

        for (int i = 0; i < objectives.size(); i++) {
            String obj = objectives.get(i);
            Double value = null;

            // TODO verify if the else branch is necessary
            if (knownExactLabels.contains(obj)) {
                value = mapOfObjectives.get(obj);
            } else if (obj.startsWith(Configurator.ENERGY_PER_SCENARIO_LABEL)
                    || obj.startsWith(Configurator.ECONOMIC_COST_PER_SCENARIO_LABEL)
                    || obj.startsWith(Configurator.RESP_T_PER_SCENARIO_LABEL)) {
                value = mapOfObjectives.get(obj);
            }

            if (value != null) {
                solution.setObjective(i, value);
            } else {
                EasierLogger.logger_.severe(String.format("Objective '%s' not recognized.", obj));
                throw new EasierObjectiveNotFoundException(
                        String.format("Objective %s not recognized for solution: %s", obj, solution.getName()));
            }
        }

        EasierLogger.logger_.info(String.format("Objectives of Solution # %s have been set.", solution.getName()));
    }

    public static void surrogateEvaluation(List<RSolution<?>> solutionList, int iteration, String caseStudyName) {

        EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "surrogateEvaluation_start");

        // Setup for HTTP client
        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();


        try {
            String jsonSolutions = objectMapper.writeValueAsString(new EasierPopulationDAO(solutionList, iteration));
            jsonSolutions = String.format("{\"caseStudy\":\"%s\",\"population\":%s}", caseStudyName,
                    jsonSolutions);

            EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "surrogateServerRequest_start");
            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Configurator.eINSTANCE.getSurrogateEndpoint()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonSolutions))
                    .build();

            // Send request and get response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "surrogateServerRequest_end");

            // Check if request was successful
            if (response.statusCode() == 200) {
                EasierLogger.logger_.info("Surrogate evaluation completed successfully");

                // Parse response and update solution objectives
                JsonNode rootNode = objectMapper.readTree(response.body());
                JsonNode populationNode = rootNode.get("population");
                JsonNode solutionsNode = populationNode.get("solutions");
                for (JsonNode solutionNode : solutionsNode) {
                    if (solutionNode.get("markedForSurrogate").booleanValue()) {
                        int solutionId = solutionNode.get("solID").asInt();

                        // Find matching solution in the list
                        for (RSolution<?> solution : solutionList) {
                            if (solution.getName() == solutionId) {
                                // Update solution objectives from surrogate model
                                JsonNode objectivesNode = solutionNode.get("objectives");
                                Iterator<String> fieldNames = objectivesNode.fieldNames();
                                while (fieldNames.hasNext()) {
                                    String objectiveName = fieldNames.next();
                                    double objectiveValue = objectivesNode.get(objectiveName).asDouble();
                                    solution.getMapOfObjectives().put(objectiveName, objectiveValue);
                                }
                                setConsideredObjectives(solution);
                            }
                        }
                    }
                }
            } else {
                EasierLogger.logger_.severe("Surrogate evaluation failed with status code: " + response.statusCode());
                EasierLogger.logger_.severe("Response body: " + response.body());
            }
            EasierResourcesLogger.checkpoint(ObjectiveEstimator.class.getSimpleName(), "surrogateEvaluation_end");
        } catch (IOException | InterruptedException e) {
            EasierLogger.logger_.severe("Error during surrogate evaluation: " + e.getMessage());
        } catch (EasierObjectiveNotFoundException e) {
            EasierLogger.logger_.severe("Error during setting considered objective from surrogate solution: " + e.getMessage());
        }

    }

}
