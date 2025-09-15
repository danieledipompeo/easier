package it.univaq.disim.sealab.metaheuristic.evolutionary.nsgaiii;

import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.domain.EasierParetoDAO;
import it.univaq.disim.sealab.metaheuristic.evolutionary.EasierAlgorithm;
import it.univaq.disim.sealab.metaheuristic.evolutionary.ProgressBar;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierLogger;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIII;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.util.JMetalLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CustomNSGAIII<S extends RSolution<?>> extends NSGAIII<S> implements EasierAlgorithm {

    private final long durationThreshold;
    private final float prematureConvergenceThreshold;
    // It will be exploited to identify stagnant situation
    List<S> oldPopulation;
    private long iterationStartingTime;

    /**
     * Constructor matingPopulationSize = offspringPopulationSize = populationSize
     * as used in NSGAIIIBuilder
     */
    public CustomNSGAIII(NSGAIIIBuilder<S> builder) {
        super(builder);

        durationThreshold = Configurator.eINSTANCE.getStoppingCriterionTimeThreshold();
        prematureConvergenceThreshold = Configurator.eINSTANCE.getStoppingCriterionPrematureConvergenceThreshold();
        oldPopulation = new ArrayList<>();
    }

    /**
     * Prints to CSV each generated population
     * "algorithm,problem_tag,solID,perfQ,#changes,pas,reliability"
     */
    public void populationToCSV() {

        this.getPopulation().forEach(s -> {
            s.refactoringToCSV();
            String line = this.getName() + ',' + this.getProblem().getName() + ',' + s.objectiveToCSV();
            new FileUtils().solutionDumpToCSV(line);
        });
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

        long currentComputingTime = System.currentTimeMillis() - iterationStartingTime;

        if (Configurator.eINSTANCE.isSearchBudgetByTime()) // byTime
            return super.isStoppingConditionReached() || currentComputingTime > durationThreshold;
//        if (Configurator.eINSTANCE.isSearchBudgetByPrematureConvergence()) // byPrematureConvergence
//            return super.isStoppingConditionReached() || isStagnantState();
//         computeStagnantState
//        if (Configurator.eINSTANCE.isSearchBudgetByPrematureConvergenceAndTime()) // byBoth
//            return super.isStoppingConditionReached() || isStagnantState() || currentComputingTime > durationThreshold;
        return super.isStoppingConditionReached(); // classic

    }

    @Override
    protected void initProgress() {
        EasierResourcesLogger.checkpoint(getName(), "initProgess_start");
        super.initProgress();
        EasierResourcesLogger.checkpoint(getName(), "initProgess_end");

        iterationStartingTime = System.currentTimeMillis();
        oldPopulation = this.getPopulation(); // store the initial population
    }

    @Override
    protected void updateProgress() {
        EasierParetoDAO paretoDAO = new EasierParetoDAO((List<RSolution<?>>) population,
                iterations);
        EasierExperimentDAO.eINSTANCE.addPareto(paretoDAO);

        EasierResourcesLogger.checkpoint(getName(), "updateProgress_start");
        super.updateProgress();
        EasierResourcesLogger.checkpoint(getName(), "updateProgress_end");
        EasierResourcesLogger.checkpoint(getName(), "iteration_end");

//        populationToCSV();
//        FIX: the following method has been moved to EasierParetoDAO constructor
//        new FileUtils().populationToJSON(paretoDAO, paretoDAO.getIteration());

        System.out.println(this.getName());
        ProgressBar.showBar(iterations, maxIterations);
    }

    @Override
    protected List<S> createInitialPopulation() {
        EasierLogger.logger_.info("Creating initial population");
        EasierResourcesLogger.checkpoint(getName(), "createInitialPopulation_start");
        List<S> pop = super.createInitialPopulation();
        EasierResourcesLogger.checkpoint(getName(), "createInitialPopulation_end");
        JMetalLogger.logger.info("Initial population created: " + pop.size() + " solutions");
        return pop;
    }

    @Override
    protected List<S> selection(List<S> pop) {
        EasierResourcesLogger.iterationCheckpointStart(getName(), "iteration_start");
        EasierResourcesLogger.checkpoint(getName(), "selection_start");
        List<S> matingPopulation = super.selection(pop);
        EasierResourcesLogger.checkpoint(getName(), "selection_end");
        return matingPopulation;
    }

    @Override
    protected List<S> reproduction(List<S> matingPool) {
        EasierResourcesLogger.checkpoint(getName(), "reproduction_start");
        List<S> offspringPopulation = super.reproduction(matingPool);
        EasierResourcesLogger.checkpoint(getName(), "reproduction_end");
        return offspringPopulation;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        EasierResourcesLogger.checkpoint(getName(), "evaluatePopulation_end");
        List<S> evaluatedPop = super.evaluatePopulation(population);
        EasierResourcesLogger.checkpoint(getName(), "evaluatePopulation_end");
        return evaluatedPop;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        EasierResourcesLogger.checkpoint(getName(), "replacement_start");
        List<S> replacedPop = super.replacement(population, offspringPopulation);
        EasierResourcesLogger.checkpoint(getName(), "replacement_end");
        return replacedPop;
    }

    @Override
    public void run() {
        EasierLogger.logger_.info("Running NSGAIII");
        EasierResourcesLogger.checkpoint(getName(), "run_start");
        super.run();
        EasierResourcesLogger.checkpoint(getName(), "run_end");

        /*
         * prints the number of iterations until the search budget is not reached.
         * !!!Attn!!! evaluations / getMaxPopulationSize() -1 is required because
         * iterations has been updated just before checking the stopping criteria
         * !!!Attn!!!
         */
        new FileUtils().searchBudgetDumpToCSV(String.format("%s,%s,%s,%s,%s", this.getName(),
                this.getProblem().getName(), Configurator.eINSTANCE.getSearchBudgetType(),
                iterations / getMaxPopulationSize() - 1, maxIterations / getMaxPopulationSize()));
    }

    public void clear() {
        for (S sol : this.getPopulation()) {
            sol.setParents(null, null);
        }
        this.getPopulation().clear();
    }

}
