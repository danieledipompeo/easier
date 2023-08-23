package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;

import java.util.ArrayList;
import java.util.List;

public class EasierPopulationDAO {

    protected List<EasierSolutionDAO> solutions;

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

    public List<EasierSolutionDAO> getSolutions() {
        return solutions;
    }
}
