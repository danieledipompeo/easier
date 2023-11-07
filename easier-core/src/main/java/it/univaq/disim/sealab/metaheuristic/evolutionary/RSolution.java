package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
import org.uma.jmetal.solution.AbstractSolution;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    protected RSolution<T>[] parents;
    protected int allowedFailures;
    protected int refactoringLength;
    protected String problemName;

    protected Map<String, Double> mapOfObjectives;

    protected RSolution(Path srcModelPath, String pName) {
        super(1, Configurator.eINSTANCE.getObjectivesList().size());
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

    public Path getModelPath() {
        return modelPath;
    }

    public Path getFolderPath() {
        return this.modelPath.getParent();
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

    public int getName() {
        return name;
    }

    public String getProblemName() {
        return problemName;
    }

    public void setName() {
        this.name = getCounter();
    }

    public void setParents(RSolution<T> parent1, RSolution<T> parent2) {
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

        for(int objectiveIndex = 0; objectiveIndex <= getObjectives().length; objectiveIndex++){
            if (getObjective(objectiveIndex) != other.getObjective(objectiveIndex)) {
                return false;
            }
        }

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

    /*
     * Returns the solution data as a CSV format
     * "solID,perfQ,#changes,pas,reliability"
     */
    public String objectiveToCSV() {
        return this.getName() + String.join(",",
                Arrays.stream(getObjectives()).mapToObj(String::valueOf).toArray(String[]::new));
//        return String.format("%s,%s,%s,%s,%s", this.getName(), this.perfQ, this.getArchitecturalChanges(), this.numPAs,
//                this.reliability);
    }

    public void refactoringToCSV() {
        new FileUtils().refactoringDumpToCSV(((Refactoring) getVariable(0)).toCSV());
    }

    public Map<String, Double> getMapOfObjectives(){
        return mapOfObjectives;
    }
}
