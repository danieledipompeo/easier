package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.utils.Configurator;

import java.util.ArrayList;
import java.util.List;

public class EasierExperimentDAO {

    List<EasierPopulationDAO> population;
    List<EasierParetoDAO> pareto;
    Configurator configuration;

    public static final EasierExperimentDAO eINSTANCE = new EasierExperimentDAO();

    private EasierExperimentDAO(){
        configuration = Configurator.eINSTANCE;
        population = new ArrayList<>();
        pareto = new ArrayList<>();
    }

    public void addPopulation(EasierPopulationDAO populationDAO){
        population.add(populationDAO);
    }

    public void addPareto(EasierParetoDAO paretoDAO){
        pareto.add(paretoDAO);
    }

    public List<EasierPopulationDAO> getPopulation() {
        return population;
    }

    public List<EasierParetoDAO> getPareto() {
        return pareto;
    }

    public Configurator getConfiguration() {
        return configuration;
    }
}
