package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import it.univaq.disim.sealab.epsilon.EasierModelElementNotFoundException;
import it.univaq.disim.sealab.epsilon.EasierStereotypeNotPropertlyAppliedException;
import it.univaq.disim.sealab.epsilon.EpsilonStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.epsilon.evl.EVLStandalone;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.*;
import it.univaq.sealab.umlreliability.MissingTagException;
import it.univaq.sealab.umlreliability.Reliability;
import it.univaq.sealab.umlreliability.UMLReliability;
import it.univaq.sealab.umlreliability.model.UMLModelPapyrus;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;

import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectiveEstimator {

    /**
     * This method counts the number of Performance Antipatterns (PAs) invoking
     * the PADRE perf-detection file
     */
    public static double countPerformanceAntipattern(Path sourceModelPath, int solutionID) {
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "countingPAs_start");

        String refactoringLibraryModule =
                Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                        "easier-refactoringLibrary", "evl", "AP-UML-MARTE.evl").toString();

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
//            throw new EasierException(e);
            EasierLogger.logger_.severe(
                    String.format("Solution: #%s has thrown an error when computing the pas on: %s " +
                            "because of: %s", solutionID, sourceModelPath, e.getMessage()));
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

        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "countingPAs_end");
        EasierLogger.logger_.info(String.format("Performance antipatterns : %s", numPAs));
        return numPAs;

    }

    /**
     * @return the performance quality indicator as described in
     * <a href="https://doi.org/10.1109/ICSA.2018.00020">https://doi.org/10.1109/ICSA.2018.00020</a>
     */
    public static double perfQ(Path sourceModelPath, Path modelPath) throws EasierException {

        /*
         * The updated model can have more nodes than the source node since original
         * nodes can be cloned. The benefits of cloning nodes is taken into account by
         * the performance model. For this reason, the perfQ analyzes only the
         * performance metrics of the nodes common among the models
         */
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluatePerformance_start");
        try (EasierUmlModel source = EpsilonStandalone.createUmlModel(sourceModelPath.toString());
             EasierUmlModel uml = EpsilonStandalone.createUmlModel(modelPath.toString())) {

            double perfQ = source.computePerfQ(uml);
//            if (perfQ == Double.MAX_VALUE) {
//                EasierLogger.logger_.severe(String.format("PerfQ is %s because no performance index has been " +
//                        "computed.", Double.MAX_VALUE));
//                throw new EasierException();
//                return -1 * Double.MAX_VALUE;
//            }

            new UMLMemoryOptimizer().cleanup();
            EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluatePerformance_end");
            EasierLogger.logger_.info(String.format("PerfQ : %s", perfQ));
            // It must be minimized
            return -1 * perfQ;
        } catch (URISyntaxException | EolModelLoadingException | EolModelElementTypeNotFoundException |
                 EasierModelElementNotFoundException e) {
            EasierLogger.logger_.severe(String.format("PerfQ cannot be computed on %s because of: %s",
                    modelPath, e.getMessage()));
            EasierLogger.logger_.info("PerfQ is set to -1 * Double.MAX_VALUE");
            throw new EasierException(String.format("PerfQ cannot be computed on %s because of: %s",
                    modelPath, e.getMessage()));
//            return -1 * Double.MAX_VALUE;
        }
    }

    /**
     * It computes the system response time of the model.
     * The system response time is the sum of the response time of all the scenarios
     *
     * @param modelPath the path of the UML model
     * @return the system response time
     */
    public static double systemResponseTime(Path modelPath) throws EasierException {
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluatePerformance_start");
        try (EasierUmlModel model = EpsilonStandalone.createUmlModel(modelPath.toString())) {
            double sysRespT = model.computeSystemResponseTime();
            new UMLMemoryOptimizer().cleanup();
            EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluatePerformance_end");
            EasierLogger.logger_.info(String.format("System RespT : %s", sysRespT));

            return sysRespT;
        } catch (URISyntaxException | EolModelLoadingException | EolModelElementTypeNotFoundException | EasierStereotypeNotPropertlyAppliedException e) {
            EasierLogger.logger_.severe(String.format("Error while computing the System RespT on: %s for the reason: %s",
                    modelPath, e.getMessage()));
            EasierLogger.logger_.info("System response time is set to Double.MAX_VALUE");
            throw new EasierException(String.format("Error while computing the System RespT on: %s for the reason: %s",
                    modelPath, e.getMessage()));
        }
    }

    /**
     * Estimate the system energy consumption as defined in
     * <p>
     *     Stoico, V., Cortellessa, V., Malavolta, I., Di Pompeo, D., Pomante, L., Lago, P. (2023).
     *     An Approach Using Performance Models for Supporting Energy Analysis of Software Systems.
     *     In: Computer Performance Engineering and Stochastic Modelling. EPEW ASMTA 2023.
     *     Lecture Notes in Computer Science, vol 14231. Springer, Cham.
     *     https://doi.org/10.1007/978-3-031-43185-2_17
     * </p>
     * @param modelPath the path of the UML model
     * @return the system energy consumption
     * @throws EasierException when the system energy cannot be computed
     */
    public static double energyEstimation(Path modelPath) throws EasierException {
        double energy;
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluateEnergy_start");

        String umlFile = modelPath.toString();
        String lqxoFile = modelPath.getParent().resolve("output.lqxo").toString();
        energy = Energy.computeSystemEnergy(umlFile, lqxoFile);

        EasierLogger.logger_.info(String.format("System Energy : %s", energy));
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluateEnergy_end");
        return energy;
    }

    /**
     * Computes the reliability of the system. It uses the closed form model defined in:
     * <p>
     *     Cortellessa, V., Grassi, V. (2007).
     *     A Modeling Approach to Analyze the Impact of Error Propagation on Reliability of Component-Based Systems.
     *     In: Component-Based Software Engineering. CBSE 2007.
     *     Lecture Notes in Computer Science, vol 4608. Springer, Berlin, Heidelberg.
     *     https://doi.org/10.1007/978-3-540-73551-9_10
     * </p>
     * @param modelPath the path of the UML model
     * @return the system reliability
     * @throws MissingTagException when the reliability cannot be computed due to a not well-formed UML model
     */
    public static double reliability(Path modelPath) throws EasierException {
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "computeReliability_start");
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
            EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "computeReliability_end");
            // It must be minimized
            return -1 * reliability;
        } catch (MissingTagException e) {
            EasierLogger.logger_.severe( "Error in computing the reliability on " + modelPath + ". The reason is: " + e.getMessage());
            throw new EasierException( "Error in computing the reliability on " + modelPath + ". The reason is: " + e.getMessage());
        }
    }

    public static double refactoringCost(RSolution<?> solution) {
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "computeArchitecturalChanges_start");

        double refactoringCost = 0d;

        refactoringCost += Configurator.eINSTANCE.getInitialChanges();

        for (RefactoringAction action : solution.getVariable(RSolution.VARIABLE_INDEX).getActions()) {

            double brf = Configurator.eINSTANCE.getBRF(action.getName());
            double aw = action.getRefactoringCost();

            refactoringCost += brf * aw;
        }

        EasierLogger.logger_.info(String.format("Refactoring Cost : %s", refactoringCost));
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "computeArchitecturalChanges_end");
        return refactoringCost;
    }

    /**
     * Estimate the system power consumption.
     * K is Power_idle / Power_max. The default value is 0.66 as suggested in
     * <p>
     * Gong Chen, Wenbo He, Jie Liu, Suman Nath, Leonidas Rigas, Lin Xiao, and Feng Zhao. 2008. Energy-aware server
     * provisioning and load dispatching for connection-intensive internet services.
     * In Proceedings of the 5th USENIX Symposium on Networked Systems Design and Implementation (NSDI'08).
     * USENIX Association, USA, 337–350.
     * https://dl.acm.org/doi/10.5555/1387589.1387613
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

    /**
     * Compute all available objectives of the solution.
     * Then, the Problem::evaluate will select the ones to be used.
     * The objectives that should be maximized are negated by the proper method.
     *
     */
    public void computeObjectives(RSolution<?> solution) throws EasierException {

        solution.getMapOfObjectives().put(Configurator.PAS_LABEL,
                ObjectiveEstimator.countPerformanceAntipattern(solution.getModelPath(), solution.getName()));
        solution.getMapOfObjectives().put(Configurator.RELIABILITY_LABEL, ObjectiveEstimator.reliability(solution.getModelPath()));
        solution.getMapOfObjectives().put(Configurator.CHANGES_LABEL, ObjectiveEstimator.refactoringCost(solution));
        solution.getMapOfObjectives().put(Configurator.PERF_Q_LABEL, ObjectiveEstimator.perfQ(solution.getSourceModelPath(), solution.getModelPath()));
        solution.getMapOfObjectives().put(Configurator.SYS_RESP_T_LABEL, ObjectiveEstimator.systemResponseTime(solution.getModelPath()));
        solution.getMapOfObjectives().put(Configurator.ENERGY_LABEL, ObjectiveEstimator.energyEstimation(solution.getModelPath()));
        solution.getMapOfObjectives().put(Configurator.POWER_LABEL, ObjectiveEstimator.powerEstimator(solution.getModelPath()));
        solution.getMapOfObjectives().put(Configurator.ECONOMIC_COST, ObjectiveEstimator.economicCost(solution.getModelPath()));

        EasierLogger.logger_.info(String.format("Solution id: # %d has been evaluated: %s", solution.getName(), solution.getMapOfObjectives()));
    }


    public void setConsideredObjectives(RSolution<?> solution) {

        List<String> objectives = Configurator.eINSTANCE.getObjectivesList();

        for (String obj : objectives) {
            int index = objectives.indexOf(obj);
            switch (obj) {
                case Configurator.PERF_Q_LABEL:
                    solution.setObjective(index, solution.getMapOfObjectives().get(Configurator.PERF_Q_LABEL));
                    break;
                case Configurator.SYS_RESP_T_LABEL:
                    solution.setObjective(index, solution.getMapOfObjectives().get(Configurator.SYS_RESP_T_LABEL));
                    break;
                case Configurator.CHANGES_LABEL:
                    solution.setObjective(index, solution.getMapOfObjectives().get(Configurator.CHANGES_LABEL));
                    break;
                case Configurator.RELIABILITY_LABEL:
                    solution.setObjective(index, solution.getMapOfObjectives().get(Configurator.RELIABILITY_LABEL));
                    break;
                case Configurator.ENERGY_LABEL:
                    solution.setObjective(index, solution.getMapOfObjectives().get(Configurator.ENERGY_LABEL));
                    break;
                case Configurator.PAS_LABEL:
                    solution.setObjective(index, solution.getMapOfObjectives().get(Configurator.PAS_LABEL));
                    break;
                case Configurator.POWER_LABEL:
                    solution.setObjective(index, solution.getMapOfObjectives().get(Configurator.POWER_LABEL));
                    break;
                case Configurator.ECONOMIC_COST:
                    solution.setObjective(index, solution.getMapOfObjectives().get(Configurator.ECONOMIC_COST));
                    break;
                default:
                    EasierLogger.logger_.severe(String.format("Objective '%s' not recognized.", obj));
                    break;
            }
        }
        EasierLogger.logger_.info(String.format("Objectives of Solution # %s have been set.", solution.getName()));
    }

}
