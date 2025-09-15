package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;

import java.util.List;

public class EasierParetoDAO extends EasierPopulationDAO {

    int independentRun = -1;
    int iteration;

    /**
     * Constructor for EasierParetoDAO
     * This constructor initializes the EasierParetoDAO with a list of solutions and an iteration number.
     * It also saves the population to a JSON file using the FileUtils class.
     *
     * @param solPareto
     * @param it
     */
    public EasierParetoDAO(List<RSolution<?>> solPareto, int it) {
        super();
        addPopulation(solPareto);
        this.iteration = it;
        new FileUtils().populationToJSON(this, it);
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
