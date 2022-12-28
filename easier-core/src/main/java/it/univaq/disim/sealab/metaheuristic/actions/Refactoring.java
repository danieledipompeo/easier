package it.univaq.disim.sealab.metaheuristic.actions;

import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Refactoring implements Cloneable {

    protected EasierModel easierModel;

    protected List<RefactoringAction> actions;
    protected int solutionID = -1;
    protected String modelPath;

    public Refactoring(final String mPath) {
        actions = new ArrayList<>();
        this.modelPath = mPath;
    }

    public Refactoring(Refactoring rfSource) {
        this(rfSource.modelPath);
        this.solutionID = rfSource.solutionID;
        for (RefactoringAction a : rfSource.getActions()) {
            this.getActions().add(a.clone());
        }
    }

    public abstract Refactoring clone();

    public void setSolutionID(int id) {
        solutionID = id;
    }

    public List<RefactoringAction> getActions() {
        return actions;
    }

    public void setActions(List<RefactoringAction> actions) {
        this.actions = actions;
    }

    public abstract boolean execute();

    public abstract boolean tryRandomPush();

    public abstract boolean isFeasible();

    public boolean hasMultipleOccurrence() {

        int refactoringLength = this.getActions().size();
        List<RefactoringAction> actions = this.getActions();
        for (int i = 0; i < refactoringLength; i++) {
            RefactoringAction a = actions.get(i);
            for (int j = i + 1; j < refactoringLength; j++) {
                RefactoringAction a2 = actions.get(j);
                if (a.equals(a2)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isIndependent(List<RefactoringAction> listOfActions){
        if (listOfActions.size() == 1) {
            RefactoringAction act = listOfActions.get(0);
               if (!easierModel.contains(act.getTargetElements())){
                        return false;
               }
        } else {
            for (int i = 0; i < listOfActions.size(); i++) {
                RefactoringAction act = listOfActions.get(i);
                for (int j = i + 1; j < listOfActions.size(); j++) {

                    // only use independent actions
                    if (listOfActions.get(j).isIndependent()) {
                        if(easierModel.contains(act.getCreatedElements())){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public EasierModel getEasierModel(){
        return easierModel;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Refactoring other = (Refactoring) obj;
        if (actions == null && other.actions != null) {
            return false;
        } else {
            if (actions.size() != other.actions.size())
                return false;
            for (int i = 0; i < actions.size(); i++) {
                if (!actions.get(i).equals(other.actions.get(i))) {
                    return false;
                }
            }
        }
        if(easierModel == null && other.easierModel != null) {
            return false;
        }
        return easierModel.equals(other.easierModel);
    }

    /**
     * Prints for each action a semicolon separated line
     * solID,action.toCSV()
     *
     * @return
     */
    public String toCSV() {
        StringBuilder strBuilder = new StringBuilder();
        final int solutionID = this.solutionID;
        for (int i = 0; i < getActions().size() - 1; i++) {
            strBuilder.append(solutionID).append(",").append(getActions().get(i).toCSV()).append("\n");
        }
        strBuilder.append(solutionID).append(",").append(getActions().get(getActions().size() - 1).toCSV());
        return strBuilder.toString();
    }

    /**
     * Print actions belong to the refactoring a semi-column string.
     * Add as prefix the solutionID
     *
     * @return
     */
    @Override
    public String toString() {
        return getActions().stream().map(RefactoringAction::toCSV)
                .collect(Collectors.joining("\n"+this.solutionID+",", this.solutionID+",",""));
    }
}
