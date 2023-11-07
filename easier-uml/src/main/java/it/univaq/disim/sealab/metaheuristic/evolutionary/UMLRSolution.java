package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.actions.UMLRefactoring;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.ObjectiveEstimator;
import it.univaq.disim.sealab.metaheuristic.utils.*;
import it.univaq.sealab.umlreliability.MissingTagException;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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


        this.mapOfObjectives.putAll(s.getMapOfObjectives());

        for(int i = 0; i < s.getObjectives().length; i++) {
            this.setObjective(i, s.getObjective(i));
        }

        for (int i = 0; i < s.getNumberOfObjectives(); i++) {
            this.setObjective(i, s.getObjective(i));
        }

        this.attributes = s.attributes;
        this.setAttribute(CrowdingDistance.class, s.getAttribute(CrowdingDistance.class));

    }

    protected void init() {

        parents = new UMLRSolution[2];
        scenarioRTs = new double[3];
        mapOfObjectives = new HashMap<>(Configurator.eINSTANCE.getObjectivesList().size());

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

    /**
     * Compute all available objectives of the solution.
     * Then, the Problem::evaluate will select the ones to be used.
     *
     */
    public void computeObjectives() {
        mapOfObjectives.put(Configurator.PAS_LABEL,
                ObjectiveEstimator.countPerformanceAntipattern(this.modelPath, this.getName()));
        mapOfObjectives.put(Configurator.RELIABILITY_LABEL, ObjectiveEstimator.reliability(this.modelPath));
        mapOfObjectives.put(Configurator.CHANGES_LABEL, ObjectiveEstimator.refactoringCost(this));
        mapOfObjectives.put(Configurator.PERF_Q_LABEL, ObjectiveEstimator.perfQ(this.sourceModelPath, this.modelPath));
        mapOfObjectives.put(Configurator.SYS_RESP_T_LABEL, ObjectiveEstimator.systemResponseTime(this.modelPath));
        mapOfObjectives.put(Configurator.ENERGY_LABEL, ObjectiveEstimator.energyEstimation(this.modelPath));
        mapOfObjectives.put(Configurator.POWER_LABEL, ObjectiveEstimator.powerEstimator(this.modelPath));
        mapOfObjectives.put(Configurator.ECONOMIC_COST, ObjectiveEstimator.economicCost(this.modelPath));
    }

    public void computeObjectivesToUnfeasibleValues(){
        mapOfObjectives.put(Configurator.PAS_LABEL, Double.MAX_VALUE);
        mapOfObjectives.put(Configurator.RELIABILITY_LABEL, -1 * Double.MIN_VALUE);
        mapOfObjectives.put(Configurator.CHANGES_LABEL, Double.MAX_VALUE);
        mapOfObjectives.put(Configurator.PERF_Q_LABEL, -1 * Double.MAX_VALUE);
        mapOfObjectives.put(Configurator.SYS_RESP_T_LABEL, Double.MAX_VALUE);
        mapOfObjectives.put(Configurator.ENERGY_LABEL, Double.MAX_VALUE);
        mapOfObjectives.put(Configurator.POWER_LABEL, Double.MAX_VALUE);
        mapOfObjectives.put(Configurator.ECONOMIC_COST, Double.MAX_VALUE);
    }
}
