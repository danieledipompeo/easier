package it.univaq.disim.sealab.metaheuristic.evolutionary.nsgaii;

import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.domain.EasierParetoDAO;
import it.univaq.disim.sealab.metaheuristic.domain.EasierPopulationDAO;
import it.univaq.disim.sealab.metaheuristic.evolutionary.EasierAlgorithm;
import it.univaq.disim.sealab.metaheuristic.evolutionary.ProgressBar;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
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

    // It will be exploited to identify stagnant situation
    List<S> oldPopulation;
    private long durationThreshold, iterationStartingTime;
    private float prematureConvergenceThreshold;

    /**
     * Constructor matingPopulationSize = offspringPopulationSize = populationSize
     * as used in NSGAIIBuilder
     */
    public CustomNSGAII(Problem<S> problem, int maxIterations, int populationSize,
                        CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                        SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
        super(problem, maxIterations, populationSize, populationSize, populationSize, crossoverOperator,
                mutationOperator, selectionOperator, evaluator);

        durationThreshold = Configurator.eINSTANCE.getStoppingCriterionTimeThreshold();
        prematureConvergenceThreshold = Configurator.eINSTANCE.getStoppingCriterionPrematureConvergenceThreshold();
        oldPopulation = new ArrayList<>();
    }

    /**
     * Prints to CSV each generated population
     * "algorithm,problem_tag,solID,perfQ,#changes,pas,reliability"
     *
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
        EasierResourcesLogger.checkpoint(getName(),"initProgess_start");
        super.initProgress();
        EasierResourcesLogger.checkpoint(getName(),"initProgess_end");

        iterationStartingTime = System.currentTimeMillis();
        oldPopulation = this.getPopulation(); // store the initial population
    }

    @Override
    protected void updateProgress() {
        EasierExperimentDAO.eINSTANCE.addPareto(new EasierParetoDAO((List<RSolution<?>>) population,
                evaluations / getMaxPopulationSize() ));

        EasierResourcesLogger.checkpoint(getName(),"updateProgress_start");
        super.updateProgress();
        EasierResourcesLogger.checkpoint(getName(),"updateProgress_end");
        EasierResourcesLogger.checkpoint(getName(),"iteration_end");

        populationToCSV();
        System.out.println(this.getName());
        ProgressBar.showBar((evaluations / getMaxPopulationSize()), (maxEvaluations / getMaxPopulationSize()));
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

    @Override
    protected List<S> createInitialPopulation() {
        EasierResourcesLogger.checkpoint(getName(),"createInitialPopulation_start");
        List<S> pop = super.createInitialPopulation();
        EasierResourcesLogger.checkpoint(getName(),"createInitialPopulation_end");
        JMetalLogger.logger.info("Initial population created");
        return pop;
    }

    @Override
    protected List<S> selection(List<S> pop) {
        EasierResourcesLogger.iterationCheckpointStart(getName(), "iteration_start");
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
    protected List<S> evaluatePopulation(List<S> population) {
        EasierResourcesLogger.checkpoint(getName(),"evaluatePopulation_end");
        List<S> evaluatedPop = super.evaluatePopulation(population);
        EasierResourcesLogger.checkpoint(getName(),"evaluatePopulation_end");
        return evaluatedPop;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        EasierResourcesLogger.checkpoint(getName(),"replacement_start");
        List<S> replacedPop = super.replacement(population, offspringPopulation);
        EasierResourcesLogger.checkpoint(getName(),"replacement_end");
        return replacedPop;
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
                evaluations / getMaxPopulationSize() - 1, maxEvaluations / getMaxPopulationSize()));
    }

    @Override
    public String getDescription() {
        return "Nondominated Sorting Genetic Algorithm version II. Version using measures";
    }

    public void clear() {
        for (S sol : this.getPopulation()) {
            sol.setParents(null, null);
        }
        this.getPopulation().clear();
    }

}
