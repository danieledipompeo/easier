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

    // All computed objectives
    Map<String, Double> objectives;

    // List of objectives considered to compute the fitness
    Map<String, Double> consideredObjectives;
    int solID;
    List<EasierRefactoringActionDAO> refactoring;

    public EasierSolutionDAO(RSolution<?> sol) {
        refactoring = new ArrayList<>();
        objectives = new HashMap<>();
        consideredObjectives = new HashMap<>();
        setSolution(sol);
    }

    /**
     * Check if the RSolution is already added
     */
    public static boolean alreadyIn(int id) {
        return ADDED_SOLUTION.contains(id);
    }

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
        setConsideredObjectives(sol.getMapOfObjectives());
        objectives.putAll(sol.getMapOfObjectives());
        setRefactoring(sol.getVariable(0));
    }

    public void setConsideredObjectives(Map<String, Double> mapOfObjs) {

        List<String> objList =  Configurator.eINSTANCE.getObjectivesList();

        objList.forEach(objName -> {
            if(mapOfObjs.containsKey(objName)) {
                this.consideredObjectives.put(objName, mapOfObjs.get(objName));
            }
        });
    }

    public Map<String, Double> getObjectives() {
        return objectives;
    }

    public Map<String, Double> getConsideredObjectives() {
        return consideredObjectives;
    }
}
