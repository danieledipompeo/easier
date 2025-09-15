package it.univaq.disim.sealab.metaheuristic.evolutionary.ibea;

import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.domain.EasierParetoDAO;
import it.univaq.disim.sealab.metaheuristic.evolutionary.EasierAlgorithm;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierLogger;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
import org.uma.jmetal.algorithm.multiobjective.ibea.IBEA;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.List;

public class CustomIBEA<S extends RSolution<?>> extends IBEA<S> implements EasierAlgorithm {

    private final long durationThreshold;
    private int evaluations = 0;
    private long iterationStartingTime;
    private final SolutionListEvaluator<S> solutionListEvaluator;

    public CustomIBEA(Problem<S> problem, int populationSize, int archiveSize, int maxEvaluations,
                      SelectionOperator<List<S>, S> selectionOperator, CrossoverOperator<S> crossoverOperator,
                      MutationOperator<S> mutationOperator, SolutionListEvaluator<S> solEval) {
        super(problem, populationSize, archiveSize, maxEvaluations, selectionOperator, crossoverOperator, mutationOperator);
        durationThreshold = Configurator.eINSTANCE.getStoppingCriterionTimeThreshold();
        this.solutionListEvaluator = solEval;
    }

    // TODO: check if the constructor of EasierParetoDAO is dumping the pareto to JSON correctly.
    //  It might overwrite the one at line 57
    public void run() {

        List<S> evaluatedOffspring = new ArrayList<>();
        iterationStartingTime = System.currentTimeMillis();
        List<S> solutionSet = new ArrayList<>(this.populationSize);
        this.archive = new ArrayList<>(this.archiveSize);
//        int evaluations = 0;
        EasierResourcesLogger.checkpoint(getName(), "createInitialPopulation_start");
        for (int i = 0; i < this.populationSize; ++i) {
            S newSolution = this.problem.createSolution();
//            this.problem.evaluate(newSolution);
            ++evaluations;
            solutionSet.add(newSolution);
        }

        evaluatedOffspring = solutionListEvaluator.evaluate(solutionSet, this.problem);

        EasierResourcesLogger.checkpoint(getName(), "createInitialPopulation_end");

        // Add the population generated in the current iteration (evaluations / this.populationSize) to the experiment
        EasierExperimentDAO.eINSTANCE.addPareto(new EasierParetoDAO((List<RSolution<?>>) evaluatedOffspring,
                evaluations / this.populationSize));

        EasierResourcesLogger.checkpoint(getName(), "run_start");
        while (this.isStoppingConditionReached()) {
            List<S> union = new ArrayList();
            union.addAll(evaluatedOffspring);
            union.addAll(this.archive);
            this.calculateFitness(union);
            EasierLogger.logger_.info("IBEA fitness:" + this.indicatorValues);
            this.archive = union;

            while (this.archive.size() > this.populationSize) {
                this.removeWorst(this.archive);
            }

            ArrayList<S> offSpringSolutionSet;
            for (offSpringSolutionSet = new ArrayList<>(this.populationSize);
                 offSpringSolutionSet.size() < this.populationSize; ++evaluations) {
                int j = 0;

                S parent1;
                do {
                    ++j;
                    parent1 = this.selectionOperator.execute(this.archive);
                } while (j < 1);

                int k = 0;

                S parent2;
                do {
                    ++k;
                    parent2 = this.selectionOperator.execute(this.archive);
                } while (k < 1);

                EasierResourcesLogger.checkpoint(getName(), "reproduction_start");
                List<S> parents = new ArrayList<>(2);
                parents.add(parent1);
                parents.add(parent2);
                List<S> offspring = this.crossoverOperator.execute(parents);
                this.mutationOperator.execute(offspring.get(0));
//                this.problem.evaluate(offspring.get(0));
                evaluatedOffspring = solutionListEvaluator.evaluate(offspring, this.problem);

                offSpringSolutionSet.add(evaluatedOffspring.get(0));

                EasierResourcesLogger.checkpoint(getName(), "reproduction_end");
            }

            solutionSet = offSpringSolutionSet;

            // Add the population generated in the current iteration (evaluations / this.populationSize) to the experiment
            EasierExperimentDAO.eINSTANCE.addPareto(new EasierParetoDAO((List<RSolution<?>>) solutionSet,
                    evaluations / this.populationSize));

        }
        EasierResourcesLogger.checkpoint(getName(), "run_end");

    }


    @Override
    public void clear() {

    }

    @Override
    public void populationToCSV() {
        this.archive.forEach(s -> {
            s.refactoringToCSV();
            String line = this.getName() + ',' + this.problem.getName() + ',' + s.objectiveToCSV();
            new FileUtils().solutionDumpToCSV(line);
        });
    }

    @Override
    public boolean isStoppingConditionReached() {
        EasierLogger.logger_.info(String.format("IBEA evaluations / max evaluation: %s / %s", evaluations, this.maxEvaluations));
        long currentComputingTime = System.currentTimeMillis() - iterationStartingTime;

        if (Configurator.eINSTANCE.isSearchBudgetByTime()) // byTime
            return evaluations < this.maxEvaluations || currentComputingTime > durationThreshold;
//        if (Configurator.eINSTANCE.isSearchBudgetByPrematureConvergence()) // byPrematureConvergence
//            return super.isStoppingConditionReached() || isStagnantState();
//         computeStagnantState
//        if (Configurator.eINSTANCE.isSearchBudgetByPrematureConvergenceAndTime()) // byBoth
//            return super.isStoppingConditionReached() || isStagnantState() || currentComputingTime > durationThreshold;
        return evaluations < this.maxEvaluations; // classic
    }
}
