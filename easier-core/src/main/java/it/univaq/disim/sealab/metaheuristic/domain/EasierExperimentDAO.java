package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierLogger;

import java.util.ArrayList;
import java.util.List;

public class EasierExperimentDAO {

    List<EasierSolutionDAO> population;
    List<EasierParetoDAO> pareto;
    List<EasierParetoDAO> superPareto;
    Configurator configuration;

    public static final EasierExperimentDAO eINSTANCE = new EasierExperimentDAO();

    private EasierExperimentDAO(){
        configuration = Configurator.eINSTANCE;
        population = new ArrayList<>();
        pareto = new ArrayList<>();
        superPareto = new ArrayList<>();
    };

    /**
     * Add a solution to the population of the experiment for the export to JSON
     * if the solution is already in the population, it is not added
     *
     * @param solution
     */
    public void addPopulation(RSolution<?> solution){
        if(!EasierSolutionDAO.alreadyIn(solution.getName())) {
            population.add(new EasierSolutionDAO(solution));
//            EasierLogger.logger_.info("Added solution " + solution.getName() + " to the DAO population");
        } else {
            EasierLogger.logger_.info("Solution " + solution.getName() + " already in the DAO population");
        }
    }

    public void addPopulation(EasierPopulationDAO populationDAO){
        populationDAO.getSolutions().forEach(solution -> population.add(solution));
    }

    public List<EasierParetoDAO> getSuperPareto() {
        return superPareto;
    }

    public void addPareto(EasierParetoDAO paretoDAO){
        pareto.add(paretoDAO);
    }
    public void addSuperPareto(EasierParetoDAO paretoDAO){
        superPareto.add(paretoDAO);
    }

    public List<EasierSolutionDAO> getPopulation() {
        return population;
    }

    public List<EasierParetoDAO> getPareto() {
        return pareto;
    }

    public Configurator getConfiguration() {
        return configuration;
    }
}
