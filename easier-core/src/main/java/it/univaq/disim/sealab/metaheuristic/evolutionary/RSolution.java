package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
import org.uma.jmetal.solution.AbstractSolution;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public abstract class RSolution<T> extends AbstractSolution<T> {

    protected Path modelPath, sourceModelPath, initialModelPath;

    protected boolean refactored;
    protected boolean isCrossover;
    protected boolean mutated;

    public static int SOLUTION_COUNTER = 0;

    protected int name;

    protected double perfQ;
    protected int numPAs;

    protected double reliability;
    protected double architecturalChanges;
    public static final int VARIABLE_INDEX;

    protected RSolution<T>[] parents;

    public static int MutationCounter = 0;
    public static int XOverCounter = 0;

    protected int allowedFailures;
    protected int refactoringLength;
    protected String problemName;

    static {
        VARIABLE_INDEX = 0;
    }

    /**
     * Constructor
     */
    protected RSolution(Path srcModelPath, String pName) {
        super(1, Configurator.eINSTANCE.getObjectives());
        allowedFailures = Configurator.eINSTANCE.getAllowedFailures();
        refactoringLength = Configurator.eINSTANCE.getLength();
        sourceModelPath = srcModelPath;
        problemName = pName;
    }

    public abstract void countingPAs();

    public abstract double evaluatePerformance();

    public abstract void executeRefactoring();

    public abstract void applyTransformation();

    public abstract void computeReliability();

    public abstract void computeArchitecturalChanges();

    public abstract void invokeSolver();

    public abstract boolean isFeasible();

    public Path getModelPath() {
        return modelPath;
    }

    public double getReliability() {
        return reliability;
    }

    public Path getSourceModelPath() {
        return sourceModelPath;
    }

    public void setRefactored(boolean isRefactored) {
        this.refactored = isRefactored;
    }

    public boolean isRefactored() {
        return refactored;
    }

    public void setCrossovered(boolean isCrossover) {
        this.isCrossover = isCrossover;
        XOverCounter++;
    }

    public void setMutated(boolean isMutated) {
        this.mutated = isMutated;
        MutationCounter++;
    }

    public boolean isMutated() {
        return mutated;
    }

    public boolean isCrossover() {
        return isCrossover;
    }

    public double getPerfQ() {
        return perfQ;
    }

    public void setPerfQ(float perfQ) {
        this.perfQ = perfQ;
    }

    public int getPAs() {
        return numPAs;
    }

    public int getName() {
        return name;
    }

    public String getProblemName() {
        return problemName;
    }

    public static synchronized int getCounter() {
        return SOLUTION_COUNTER++;
    }

    public void setName() {
        this.name = getCounter();
    }

    public void setParents(RSolution parent1, RSolution parent2) {
        this.parents[0] = parent1;
        this.parents[1] = parent2;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if(obj == null)
            return false;
        
        if (getClass() != obj.getClass())
            return false;

        RSolution<T> other = (RSolution<T>) obj;

        if (isCrossover != other.isCrossover)
            return false;
        if (mutated != other.mutated)
            return false;
        if (numPAs != other.numPAs)
            return false;
        if (Double.doubleToLongBits(perfQ) != Double.doubleToLongBits(other.perfQ))
            return false;
        if (Double.doubleToLongBits(reliability) != Double.doubleToLongBits(other.reliability))
            return false;

        if(!parents.equals(other.parents))
            return false;

        if (getVariable(VARIABLE_INDEX) != null && !getVariable(VARIABLE_INDEX).equals(other.getVariable(VARIABLE_INDEX)))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), modelPath, sourceModelPath, initialModelPath, refactored, isCrossover, mutated, name, perfQ, numPAs, reliability, architecturalChanges, allowedFailures, refactoringLength, problemName);
        result = 31 * result + Arrays.hashCode(parents);
        return result;
    }

    public void setRefactoring(Refactoring ref) {
        setVariable(0, (T) ref);
    }

    public double getArchitecturalChanges() {
        return architecturalChanges;
    }

    /**
     * Returns the solution data as a CSV format
     * "solID,perfQ,#changes,pas,reliability"
     */
    public String objectiveToCSV() {
        return String.format("%s,%s,%s,%s,%s", this.getName(), this.perfQ, this.getArchitecturalChanges(), this.numPAs,
                this.reliability);
    }

    public void refactoringToCSV() {
        new FileUtils().refactoringDumpToCSV(((Refactoring) getVariable(0)).toCSV());
    }

    /**
     * Check if two RSolutions have the same objectives values. If a local
     * minimum/maximum is reached then the two solutions should have the same
     * objective values
     *
     * @param rSolution
     * @return true if two solutions have the same objective values, false otherwise
     */
    public boolean isLocalOptmimalPoint(RSolution<?> rSolution) {
        double ePas = Configurator.eINSTANCE.getLocalOptimalPointEpsilon()[0];
        double eRel = Configurator.eINSTANCE.getLocalOptimalPointEpsilon()[1];
        double ePerfQ = Configurator.eINSTANCE.getLocalOptimalPointEpsilon()[2];
        double eChanges = Configurator.eINSTANCE.getLocalOptimalPointEpsilon()[3];

        return (Math.abs(this.getPAs()) <= Math.abs(rSolution.getPAs()) + ePas
                && Math.abs(this.getPAs()) >= Math.abs(rSolution.getPAs()) - ePas)
                && (Math.abs(this.getArchitecturalChanges()) <= Math.abs(rSolution.getArchitecturalChanges()) * eChanges
                && Math.abs(this.getArchitecturalChanges()) >= Math.abs(rSolution.getArchitecturalChanges()) / eChanges)
                && (Math.abs(this.getPerfQ()) <= Math.abs(rSolution.getPerfQ()) * ePerfQ
                && Math.abs(this.getPerfQ()) >= Math.abs(rSolution.getPerfQ()) / ePerfQ)
                && (Math.abs(this.getReliability()) <= Math.abs(rSolution.getReliability()) * eRel
                && Math.abs(this.getReliability()) >= Math.abs(rSolution.getReliability()) / eRel);
    }
}
