package it.univaq.disim.sealab.metaheuristic.utils;

import it.univaq.disim.sealab.epsilon.EpsilonStandalone;
import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.epsilon.etl.ETLStandalone;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
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
import java.util.List;
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
            throw new EasierException(String.format("Failed execution of the LQN solver on the model: %s. The " +
                            "reason is: %s", lqnModelPath, e.getMessage()), e);
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

}