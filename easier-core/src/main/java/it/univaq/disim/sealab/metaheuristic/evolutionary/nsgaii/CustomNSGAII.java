package it.univaq.disim.sealab.metaheuristic.evolutionary.nsgaii;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import it.univaq.disim.sealab.metaheuristic.evolutionary.EasierAlgorithm;
import it.univaq.disim.sealab.metaheuristic.evolutionary.ProgressBar;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.impl.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.PointSolution;

@SuppressWarnings("serial")
public class CustomNSGAII<S extends RSolution<?>> extends NSGAII<S> implements EasierAlgorithm {

    private long durationThreshold, iterationStartingTime;
    private float prematureConvergenceThreshold;

    // It will be exploited to identify stagnant situation
    List<S> oldPopulation;

    /**
     * Constructor matingPopulationSize = offspringPopulationSize = populationSize
     * as used in NSGAIIBuilder
     */
    public CustomNSGAII(Problem<S> problem, int maxIterations, int populationSize,
                        CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                        SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
        super((Problem<S>) problem, maxIterations, populationSize, populationSize, populationSize, crossoverOperator,
                mutationOperator, selectionOperator, evaluator);
        durationThreshold = Configurator.eINSTANCE.getStoppingCriterionTimeThreshold();
        prematureConvergenceThreshold = Configurator.eINSTANCE.getStoppingCriterionPrematureConvergenceThreshold();
        oldPopulation = new ArrayList<S>();
    }

//	@Override protected boolean isStoppingConditionReached() {
//		
//		return evaluations > maxEvaluations;
//	}

    /*
     * Prints to CSV each generated population
     * "algorithm,problem_tag,solID,perfQ,#changes,pas,reliability"
     *
     */
    void populationToCSV() {
//		super.updateProgress();

        for (RSolution<?> sol : population) {
            String line = this.getName() + ',' + this.getProblem().getName() + ',' + sol.objectiveToCSV();
            new FileUtils().solutionDumpToCSV(line);
        }
    }

    /**
     * Support multiple stopping criteria. byTime the default computing threshold is
     * set to 1 h byPrematureConvergence the default premature convergence is set to
     * 3 consecutive populations with the same objectives byBoth using byTime and
     * byPrematureConvergence classic using the number of evaluation
     */
    @Override
    protected boolean isStoppingConditionReached() {

        long currentComputingTime = System.currentTimeMillis() - iterationStartingTime;

        if (Configurator.eINSTANCE.isSearchBudgetByTime()) // byTime
            return super.isStoppingConditionReached() || currentComputingTime > durationThreshold;
        if (Configurator.eINSTANCE.isSearchBudgetByPrematureConvergence()) // byPrematureConvergence
            return super.isStoppingConditionReached() || isStagnantState();
        // computeStagnantState
        if (Configurator.eINSTANCE.isSearchBudgetByPrematureConvergenceAndTime()) // byBoth
            return super.isStoppingConditionReached() || isStagnantState() || currentComputingTime > durationThreshold;
        return super.isStoppingConditionReached(); // classic

    }

    @Override
    protected void initProgress() {
        super.initProgress();
        iterationStartingTime = System.currentTimeMillis();
        oldPopulation = (List<S>) this.getPopulation(); // store the initial population
        this.getPopulation().forEach(s -> s.refactoringToCSV());
    }

    public boolean isStagnantState() {
        // create a joined list of the current population and the old one
//		List<RSolution<?>> joinedPopulation = new ArrayList<>(oldPopulation);
//		joinedPopulation.addAll(population);

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
        oldPopulation = (List<S>) population;

        // check if all solutions within the joined list have the same objective values
        return ((double) (population.size() - countedSameObjectives / population.size())
                / population.size()) > prematureConvergenceThreshold;
    }

    private double computeQualityIndicator() {
        GenericIndicator<PointSolution> hyperVolume = new PISAHypervolume<>();
        Front referenceFront = null;
        try {
            referenceFront = new ArrayFront(removeSolID(Configurator.eINSTANCE.getReferenceFront().get(0).toString()), ",");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        FrontNormalizer frontNormalizer = new FrontNormalizer(referenceFront);
        Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);
        Front front = null;
        front = new ArrayFront(population);
        Front normalizedFront = frontNormalizer.normalize(front);
        List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);
        double hv = hyperVolume.evaluate(normalizedPopulation);
        String line = String.format("%s,%s,%s,%s", this.getName(), this.getProblem().getName(),
                hyperVolume.getName(), hv);
        new FileUtils().qualityIndicatorDumpToCSV(line);
        return hv;
    }

    public String removeSolID(String frontFileName) {

        File tmpFile;
        String tmpFileName = null;
        try {
            tmpFile = File.createTempFile("front", "");
            tmpFile.deleteOnExit();
            tmpFileName = tmpFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String readLine = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(frontFileName)); BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
            while ((readLine = reader.readLine()) != null) {
                if (!readLine.contains("solID")) {
//				} else {
                    String line = readLine.split(",", 2)[1];
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmpFileName;
    }

    @Override
    protected void updateProgress() {
        super.updateProgress();
        populationToCSV();

        computeQualityIndicator();

    }

    @Override
    public void run() {
        List<S> offspringPopulation;
        List<S> matingPopulation;

        this.setPopulation(createInitialPopulation());
        this.setPopulation(evaluatePopulation(this.getPopulation()));
        initProgress();
        while (!isStoppingConditionReached()) {

            System.out.println(this.getName());
            ProgressBar.showBar((evaluations / getMaxPopulationSize()), (maxEvaluations / getMaxPopulationSize()));

            long freeBefore = Runtime.getRuntime().freeMemory();
            long totalBefore = Runtime.getRuntime().totalMemory();

            long initTime = System.currentTimeMillis();

            matingPopulation = selection(this.getPopulation());
            offspringPopulation = reproduction(matingPopulation);
            offspringPopulation = evaluatePopulation(offspringPopulation);
            this.setPopulation(replacement(this.getPopulation(), offspringPopulation));

            long computingTime = System.currentTimeMillis() - initTime;

            long freeAfter = Runtime.getRuntime().freeMemory();
            long totalAfter = Runtime.getRuntime().totalMemory();

            new FileUtils().algoPerfStatsDumpToCSV(String.format("%s,%s,%s,%s,%s,%s,%s", this.getName(), this.getProblem().getName(),
                    computingTime, totalBefore, freeBefore, totalAfter, freeAfter));

            updateProgress();
//			populationToCSV();

        }

        /* prints the number of iterations until the search budget is not reached.
         * !!!Attn!!!
         * evaluations / getMaxPopulationSize() -1
         * is required because evaluations has been updated just before checking the stopping criteria
         * !!!Attn!!!
         */
        new FileUtils().searchBudgetDumpToCSV(String.format("%s,%s,%s,%s,%s", this.getName(), this.getProblem().getName(),
                Configurator.eINSTANCE.getSearchBudgetType(), evaluations / getMaxPopulationSize() - 1,
                maxEvaluations / getMaxPopulationSize()));
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
