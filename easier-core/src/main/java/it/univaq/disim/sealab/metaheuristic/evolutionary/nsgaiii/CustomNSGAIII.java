package it.univaq.disim.sealab.metaheuristic.evolutionary.nsgaiii;

import it.univaq.disim.sealab.metaheuristic.evolutionary.EasierAlgorithm;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.support.CheckpointPhases;
import it.univaq.disim.sealab.metaheuristic.evolutionary.support.PopulationCsvSupport;
import it.univaq.disim.sealab.metaheuristic.evolutionary.support.SearchBudgetPolicy;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierLogger;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIII;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.util.JMetalLogger;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CustomNSGAIII<S extends RSolution<?>> extends NSGAIII<S> implements EasierAlgorithm {

    private final SearchBudgetPolicy searchBudget = new SearchBudgetPolicy();
    private final float prematureConvergenceThreshold;
    // It will be exploited to identify stagnant situation
    List<S> oldPopulation;

    /**
     * Constructor matingPopulationSize = offspringPopulationSize = populationSize
     * as used in NSGAIIIBuilder
     */
    public CustomNSGAIII(NSGAIIIBuilder<S> builder) {
        super(builder);

        prematureConvergenceThreshold = Configurator.eINSTANCE.getStoppingCriterionPrematureConvergenceThreshold();
        oldPopulation = new ArrayList<>();
    }

    /**
     * Prints to CSV each generated population
     * "algorithm,problem_tag,solID,perfQ,#changes,pas,reliability"
     */
    public void populationToCSV() {
        PopulationCsvSupport.dumpPopulation(getName(), getProblem().getName(), this.getPopulation(), true);
    }

    /**
     * Support multiple stopping criteria.
     * <ul>
     *     <li><b>byTime</b> the default computing threshold is set to 1 h</li>
     *     <li><b>byPrematureConvergence</b> the default premature convergence is set to 3 consecutive populations with the same objectives</li>
     *     <li><b>byBoth</b> using byTime and byPrematureConvergence classic using the number of evaluation</li>
     *     <li><b>none</b></li> using the default stopping criterion based on the number of evolutions
     * </ul>
     */
    @Override
    public boolean isStoppingConditionReached() {

        if (Configurator.eINSTANCE.isSearchBudgetByTime()) // byTime
            return super.isStoppingConditionReached() || searchBudget.isTimeExceeded();
//        if (Configurator.eINSTANCE.isSearchBudgetByPrematureConvergence()) // byPrematureConvergence
//            return super.isStoppingConditionReached() || isStagnantState();
//         computeStagnantState
//        if (Configurator.eINSTANCE.isSearchBudgetByPrematureConvergenceAndTime()) // byBoth
//            return super.isStoppingConditionReached() || isStagnantState() || currentComputingTime > durationThreshold;
        return super.isStoppingConditionReached(); // classic

    }

    @Override
    protected void initProgress() {
        CheckpointPhases.phase(getName(), "initProgess", super::initProgress);

        searchBudget.start();
        oldPopulation = this.getPopulation(); // store the initial population
    }

    @Override
    protected void updateProgress() {
        CheckpointPhases.recordParetoFront(population, iterations);

        CheckpointPhases.phase(getName(), "updateProgress", (Runnable) super::updateProgress);
        EasierResourcesLogger.checkpoint(getName(), "iteration_end");

//        populationToCSV();
//        FIX: the following method has been moved to EasierParetoDAO constructor
//        new FileUtils().populationToJSON(paretoDAO, paretoDAO.getIteration());

        CheckpointPhases.printProgress(getName(), iterations, maxIterations);
    }

    @Override
    protected List<S> createInitialPopulation() {
        EasierLogger.logger_.info("Creating initial population");
        List<S> pop = CheckpointPhases.phase(getName(), "createInitialPopulation", super::createInitialPopulation);
        JMetalLogger.logger.info("Initial population created: " + pop.size() + " solutions");
        return pop;
    }

    @Override
    protected List<S> selection(List<S> pop) {
        EasierResourcesLogger.iterationCheckpointStart(getName(), "iteration_start");
        return CheckpointPhases.phase(getName(), "selection", () -> super.selection(pop));
    }

    @Override
    protected List<S> reproduction(List<S> matingPool) {
        return CheckpointPhases.phase(getName(), "reproduction", () -> super.reproduction(matingPool));
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        return CheckpointPhases.phase(getName(), "evaluatePopulation", () -> super.evaluatePopulation(population));
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        return CheckpointPhases.phase(getName(), "replacement", () -> super.replacement(population, offspringPopulation));
    }

    @Override
    public void run() {
        EasierLogger.logger_.info("Running NSGAIII");
        CheckpointPhases.phase(getName(), "run", (Runnable) super::run);

        /*
         * prints the number of iterations until the search budget is not reached.
         * !!!Attn!!! evaluations / getMaxPopulationSize() -1 is required because
         * iterations has been updated just before checking the stopping criteria
         * !!!Attn!!!
         */
        PopulationCsvSupport.dumpSearchBudget(getName(), getProblem().getName(),
                Configurator.eINSTANCE.getSearchBudgetType(),
                iterations / getMaxPopulationSize() - 1, maxIterations / getMaxPopulationSize());
    }

    public void clear() {
        PopulationCsvSupport.clear(this.getPopulation());
    }

}
