package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.epsilon.EpsilonStandalone;
import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.epsilon.evl.EVLStandalone;
import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.actions.UMLRefactoring;
import it.univaq.disim.sealab.metaheuristic.utils.*;
import it.univaq.sealab.umlreliability.MissingTagException;
import it.univaq.sealab.umlreliability.Reliability;
import it.univaq.sealab.umlreliability.UMLReliability;
import it.univaq.sealab.umlreliability.model.UMLModelPapyrus;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Node;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UseCase;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author peo
 */
public class UMLRSolution extends RSolution<Refactoring> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final static String refactoringLibraryModule, uml2lqnModule;
    private final static String GQAM_NAMESPACE;
    public static int FAILED_CROSSOVER = 0;

    static {
        refactoringLibraryModule = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-refactoringLibrary", "evl", "AP-UML-MARTE.evl").toString();
        uml2lqnModule = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
                "easier-uml2lqn", "org.univaq.uml2lqn").toString();

        GQAM_NAMESPACE = "MARTE::MARTE_AnalysisModel::GQAM::";
    }

    private Path initialModelPath;
    private Path folderPath;
    private double[] scenarioRTs;
    private String algorithm;
    private Map<String, Map<String, Double>> extractFuzzyValues;

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

        //        initMap();
        folderPath = Paths.get(Configurator.eINSTANCE.getTmpFolder().toString(), String.valueOf((getName() / 100)),
                String.valueOf(getName()));
        modelPath = folderPath.resolve(getName() + ".uml");
        initialModelPath = Configurator.eINSTANCE.getInitialModelPath();

        algorithm = this.problemName.substring(this.problemName.lastIndexOf('_') + 1);

        try {
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

        int num_failures = 0;

        do {
            try {
                if (!tryRandomPush())
                    num_failures++;
                if (num_failures >= allowedFailures) {
                    throw new RuntimeException(
                            String.format("Exceed %s failures \t %s ", allowedFailures, num_failures));
                }
            } catch (UnexpectedException | EolRuntimeException e) {
                e.printStackTrace();
            }
        } while (getVariable(VARIABLE_INDEX).getActions().size() < refactoringLength);
        this.setAttribute(CrowdingDistance.class, 0.0);
    }

    /**
     * Return @return true, if @param listOfActions is made up of independent refactoring actions,
     *
     * @return false otherwise
     */
    public boolean isIndependent(List<RefactoringAction> listOfActions) {
        return getVariable(0).isIndependent(listOfActions);
    }

    boolean tryRandomPush() throws UnexpectedException, EolRuntimeException {
        return getVariable(0).tryRandomPush();
    }

    public boolean isFeasible() {
        return getVariable(VARIABLE_INDEX).isFeasible();
    }

    protected void copyRefactoringVariable(Refactoring refactoring) {
        Refactoring refactoringCloned = refactoring.clone();
        refactoringCloned.setSolutionID(this.getName());
        this.setVariable(VARIABLE_INDEX, refactoringCloned);
    }

    @Override
    public Solution<Refactoring> copy() {
        return new UMLRSolution(this);
    }

    /**
     * This method counts the number of Performance Antipatterns (PAs) invoking
     * the PADRE perf-detection file
     */
    public void countingPAs() {

        EasierResourcesLogger.checkpoint("UMLRSolution", "countingPAs_start");
        EVLStandalone pasCounter = new EVLStandalone();
        try (EasierUmlModel uml = EpsilonStandalone.createUmlModel(modelPath.toString())) {
            pasCounter.setModel(uml);

            pasCounter.setSource(Paths.get(refactoringLibraryModule));

            // set the prob to be perf antipatterns
            double fuzzyThreshold = Configurator.eINSTANCE.getProbPas();
            pasCounter.setParameter(fuzzyThreshold, "float", "prob_to_be_pa");

            extractFuzzyValues = pasCounter.extractFuzzyValues();
        } catch (EolModelLoadingException | URISyntaxException e) {
            e.printStackTrace();
        }

        // Count performance antipatterns and build a string for the next csv storing
        for (String pas : extractFuzzyValues.keySet()) {
            Map<String, Double> mPaf = extractFuzzyValues.get(pas);
            numPAs += mPaf.keySet().size();
            for (String targetElement : mPaf.keySet()) {
                double fuzzy = mPaf.get(targetElement);
                new FileUtils().performanceAntipatternDumpToCSV(
                        String.format("%s,%s,%s,%.4f", this.name, pas, targetElement, fuzzy));
            }
        }

        pasCounter.clearMemory();
        new UMLMemoryOptimizer().cleanup();
        EasierResourcesLogger.checkpoint("UMLRSolution", "countingPAs_end");
        //        EasierResourcesLogger.toCSV();
        JMetalLogger.logger.info(String.format("Performance antipatterns : %s", numPAs));
    }

    /*
     * From the ANT scripts target name="ChangeRoot" depends="LoadModels">
     * <epsilon.xml.loadModel name="PlainLQN" file="${output}/${name}.xml"
     * read="true" store="true"/> <epsilon.eol src="changeRoot.eol"> <model
     * ref="PlainLQN"/> </epsilon.eol>
     *
     * <epsilon.storeModel model="PlainLQN"/> <!-- <eclipse.refreshLocal
     * resource="${output}/output.xml" depth="infinite"/> -->
     *
     * </target>
     *
     * <target name="Solver" depends="ChangeRoot"> <exec
     * executable="${executableAbsPath}"> <arg value="${output}/${name}.xml"/>
     * </exec> </target>
     */
    public void invokeSolver() {
        try {
            EasierResourcesLogger.checkpoint("UMLRSolution", "invokeSolver_start");
            new WorkflowUtils().invokeSolver(this.folderPath);
            EasierResourcesLogger.checkpoint("UMLRSolution", "invokeSolver_end");
        } catch (Exception e) {
            String line = this.name + "," + e.getMessage() + "," + getVariable(VARIABLE_INDEX).toString();
            new FileUtils().failedSolutionLogToCSV(line);
        }

        EasierResourcesLogger.checkpoint("UMLRSolution", "backAnnotation_start");
        try {
            new WorkflowUtils().backAnnotation(modelPath);
        } catch (URISyntaxException | EolRuntimeException e) {
            String line = this.name + "," + e.getMessage() + "," + getVariable(VARIABLE_INDEX).toString() + "," +
                    isMutated() + "," + isCrossover();
            new FileUtils().failedSolutionLogToCSV(line);
        }
        EasierResourcesLogger.checkpoint("UMLRSolution", "backAnnotation_end");
    }

    /**
     * @return the performance quality indicator as described in
     * <a href="https://doi.org/10.1109/ICSA.2018.00020">https://doi.org/10.1109/ICSA.2018.00020</a>
     * @throws EolModelElementTypeNotFoundException when the perfQ cannot be computed
     */
    public double evaluatePerformance() {
        EasierResourcesLogger.checkpoint("UMLRSolution", "evaluatePerformance_start");
        perfQ = perfQ();
        EasierResourcesLogger.checkpoint("UMLRSolution", "evaluatePerformance_end");
        //        EasierResourcesLogger.toCSV();
        JMetalLogger.logger.info(String.format("Performance : %s", perfQ));
        return perfQ;
    }

    private double perfQ() {

        /*
         * The updated model can have more nodes than the source node since original
         * nodes can be cloned. The benefits of cloning nodes is taken into account by
         * the performance model. For this reason, the perfQ analyzes only the
         * performance metrics of the nodes common among the models
         */

        // The lists used to store the elements of both models
        List<EObject> sourceElements = new ArrayList<EObject>();

        // The elements of the source model;
        List<EObject> nodes = null;
        List<EObject> scenarios = null;
        //        try (EasierUmlModel source = (EasierUmlModel) EpsilonStandalone.createUmlModel(sourceModelPath.toString());
        try (EasierUmlModel source = EpsilonStandalone.createUmlModel(initialModelPath.toString());
             EasierUmlModel uml = EpsilonStandalone.createUmlModel(modelPath.toString())) {
            nodes = (List<EObject>) source.getAllOfType("Node");
            scenarios = (List<EObject>) source.getAllOfType("UseCase");


            // The function considers only the elements having the stereotypes GaExecHosta
            // and GaScenario
            nodes = filterByStereotype(nodes, GQAM_NAMESPACE + "GaExecHost");
            scenarios = filterByStereotype(scenarios, GQAM_NAMESPACE + "GaScenario");
            sourceElements.addAll(scenarios);
            sourceElements.addAll(nodes);

            int numberOfMetrics = 0;


            // Variable representing the perfQ value
            double value = 0d;
            // for each elements of the source model, it is picked the element with the same
            // id in the refactored one
            for (EObject element : sourceElements) {
                String id = getXmiId(source, element);
                EObject correspondingElement = (EObject) uml.getElementById(id);

                if (element instanceof UseCase) {
                    value += -1 * computePerfQValue((Element) element, (Element) correspondingElement, "GaScenario",
                            "respT");
                    value += computePerfQValue((Element) element, (Element) correspondingElement, "GaScenario",
                            "throughput");
                    numberOfMetrics += 2;
                } else if (element instanceof Node) {
                    value += -1 * computePerfQValue((Element) element, (Element) correspondingElement, "GaExecHost",
                            "utilization");
                    numberOfMetrics++;
                }
            }
            uml.dispose();
            new UMLMemoryOptimizer().cleanup();
            return value / numberOfMetrics;
        } catch (Exception e) {
            JMetalLogger.logger.severe(String.format("Solution # '%s' has thrown an error while computing PerfQ!!!",
                    this.name));
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void computeArchitecturalChanges() {
        EasierResourcesLogger.checkpoint("UMLRSolution", "computeArchitecturalChanges_start");

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
        EasierResourcesLogger.checkpoint("UMLRSolution", "computeArchitecturalChanges_end");
        JMetalLogger.logger.info(String.format("Architectural changes computed : %s", architecturalChanges));
    }

    @Override
    public void computeScenarioRT() {

        /*
         * The updated model can have more nodes than the source node since original
         * nodes can be cloned. The benefits of cloning nodes is taken into account by
         * the performance model. For this reason, the perfQ analyzes only the
         * performance metrics of the nodes common among the models
         */

        // Represent the reference point index of each UML scenario
        final int rebook_index = 0;
        final int update_user_index = 1;
        final int login_index = 2;

        int scenarioIndex = 0;

        // The elements of the source model;
        List<EObject> scenarios = null;

        // The function considers only the elements having the stereotypes GaScenario
        try (EasierUmlModel uml = EpsilonStandalone.createUmlModel(modelPath.toString())) {
            scenarios = (List<EObject>) uml.getAllOfType("UseCase");
            scenarios = filterByStereotype(scenarios, GQAM_NAMESPACE + "GaScenario");

            // for each element of the source model, it is picked the element with the same
            // id in the refactored one
            for (EObject element : scenarios) {
                Stereotype stereotype = ((Element) element).getAppliedStereotype(GQAM_NAMESPACE + "GaScenario");
                EList<?> values = (EList<?>) ((Element) element).getValue(stereotype, "respT");

                if (!values.isEmpty()) {
                    if ("Rebook a ticket".equals(((UseCase) element).getName())) {
                        scenarioIndex = rebook_index;
                    } else if ("Update user details".equals(((UseCase) element).getName())) {
                        scenarioIndex = update_user_index;
                    } else if ("Login".equals(((UseCase) element).getName())) {
                        scenarioIndex = login_index;
                    } else {
                        throw new RuntimeException("Scenario name does not support yet!");
                    }

                    scenarioRTs[scenarioIndex] = Double.parseDouble(values.get(0).toString());
                }

            }
            uml.dispose();
            new UMLMemoryOptimizer().cleanup();
        } catch (Exception e) {
            JMetalLogger.logger.severe(
                    String.format("Solution # '%s' has trown an error while computing PerfQ!!!", this.name));
            e.printStackTrace();
        }
    }

    public double[] getScenarioRTs() {
        return scenarioRTs;
    }

    private double computePerfQValue(final Element source, final Element ref, final String stereotypeName,
                                     final String tag) {

        Stereotype stereotype = source.getAppliedStereotype(GQAM_NAMESPACE + stereotypeName);
        EList<?> values = (EList<?>) source.getValue(stereotype, tag);

        double sourceValue = 0d;
        if (!values.isEmpty())
            sourceValue = Double.parseDouble(values.get(0).toString());

        stereotype = ref.getAppliedStereotype(GQAM_NAMESPACE + stereotypeName);
        values = (EList<?>) ref.getValue(stereotype, tag);

        double refValue = 0d;
        if (!values.isEmpty())
            refValue = Double.parseDouble(values.get(0).toString());

        return (refValue + sourceValue) == 0 ? 0d : (refValue - sourceValue) / (refValue + sourceValue);
    }

    private List<EObject> filterByStereotype(Collection<EObject> elements, String stereotypeNamespace) {
        return elements.stream().filter(e -> ((Element) e).getAppliedStereotype(stereotypeNamespace) != null)
                .collect(Collectors.toList());
    }

    private String getXmiId(EmfModel model, EObject element) {
        return ((XMLResource) model.getResource()).getID(element);
    }

    /**
     * Invokes the ETL engine in order to run the UML2LQN transformation.
     */
    public void applyTransformation() {

        EasierResourcesLogger.checkpoint("UMLRSolution", "applyTransformation_start");
        new WorkflowUtils().applyTransformation(this.modelPath);
        EasierResourcesLogger.checkpoint("UMLRSolution", "applyTransformation_end");
    }

    public void executeRefactoring() {
        final Refactoring ref = getVariable(VARIABLE_INDEX);

        this.setRefactored(ref.execute());
        new UMLMemoryOptimizer().cleanup();
    }

    @Override
    public void setRefactored(boolean isRefactored) {
        super.setRefactored(isRefactored);

        // If the solution is a xOvered solution, and it cannot be applied to the model
        // the FAILED_CROSSOVER counter is increased
        if (!isRefactored && isCrossover())
            FAILED_CROSSOVER++;
    }

    @Override
    public void computeReliability() {

        EasierResourcesLogger.checkpoint("UMLRSolution", "computeReliability_start");
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
        EasierResourcesLogger.checkpoint("UMLRSolution", "computeReliability_end");
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
