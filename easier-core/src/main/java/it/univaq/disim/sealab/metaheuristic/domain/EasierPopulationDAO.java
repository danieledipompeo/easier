package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;

import java.util.ArrayList;
import java.util.List;

public class EasierPopulationDAO {

    protected List<EasierSolutionDAO> solutions;
    protected int iteration;

    public EasierPopulationDAO(){
        solutions = new ArrayList<>();
    }

    public EasierPopulationDAO(final List<RSolution<?>> sols){
        this();
        for (RSolution<?> sol : sols) {
            if(!EasierSolutionDAO.alreadyIn(sol.getName())) {
                EasierSolutionDAO easierSolutionDAO = new EasierSolutionDAO(sol);
                solutions.add(easierSolutionDAO);
            }
        }
    }

    public EasierPopulationDAO(final List<RSolution<?>> sols, int iteration){
        this(sols);
        this.iteration = iteration;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {}

    public List<EasierSolutionDAO> getSolutions() {
        return solutions;
    }
}
