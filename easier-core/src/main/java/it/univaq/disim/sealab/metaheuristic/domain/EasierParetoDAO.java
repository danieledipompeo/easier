package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;

import java.util.List;

public class EasierParetoDAO extends EasierPopulationDAO {

    int independentRun = -1;
    int iteration;

    public EasierParetoDAO(List<RSolution<?>> solPareto, int it) {
        super();
        addPopulation(solPareto);
        this.iteration = it;
    }

    public EasierParetoDAO(List<RSolution<?>> solPareto, int it, int iRun){
        this(solPareto, it);
        this.independentRun = iRun;
    }

    public void addPopulation(final List<RSolution<?>> sols) {
        for (RSolution<?> sol : sols) {
            EasierSolutionDAO easierSolutionDAO = new EasierSolutionDAO(sol);
            solutions.add(easierSolutionDAO);
        }
    }

    public int getIteration() {
        return iteration;
    }
    public int getIndependentRun() {
        return independentRun;
    }
}
