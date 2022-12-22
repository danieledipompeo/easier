package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;

import java.util.ArrayList;
import java.util.List;

public class EasierParetoDAO extends EasierPopulationDAO {

    int independentRun;

    public EasierParetoDAO(List<RSolution<?>> solPareto, int iRun) {
        super();
        addPopulation(solPareto);
        this.independentRun = iRun;
    }

    public void addPopulation(final List<RSolution<?>> sols){
        for (RSolution<?> sol : sols) {
            EasierSolutionDAO easierSolutionDAO = new EasierSolutionDAO(sol);
            solutions.add(easierSolutionDAO);
        }
    }
    public int getIndependentRun() {
        return independentRun;
    }
}
