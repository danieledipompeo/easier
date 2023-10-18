package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;

import java.util.*;

public class EasierSolutionDAO {

    private static Set<Integer> ADDED_SOLUTION;

    static {
        ADDED_SOLUTION = new HashSet<>();
    }

    double reliability;
    double pas;
    double changes;
    double perfq;
    double energy;

    Map<String, Double> objectives;
    int solID;
    List<EasierRefactoringActionDAO> refactoring;

    public EasierSolutionDAO(RSolution<?> sol) {
        refactoring = new ArrayList<>();
        objectives = new HashMap<>();
        setSolution(sol);
    }

    /**
     * Check if the RSolution is already added
     */
    public static boolean alreadyIn(int id) {
        return ADDED_SOLUTION.contains(id);
    }

   /* public double getReliability() {
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

    public double getEnergy() {
        return energy;
    }*/

    public List<EasierRefactoringActionDAO> getRefactoring() {
        return refactoring;
    }

    public void setRefactoring(Refactoring ref) {
        for (RefactoringAction action : ref.getActions()) {
            EasierRefactoringActionDAO refactoringActionDAO = new EasierRefactoringActionDAO();
            refactoringActionDAO.setRefactoringAction(action);
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

    public void setObjectives(double[] objValues) {

        List<String> objList =  Configurator.eINSTANCE.getObjectivesList();
        for(String objName : objList) {
            int index = objList.indexOf(objName);
            this.objectives.put(objName, objValues[index]);
        }
    }

    public Map<String, Double> getObjectives() {
        return objectives;
    }
}
