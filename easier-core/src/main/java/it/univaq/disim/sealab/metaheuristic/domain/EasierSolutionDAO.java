package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EasierSolutionDAO {

    private static Set<Integer> ADDED_SOLUTION;

    static {
        ADDED_SOLUTION = new HashSet<>();
    }

    double reliability;
    double pas;
    double changes;
    double perfq;
    int solID;
    List<EasierRefactoringActionDAO> refactoring;

    public EasierSolutionDAO(RSolution<?> sol) {
        refactoring = new ArrayList<>();
        setSolution(sol);
    }

    public static boolean alreadyIn(int id) {
        return ADDED_SOLUTION.contains(id);
    }

    public double getReliability() {
        return reliability;
    }

    public double getPas() {
        return pas;
    }

    public double getChanges() {
        return changes;
    }

    public double getPerfq() {
        return perfq;
    }

    public List<EasierRefactoringActionDAO> getRefactoring() {
        return refactoring;
    }

    public void setRefactoring(Refactoring ref) {
        for (RefactoringAction action : ref.getActions()) {
            EasierRefactoringActionDAO refactoringActionDAO = new EasierRefactoringActionDAO();
            try {
                refactoringActionDAO.setRefactoringAction(action);
            } catch (EasierException e) {
                throw new RuntimeException(e);
            }
            refactoring.add(refactoringActionDAO);
        }
    }

    public int getSolID() {
        return solID;
    }

    private void setSolution(RSolution<?> sol) {
        solID = sol.getName();
        ADDED_SOLUTION.add(solID);
        setObjectives(sol.getObjectives());
        setRefactoring((Refactoring) sol.getVariable(0));
    }

    public void setObjectives(double[] objectives) {
        perfq = objectives[0];
        changes = objectives[1];
        if (Configurator.eINSTANCE.getProbPas() != 0) {
            pas = objectives[2];
            reliability = objectives[3];
        } else {
            reliability = objectives[2];
        }
    }
}
