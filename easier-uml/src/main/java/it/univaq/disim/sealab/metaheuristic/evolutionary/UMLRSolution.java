package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.actions.UMLRefactoring;
import it.univaq.disim.sealab.metaheuristic.utils.*;
import it.univaq.sealab.umlreliability.MissingTagException;
import it.univaq.sealab.umlreliability.Reliability;
import it.univaq.sealab.umlreliability.UMLReliability;
import it.univaq.sealab.umlreliability.model.UMLModelPapyrus;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * @author Daniele Di Pompeo
 * daniele.dipompeo@univaq.it
 */
public class UMLRSolution extends RSolution<Refactoring> {

    public static int FAILED_CROSSOVER = 0;

    private Path folderPath;
    private double[] scenarioRTs;
    private String algorithm;

    public UMLRSolution(Path sourceModelPath, String problemName) {
        super(sourceModelPath, problemName);
        init();
    }

    public UMLRSolution(UMLRSolution s) {
        this(s.sourceModelPath, s.problemName);

        // create a new refactoring and clone refactoring actions from the source solution
        Refactoring ref = new UMLRefactoring(this.getModelPath().toString());
        ref.setSolutionID(this.getName());
        ref.getActions().addAll(s.getVariable(0).getActions().stream().map(RefactoringAction::clone)
                .collect(Collectors.toList()));
        this.setVariable(0, ref);

        this.perfQ = s.perfQ;
        this.reliability = s.reliability;
        this.architecturalChanges = s.architecturalChanges;
        this.numPAs = s.numPAs;

        for (int i = 0; i < s.getNumberOfObjectives(); i++) {
            this.setObjective(i, s.getObjective(i));
        }

        this.attributes = s.attributes;
        this.setAttribute(CrowdingDistance.class, s.getAttribute(CrowdingDistance.class));

    }

    protected void init() {

        parents = new UMLRSolution[2];
        scenarioRTs = new double[3];

        this.setName();

        folderPath = Paths.get(Configurator.eINSTANCE.getTmpFolder().toString(), String.valueOf((getName() / 100)),
                String.valueOf(getName()));
        modelPath = folderPath.resolve(getName() + ".uml");
//        initialModelPath = Configurator.eINSTANCE.getInitialModelPath();

        algorithm = this.problemName.substring(this.problemName.lastIndexOf('_') + 1);

        try {
            //            Files.copy(sourceModelPath, modelPath);
            org.apache.commons.io.FileUtils.copyFile(sourceModelPath.toFile(), modelPath.toFile());
        } catch (IOException | RuntimeException e) {
            String msg = String.format("Coping the source model %s to %s has generated the error: %s", sourceModelPath,
                    folderPath, e.getMessage());
            JMetalLogger.logger.severe(msg);
        }

        Refactoring refactoring = new UMLRefactoring(modelPath.toString());
        refactoring.setSolutionID(this.name);
        this.setVariable(0, refactoring);
    }


    public void createRandomRefactoring() {

        try {
            getVariable(VARIABLE_INDEX).createRandomRefactoring();
        } catch (EasierException e) {
            JMetalLogger.logger.severe(String.format("Cannot be computed a refactoring for Solution: %s.", this.getName()));
        }

        this.setAttribute(CrowdingDistance.class, 0.0);
    }


    @Override
    public Solution<Refactoring> copy() {
        return new UMLRSolution(this);
    }

    @Override
    public void computeArchitecturalChanges() {
        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "computeArchitecturalChanges_start");

        try (EasierUmlModel model = EOLStandalone.createUmlModel(modelPath.toString())) {

            for (RefactoringAction action : getVariable(0).getActions()) {

                double brf = Configurator.eINSTANCE.getBRF(action.getName());
                double aw = action.computeArchitecturalChanges(model.allContents());

                architecturalChanges += brf * aw;
            }
            architecturalChanges += Configurator.eINSTANCE.getInitialChanges();

        } catch (URISyntaxException | EolModelLoadingException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException eRun) {
            JMetalLogger.logger.severe(eRun.getMessage());
        } catch (EasierException e) {
            throw new RuntimeException(e);
        }
        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "computeArchitecturalChanges_end");
        JMetalLogger.logger.info(String.format("Architectural changes computed : %s", architecturalChanges));
    }

    @Override
    public void computeScenarioRT() {
//
//        /*
//         * The updated model can have more nodes than the source node since original
//         * nodes can be cloned. The benefits of cloning nodes is taken into account by
//         * the performance model. For this reason, the perfQ analyzes only the
//         * performance metrics of the nodes common among the models
//         */
//
//        // Represent the reference point index of each UML scenario
//        final int rebook_index = 0;
//        final int update_user_index = 1;
//        final int login_index = 2;
//
//        int scenarioIndex = 0;
//
//        // The elements of the source model;
//        List<EObject> scenarios = null;
//
//        // The function considers only the elements having the stereotypes GaScenario
//        try (EasierUmlModel uml = EpsilonStandalone.createUmlModel(modelPath.toString())) {
//            scenarios = (List<EObject>) uml.getAllOfType("UseCase");
//            scenarios = filterByStereotype(scenarios, GQAM_NAMESPACE + "GaScenario");
//
//            // for each element of the source model, it is picked the element with the same
//            // id in the refactored one
//            for (EObject element : scenarios) {
//                Stereotype stereotype = ((Element) element).getAppliedStereotype(GQAM_NAMESPACE + "GaScenario");
//                EList<?> values = (EList<?>) ((Element) element).getValue(stereotype, "respT");
//
//                if (!values.isEmpty()) {
//                    if ("Rebook a ticket".equals(((UseCase) element).getName())) {
//                        scenarioIndex = rebook_index;
//                    } else if ("Update user details".equals(((UseCase) element).getName())) {
//                        scenarioIndex = update_user_index;
//                    } else if ("Login".equals(((UseCase) element).getName())) {
//                        scenarioIndex = login_index;
//                    } else {
//                        throw new RuntimeException("Scenario name does not support yet!");
//                    }
//
//                    scenarioRTs[scenarioIndex] = Double.parseDouble(values.get(0).toString());
//                }
//
//            }
//            uml.dispose();
//            new UMLMemoryOptimizer().cleanup();
//        } catch (Exception e) {
//            JMetalLogger.logger.severe(String.format("Solution # '%s' has trown an error while computing PerfQ!!!", this.name));
//            e.printStackTrace();
//        }
    }

    public double[] getScenarioRTs() {
        return scenarioRTs;
    }

    public void executeRefactoring() {
        final Refactoring ref = getVariable(VARIABLE_INDEX);

        this.setRefactored(ref.execute());
        new UMLMemoryOptimizer().cleanup();
    }

    @Override
    public void setRefactored(boolean bRefactored) {
        super.setRefactored(bRefactored);

        // If the solution is a xOvered solution, and it cannot be applied to the model
        // the FAILED_CROSSOVER counter is increased
        if (!isRefactored() && isCrossover())
            FAILED_CROSSOVER++;
    }

    @Override
    public void computeReliability() {

        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "computeReliability_start");
        // stores the in memory model to a file
        UMLReliability uml = null;
        try {
            uml = new UMLReliability(new UMLModelPapyrus(modelPath.toString()).getModel());
            setReliability(new Reliability(uml.getScenarios()).compute());

            ResourceSet rs = uml.getModel().eResource().getResourceSet();
            while (rs.getResources().size() > 0) {
                Resource res = rs.getResources().get(0);
                res.eAdapters().clear();
                res.unload();
                rs.getResources().remove(res);
            }
        } catch (MissingTagException e) {
            JMetalLogger.logger.severe("Error in computing the reliability");
            String line = this.name + "," + e.getMessage() + "," + getVariable(VARIABLE_INDEX).toString();

            new FileUtils().reliabilityErrorLogToCSV(line);
        }

        new UMLMemoryOptimizer().cleanup();
        EasierResourcesLogger.checkpoint(this.getClass().getSimpleName(), "computeReliability_end");
        JMetalLogger.logger.info(String.format("Reliability computed : %s", this.reliability));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        UMLRSolution other = (UMLRSolution) obj;

        if (folderPath == null ^ other.folderPath == null)
            return false;
        return true;
    }

    // Set Reliability.
    // If the rel param is greater than 1 reliability is set to 1
    public void setReliability(double rel) {
        this.reliability = rel < 1 ? rel : 1;
    }

    public void setPAs(int pas) {
        this.numPAs = pas;
    }

    public enum SupportedType {
        NODE {
            @Override
            public String toString() {
                return "node";
            }
        },
        COMPONENT {
            @Override
            public String toString() {
                return "component";
            }
        },
        OPERATION {
            @Override
            public String toString() {
                return "operation";
            }
        }
    }


}
