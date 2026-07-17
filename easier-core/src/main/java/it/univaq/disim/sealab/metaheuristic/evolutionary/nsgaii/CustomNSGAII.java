package it.univaq.disim.sealab.metaheuristic.evolutionary.nsgaii;

import it.univaq.disim.sealab.metaheuristic.evolutionary.EasierAlgorithm;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.support.CheckpointPhases;
import it.univaq.disim.sealab.metaheuristic.evolutionary.support.PopulationCsvSupport;
import it.univaq.disim.sealab.metaheuristic.evolutionary.support.SearchBudgetPolicy;
import it.univaq.disim.sealab.metaheuristic.utils.*;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CustomNSGAII<S extends RSolution<?>> extends NSGAII<S> implements EasierAlgorithm {

    private final SearchBudgetPolicy searchBudget = new SearchBudgetPolicy();
    private final float prematureConvergenceThreshold;
    // It will be exploited to identify stagnant situation
    List<S> oldPopulation;

    /**
     * Constructor matingPopulationSize = offspringPopulationSize = populationSize
     * as used in NSGAIIBuilder
     */
    public CustomNSGAII(Problem<S> problem, int maxIterations, int populationSize,
                        CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                        SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
        super(problem, maxIterations, populationSize, populationSize, populationSize, crossoverOperator,
                mutationOperator, selectionOperator, evaluator);

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
        CheckpointPhases.recordParetoFront(population, evaluations / getMaxPopulationSize());

        CheckpointPhases.phase(getName(), "updateProgress", (Runnable) super::updateProgress);
        EasierResourcesLogger.checkpoint(getName(), "iteration_end");

//        Population is now dumped to JSON file. When the EasierParedoDAO is created, it dumps the population to JSON.
//        populationToCSV();
        CheckpointPhases.printProgress(getName(), evaluations / getMaxPopulationSize(),
                maxEvaluations / getMaxPopulationSize());
    }

    @Override
    protected List<S> createInitialPopulation() {
        List<S> pop = CheckpointPhases.phase(getName(), "createInitialPopulation", super::createInitialPopulation);
        JMetalLogger.logger.info("Initial population created");
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
        CheckpointPhases.phase(getName(), "run", (Runnable) super::run);

        /*
         * prints the number of iterations until the search budget is not reached.
         * !!!Attn!!! evaluations / getMaxPopulationSize() -1 is required because
         * iterations has been updated just before checking the stopping criteria
         * !!!Attn!!!
         */
        PopulationCsvSupport.dumpSearchBudget(getName(), getProblem().getName(),
                Configurator.eINSTANCE.getSearchBudgetType(),
                evaluations / getMaxPopulationSize() - 1, maxEvaluations / getMaxPopulationSize());
    }

    @Override
    public String getDescription() {
        return "Nondominated Sorting Genetic Algorithm version II. Version using measures";
    }

    public void clear() {
        PopulationCsvSupport.clear(this.getPopulation());
    }

}
