package it.univaq.disim.sealab.metaheuristic.utils;

import it.univaq.disim.sealab.epsilon.EpsilonStandalone;
import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.epsilon.etl.ETLStandalone;
import it.univaq.disim.sealab.epsilon.evl.EVLStandalone;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.sealab.umlreliability.MissingTagException;
import it.univaq.sealab.umlreliability.Reliability;
import it.univaq.sealab.umlreliability.UMLReliability;
import it.univaq.sealab.umlreliability.model.UMLModelPapyrus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.xsd.ecore.XSDEcoreBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorkflowUtils {

    /**
     * Invokes the ETL engine in order to run the UML2LQN transformation.
     */
    public static void applyTransformation(Path sourceModelPath) throws EasierException {
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "applyTransformation_start");

        ETLStandalone executor;
        final String uml2lqnModule = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-uml2lqn").toString();

        try (EasierUmlModel uml = EpsilonStandalone.createUmlModel(sourceModelPath.toString())) {

            executor = new ETLStandalone(sourceModelPath.getParent());
            executor.setSource(Paths.get(uml2lqnModule, "uml2lqn.etl"));
            executor.setModel(uml);
            executor.setModel(executor.createXMLModel("LQN", sourceModelPath.getParent().resolve("output.xml"),
                    org.eclipse.emf.common.util.URI.createFileURI(
                            Paths.get(uml2lqnModule, "lqnxsd", "lqn.xsd").toString()),
                    false, true));
            executor.execute();
            executor.clearMemory();
        } catch (EolRuntimeException | URISyntaxException e) {
            throw new EasierException(String.format("Error in running the ETL transformation on %s for the following " +
                            "reason: %s",
                    sourceModelPath.getParent().resolve("output.xml"), e.getMessage()));
        }

        new UMLMemoryOptimizer().cleanup();
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "applyTransformation_start");
        EasierLogger.logger_.info("UML2LQN done");
    }

    public static void invokeSolver(Path folderPath) throws EasierException, LQNException {
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "invokeSolver_start");
        Path lqnSolverPath = Configurator.eINSTANCE.getSolver();
        Path lqnModelPath = folderPath.resolve("output.xml");

        XMLUtil.conformanceChecking(lqnModelPath);

        // Allow cycles in the lqn model as well as support MVA convergence faults
        final List<String> command =
                List.of(lqnSolverPath.toString(), "-P", "cycles=yes", "-P", "stop-on-message-loss=false",
                        lqnModelPath.toString());

        Process process;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            process = pb.start();
            process.waitFor();

            if (!Files.exists(folderPath.resolve("output.lqxo"))) {
                // Catch the error stream of the process
                final String lqnError = new BufferedReader(new InputStreamReader(process.getErrorStream())).lines()
                        .collect(Collectors.joining(","));
                throw new LQNException(String.format("LQN solver cannot solve the model: %s. The reason is: %s",
                    lqnModelPath, lqnError));
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EasierException(String.format("LQN solver cannot solve the model: %s. The reason is: %s",
                    lqnModelPath, e.getMessage()), e);
        }
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "invokeSolver_end");
        EasierLogger.logger_.info("LQN solver invoked on " + folderPath.getFileName().toString());
    }

    public static void backAnnotation(Path modelPath) throws URISyntaxException, EolRuntimeException {

        EOLStandalone bckAnn = new EOLStandalone();
        EasierUmlModel uml = EpsilonStandalone.createUmlModel(modelPath.toString());

        bckAnn.setModel(uml);

        final String uml2lqnModule = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-uml2lqn").toString();

        bckAnn.setSource(Paths.get(uml2lqnModule, "backAnnotation.eol"));

        // Points to lqn schema file and stores pacakges into the global package
        // registry
        XSDEcoreBuilder xsdEcoreBuilder = new XSDEcoreBuilder();
        String schema = Paths.get(uml2lqnModule, "lqnxsd", "lqn.xsd").toString();
        Collection<EObject> generatedPackages = xsdEcoreBuilder
                .generate(org.eclipse.emf.common.util.URI.createURI(schema));
        for (EObject generatedEObject : generatedPackages) {
            if (generatedEObject instanceof EPackage) {
                EPackage generatedPackage = (EPackage) generatedEObject;
                EPackage.Registry.INSTANCE.put(generatedPackage.getNsURI(), generatedPackage);
            }
        }
        bckAnn.setModel(bckAnn.createPlainXMLModel("LQXO", modelPath.getParent().resolve("output.lqxo"), true,
                false, true));

        bckAnn.execute();

        bckAnn.clearMemory();
        new UMLMemoryOptimizer().cleanup();

        EasierLogger.logger_.info(String.format("UML model %s back annotated", modelPath.getFileName().toString()));

    }

    /**
     * This method counts the number of Performance Antipatterns (PAs) invoking
     * the PADRE perf-detection file
     */
    public static int countPerformanceAntipattern(Path sourceModelPath, int solutionID) throws EasierException {
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "countingPAs_start");

        String refactoringLibraryModule =
                Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                        "easier-refactoringLibrary", "evl", "AP-UML-MARTE.evl").toString();

        Map<String, Map<String, Double>> extractFuzzyValues = new HashMap<>();
        int numPAs = 0;

        EVLStandalone pasCounter = new EVLStandalone();
        try (EasierUmlModel uml = EpsilonStandalone.createUmlModel(sourceModelPath.toString())) {
            pasCounter.setModel(uml);

            pasCounter.setSource(Paths.get(refactoringLibraryModule));

            // set the prob to be perf antipatterns
            double fuzzyThreshold = Configurator.eINSTANCE.getProbPas();
            pasCounter.setParameter(fuzzyThreshold, "float", "prob_to_be_pa");

            extractFuzzyValues = pasCounter.extractFuzzyValues();
        } catch (EolModelLoadingException | URISyntaxException e) {
//            EasierLogger.logger_.severe(String.format("Solution: #%s has thrown an error while loading the model: %s.",
//                    solutionID, sourceModelPath));
//            EasierLogger.logger_.severe(e.getMessage());
            throw new EasierException(e);
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
     * @throws EasierException when the perfQ cannot be computed
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
            if (perfQ == Double.MAX_VALUE) {
                EasierLogger.logger_.severe(String.format("PerfQ is %s because no performance index has been " +
                        "computed.", Double.MAX_VALUE));
                throw new EasierException("PerfQ is " + Double.MAX_VALUE);
            }

            new UMLMemoryOptimizer().cleanup();
            EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluatePerformance_end");
            EasierLogger.logger_.info(String.format("PerfQ : %s", perfQ));
            return perfQ;
        } catch (URISyntaxException | EolModelLoadingException | EolModelElementTypeNotFoundException e) {
            throw new EasierException("PerfQ cannot be computed because of: "+ e.getMessage());
        }
    }

    /**
     * It computes the system response time of the model.
     * The system response time is the sum of the response time of all the scenarios
     *
     * @param modelPath the path of the model
     * @return the system response time
     * @throws EasierException when the system response time cannot be computed
     */
    public static double systemResponseTime(Path modelPath) throws EasierException {
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluatePerformance_start");
        try (EasierUmlModel model = EpsilonStandalone.createUmlModel(modelPath.toString())) {
            double sysRespT = model.computeSystemResponseTime();
            new UMLMemoryOptimizer().cleanup();
            EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluatePerformance_end");
            EasierLogger.logger_.info(String.format("System RespT : %s", sysRespT));

            return sysRespT;
        } catch (URISyntaxException | EolModelLoadingException | EolModelElementTypeNotFoundException e) {
            throw new EasierException("Error while computing the System RespT on:  " + modelPath, e);
        }
    }

    public static double energyEstimation(Path modelPath) throws EasierException {
        double energy;
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluateEnergy_start");

        try (EasierUmlModel model = EpsilonStandalone.createUmlModel(modelPath.toString())) {
            energy = model.computeEnergy();

            EasierLogger.logger_.info(String.format("System Energy : %s", energy));
            EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "evaluateEnergy_end");
            return energy;
        } catch (URISyntaxException | EolModelLoadingException e) {
            throw new EasierException(e);
        }
    }

    public static double reliability(Path modelPath) throws MissingTagException {
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "computeReliability_start");
        // stores the in memory model to a file
        UMLReliability uml = new UMLReliability(new UMLModelPapyrus(modelPath.toString()).getModel());
        double reliability = new Reliability(uml.getScenarios()).compute();

        ResourceSet rs = uml.getModel().eResource().getResourceSet();
        while (rs.getResources().size() > 0) {
            Resource res = rs.getResources().get(0);
            res.eAdapters().clear();
            res.unload();
            rs.getResources().remove(res);
        }

        new UMLMemoryOptimizer().cleanup();
        EasierLogger.logger_.info(String.format("Reliability : %s", reliability));
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "computeReliability_end");
        return reliability;
    }

    public static double refactoringCost(RSolution<?> solution) throws EasierException {
        EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "computeArchitecturalChanges_start");

        double refactoringCost = 0d;
        try (EasierUmlModel model = EOLStandalone.createUmlModel(solution.getModelPath().toString())) {

            refactoringCost += Configurator.eINSTANCE.getInitialChanges();

            for (RefactoringAction action : solution.getVariable(RSolution.VARIABLE_INDEX).getActions()) {

                double brf = Configurator.eINSTANCE.getBRF(action.getName());
                double aw = action.getRefactoringCost();

                refactoringCost += brf * aw;
            }

            EasierLogger.logger_.info(String.format("Refactoring Cost : %s", refactoringCost));
            EasierResourcesLogger.checkpoint(WorkflowUtils.class.getSimpleName(), "computeArchitecturalChanges_end");
            return refactoringCost;
        } catch (URISyntaxException | EolModelLoadingException e) {
            throw new EasierException(e);
        }
    }
}