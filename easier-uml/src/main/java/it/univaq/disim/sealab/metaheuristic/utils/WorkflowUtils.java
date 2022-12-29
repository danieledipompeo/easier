package it.univaq.disim.sealab.metaheuristic.utils;

import it.univaq.disim.sealab.epsilon.EpsilonStandalone;
import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.epsilon.etl.ETLStandalone;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.xsd.ecore.XSDEcoreBuilder;
import org.uma.jmetal.util.JMetalLogger;

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


    public WorkflowUtils() {
    }

    public void applyTransformation(Path sourceModelPath) {

        ETLStandalone executor;
        String uml2lqnModule = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-uml2lqn").toString();

        try (EasierUmlModel uml = EpsilonStandalone.createUmlModel(sourceModelPath.toString())) {

            executor = new ETLStandalone();
            executor.setSource(Paths.get(uml2lqnModule, "uml2lqn.etl"));
            executor.setModel(uml);
            executor.setModel(executor.createXMLModel("LQN", sourceModelPath.getParent().resolve("output.xml"),
                    org.eclipse.emf.common.util.URI.createFileURI(
                            Paths.get(uml2lqnModule, "lqnxsd", "lqn.xsd").toString()),
                    false, true));
            executor.execute();
            executor.clearMemory();
        } catch (EolModelLoadingException | URISyntaxException e) {
            System.err.println("Error in runnig the ETL transformation");
            e.printStackTrace();
        } catch (EolRuntimeException e) {
            throw new RuntimeException(e);
        }
        new UMLMemoryOptimizer().cleanup();
        JMetalLogger.logger.info("UML2LQN done");
    }

    public void invokeSolver(Path folderPath) throws Exception {

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
                final String lqnError = new BufferedReader(new InputStreamReader(process.getErrorStream())).lines()
                        .map(act -> act.toString()).collect(Collectors.joining(","));
                throw new Exception(lqnError);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            Thread.currentThread().interrupt();
        }
        JMetalLogger.logger.info("LQN solver invoked");
    }

    public void backAnnotation(Path sourceModelPath) throws URISyntaxException, EolRuntimeException {

        EOLStandalone bckAnn = new EOLStandalone();
        EasierUmlModel uml;

        uml = EpsilonStandalone.createUmlModel(sourceModelPath.toString());

        bckAnn.setModel(uml);

        String uml2lqnModule = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
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
        bckAnn.setModel(bckAnn.createPlainXMLModel("LQXO", sourceModelPath.getParent().resolve("output.lqxo"), true,
                false, true));

        bckAnn.execute();

        bckAnn.clearMemory();
        new UMLMemoryOptimizer().cleanup();

        JMetalLogger.logger.info("UML model back annotated");

    }

}
