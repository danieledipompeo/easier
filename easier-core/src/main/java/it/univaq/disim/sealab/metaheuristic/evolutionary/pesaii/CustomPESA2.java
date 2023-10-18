package it.univaq.disim.sealab.metaheuristic.evolutionary.pesaii;

import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.domain.EasierParetoDAO;
import it.univaq.disim.sealab.metaheuristic.evolutionary.EasierAlgorithm;
import it.univaq.disim.sealab.metaheuristic.evolutionary.ProgressBar;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
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
    private long durationThreshold, iterationStartingTime;
    private float prematureConvergenceThreshold;
    private int _evaluations;


    public CustomPESA2(Problem<S> problem, int maxEvaluations, int populationSize, int archiveSize, int biSections,
                       CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                       SolutionListEvaluator<S> evaluator) {

        super(problem, maxEvaluations, populationSize, archiveSize, biSections, crossoverOperator,
                mutationOperator, evaluator);
        _maxEvaluations = maxEvaluations;

        durationThreshold = Configurator.eINSTANCE.getStoppingCriterionTimeThreshold();
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
        long currentComputingTime = System.currentTimeMillis() - iterationStartingTime;

        if (Configurator.eINSTANCE.isSearchBudgetByTime()) // byTime
            return super.isStoppingConditionReached() || currentComputingTime > durationThreshold;
//        if (Configurator.eINSTANCE.isSearchBudgetByPrematureConvergence()) // byPrematureConvergence
//            return super.isStoppingConditionReached() || isStagnantState();
//        if (Configurator.eINSTANCE.isSearchBudgetByPrematureConvergenceAndTime()) // byBoth
//            return super.isStoppingConditionReached() || isStagnantState() || currentComputingTime > durationThreshold;
        return super.isStoppingConditionReached();
    }

    @Override
    protected void initProgress() {
        EasierResourcesLogger.checkpoint(getName(),"initProgress_start");
        super.initProgress();
        EasierResourcesLogger.checkpoint(getName(),"initProgress_end");

        _evaluations = this.getMaxPopulationSize();
        this.getPopulation().forEach(s -> s.refactoringToCSV());
        iterationStartingTime = System.currentTimeMillis();

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
        for (RSolution<?> sol : this.getResult()) {
            String line = this.getName() + ',' + this.getProblem().getName() + ',' + sol.objectiveToCSV();
            new FileUtils().solutionDumpToCSV(line);
        }
    }

    @Override
    protected void updateProgress() {
        EasierExperimentDAO.eINSTANCE.addPareto(new EasierParetoDAO((List<RSolution<?>>) getResult(),
                _evaluations / getMaxPopulationSize()));
        EasierResourcesLogger.checkpoint(getName(),"updateProgress_start");
        super.updateProgress();
        // store the duration and the occupied memory by each step
        EasierResourcesLogger.checkpoint(getName(),"updateProgress_end");
        EasierResourcesLogger.checkpoint(getName(),"iteration_end");

        populationToCSV();
        _evaluations += this.getMaxPopulationSize();

        System.out.println(this.getName());
        ProgressBar.showBar(_evaluations / getMaxPopulationSize(), _maxEvaluations / getMaxPopulationSize());
    }

    @Override
    protected List<S> createInitialPopulation() {
        EasierResourcesLogger.checkpoint(getName(),"createInitialPopulation_start");
        List<S> pop = super.createInitialPopulation();
        EasierResourcesLogger.checkpoint(getName(),"createInitialPopulation_end");
        return pop;
    }

    @Override
    protected List<S> selection(List<S> pop) {
        EasierResourcesLogger.iterationCheckpointStart(getName(),"iteration_start");
        EasierResourcesLogger.checkpoint(getName(),"selection_start");
        List<S> matingPopulation = super.selection(pop);
        EasierResourcesLogger.checkpoint(getName(),"selection_end");
        return matingPopulation;
    }

    @Override
    protected List<S> reproduction(List<S> matingPool) {
        EasierResourcesLogger.checkpoint(getName(),"reproduction_start");
        List<S> offspringPopulation = super.reproduction(matingPool);
        EasierResourcesLogger.checkpoint(getName(),"reproduction_end");
        return offspringPopulation;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        EasierResourcesLogger.checkpoint(getName(),"replacement_start");
        List<S> replacedPop = super.replacement(population, offspringPopulation);
        EasierResourcesLogger.checkpoint(getName(),"replacement_end");
        return replacedPop;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        EasierResourcesLogger.checkpoint(getName(),"evaluatePopulation_end");
        List<S> evaluatedPop = super.evaluatePopulation(population);
        EasierResourcesLogger.checkpoint(getName(),"evaluatePopulation_end");
        return evaluatedPop;
    }

    @Override
    public void run() {
        EasierResourcesLogger.checkpoint(getName(),"run_start");
        super.run();
        EasierResourcesLogger.checkpoint(getName(),"run_end");

        /*
         * prints the number of iterations until the search budget is not reached.
         * !!!Attn!!! evaluations / getMaxPopulationSize() -1 is required because
         * iterations has been updated just before checking the stopping criteria
         * !!!Attn!!!
         */
        new FileUtils().searchBudgetDumpToCSV(String.format("%s,%s,%s,%s,%s", this.getName(),
                this.getProblem().getName(), Configurator.eINSTANCE.getSearchBudgetType(),
                _evaluations / getMaxPopulationSize() - 1, _maxEvaluations / getMaxPopulationSize()));
    }

    public void clear() {
        for (S sol : this.getPopulation()) {
            sol.setParents(null, null);
        }
        this.getPopulation().clear();
    }

}
