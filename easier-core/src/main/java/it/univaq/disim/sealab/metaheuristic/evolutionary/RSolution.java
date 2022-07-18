package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
import org.uma.jmetal.solution.AbstractSolution;

import java.nio.file.Path;
import java.util.List;

public abstract class RSolution<T extends Refactoring> extends AbstractSolution<T> {

    public static final int VARIABLE_INDEX;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static int SOLUTION_COUNTER = 0;
    protected static int iMutationCounter = 0;
    protected static int iXOverCounter = 0;

    static {
        VARIABLE_INDEX = 0;
    }

    protected Path modelPath, sourceModelPath;
    protected boolean refactored;
    protected boolean isCrossover;
    protected boolean mutated;
    protected int name;
    protected double perfQ;
    protected int numPAs;
    protected double reliability;
    protected double architecturalChanges;
    protected RSolution<T>[] parents;
    protected int allowedFailures;
    protected int refactoringLength;
    protected String problemName;

    protected RSolution(Path srcModelPath, String pName) {
        super(1, Configurator.eINSTANCE.getObjectives());
        allowedFailures = Configurator.eINSTANCE.getAllowedFailures();
        refactoringLength = Configurator.eINSTANCE.getLength();
        sourceModelPath = srcModelPath;
        problemName = pName;
    }

    protected static void incrementXOverCounter(){
        iXOverCounter++;
    }

    protected static void incrementMutationCounter(){
        iMutationCounter++;
    }

    public static synchronized int getCounter() {
        return SOLUTION_COUNTER++;
    }

    public abstract void executeRefactoring();

    public abstract void computeReliability();

    public abstract void computeArchitecturalChanges();

    public abstract void computeScenarioRT();

    public Path getModelPath() {
        return modelPath;
    }

    public Path getFolderPath() {
        return this.modelPath.getParent();
    }

    public double getReliability() {
        return reliability;
    }

    public Path getSourceModelPath() {
        return sourceModelPath;
    }

    public boolean isRefactored() {
        return refactored;
    }

    public void setRefactored(boolean isRefactored) {
        this.refactored = isRefactored;
    }

    public void setCrossovered(boolean isCrossover) {
        this.isCrossover = isCrossover;
        incrementXOverCounter();
    }

    public boolean isMutated() {
        return mutated;
    }

    public void setMutated(boolean isMutated) {
        this.mutated = isMutated;
        incrementMutationCounter();
    }

    public boolean isCrossover() {
        return isCrossover;
    }

    public double getPerfQ() {
        return perfQ;
    }

    public void setPerfQ(double perfQ) {
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

    public void setName() {
        this.name = getCounter();
    }

    public void setParents(RSolution parent1, RSolution parent2) {
        this.parents[0] = parent1;
        this.parents[1] = parent2;
    }

    /**
     * Return @return true, if @param listOfActions is made up of independent refactoring actions,
     *
     * @return false otherwise
     */
    public boolean isIndependent(List<RefactoringAction> listOfActions) {
        return getVariable(VARIABLE_INDEX).isIndependent(listOfActions);
    }

    public boolean isFeasible() {
        return getVariable(VARIABLE_INDEX).isFeasible();
    }


    protected void copyRefactoringVariable(Refactoring refactoring) {
        Refactoring refactoringCloned = refactoring.clone();
        refactoringCloned.setSolutionID(this.getName());
        this.setVariable(VARIABLE_INDEX, (T) refactoringCloned);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        RSolution<T> other = (RSolution<T>) obj;
        if (isCrossover != other.isCrossover)
            return false;
        if (modelPath == null ^ other.modelPath == null) {
            return false;
        }
        if (mutated != other.mutated)
            return false;
        if (numPAs != other.numPAs)
            return false;
        if (Double.doubleToLongBits(perfQ) != Double.doubleToLongBits(other.perfQ))
            return false;
        if (Double.doubleToLongBits(reliability) != Double.doubleToLongBits(other.reliability))
            return false;
        if (parents.length != other.parents.length)
            return false;
        for (int i = 0; i < parents.length; i++) {
            if (parents[i] != other.parents[i]) {
                return false;
            }
        }
        if (getVariable(VARIABLE_INDEX) == null ^ other.getVariable(VARIABLE_INDEX) == null) {
            return false;
        }
        return getVariable(VARIABLE_INDEX).equals(other.getVariable(VARIABLE_INDEX));
    }

    public void setRefactoring(Refactoring ref) {
        setVariable(0, (T) ref);
    }

    public double getArchitecturalChanges() {
        return architecturalChanges;
    }

    /*
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
