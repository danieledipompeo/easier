package it.univaq.disim.sealab.metaheuristic.evolutionary.pesaii;

import it.univaq.disim.sealab.metaheuristic.evolutionary.EasierAlgorithm;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.support.CheckpointPhases;
import it.univaq.disim.sealab.metaheuristic.evolutionary.support.PopulationCsvSupport;
import it.univaq.disim.sealab.metaheuristic.evolutionary.support.SearchBudgetPolicy;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import org.uma.jmetal.algorithm.multiobjective.pesa2.PESA2;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.List;

public class CustomPESA2<S extends RSolution<?>> extends PESA2<S> implements EasierAlgorithm {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    int _maxEvaluations;
    List<S> oldPopulation;
    private final SearchBudgetPolicy searchBudget = new SearchBudgetPolicy();
    private float prematureConvergenceThreshold;
    private int _evaluations;


    public CustomPESA2(Problem<S> problem, int maxEvaluations, int populationSize, int archiveSize, int biSections,
                       CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                       SolutionListEvaluator<S> evaluator) {

        super(problem, maxEvaluations, populationSize, archiveSize, biSections, crossoverOperator,
                mutationOperator, evaluator);
        _maxEvaluations = maxEvaluations;

        prematureConvergenceThreshold = Configurator.eINSTANCE.getStoppingCriterionPrematureConvergenceThreshold();
        oldPopulation = new ArrayList<S>();
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
//        if (Configurator.eINSTANCE.isSearchBudgetByPrematureConvergenceAndTime()) // byBoth
//            return super.isStoppingConditionReached() || isStagnantState() || currentComputingTime > durationThreshold;
        return super.isStoppingConditionReached();
    }

    @Override
    protected void initProgress() {
        CheckpointPhases.phase(getName(), "initProgress", super::initProgress);

        _evaluations = this.getMaxPopulationSize();
        this.getPopulation().forEach(s -> s.refactoringToCSV());
        searchBudget.start();

        // store the initial population
        oldPopulation = this.getPopulation();
    }

    /*public boolean isStagnantState() {

        int countedSameObjectives = 0;
        for (int i = 0; i < oldPopulation.size(); i++) {
            for (int j = 0; j < population.size(); j++) {
                if (!oldPopulation.get(i).isLocalOptmimalPoint(population.get(j))) {
                    break;
                }
                countedSameObjectives++;
            }
        }

        // update oldPopulation to the current population
        oldPopulation = population;

        // check if all solutions within the joined list have the same objective values
        return ((double) (population.size() - countedSameObjectives / population.size())
                / population.size()) > prematureConvergenceThreshold;
    }*/

    /**
     * Prints to CSV each generated population
     * "algorithm,problem_tag,solID,perfQ,#changes,pas,reliability"
     */
    public void populationToCSV() {
        PopulationCsvSupport.dumpPopulation(getName(), getProblem().getName(), this.getResult(), false);
    }

    @Override
    protected void updateProgress() {
        CheckpointPhases.recordParetoFront(getResult(), _evaluations / getMaxPopulationSize());
        CheckpointPhases.phase(getName(), "updateProgress", (Runnable) super::updateProgress);
        // store the duration and the occupied memory by each step
        EasierResourcesLogger.checkpoint(getName(),"iteration_end");

//        The population is dumped to JSON file for each iteration. Look at the EasierParetoDAO class
//        populationToCSV();
        _evaluations += this.getMaxPopulationSize();

        CheckpointPhases.printProgress(getName(), _evaluations / getMaxPopulationSize(),
                _maxEvaluations / getMaxPopulationSize());
    }

    @Override
    protected List<S> createInitialPopulation() {
        return CheckpointPhases.phase(getName(), "createInitialPopulation", super::createInitialPopulation);
    }

    @Override
    protected List<S> selection(List<S> pop) {
        EasierResourcesLogger.iterationCheckpointStart(getName(),"iteration_start");
        return CheckpointPhases.phase(getName(), "selection", () -> super.selection(pop));
    }

    @Override
    protected List<S> reproduction(List<S> matingPool) {
        return CheckpointPhases.phase(getName(), "reproduction", () -> super.reproduction(matingPool));
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        return CheckpointPhases.phase(getName(), "replacement", () -> super.replacement(population, offspringPopulation));
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        return CheckpointPhases.phase(getName(), "evaluatePopulation", () -> super.evaluatePopulation(population));
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
                _evaluations / getMaxPopulationSize() - 1, _maxEvaluations / getMaxPopulationSize());
    }

    public void clear() {
        PopulationCsvSupport.clear(this.getPopulation());
    }

}
