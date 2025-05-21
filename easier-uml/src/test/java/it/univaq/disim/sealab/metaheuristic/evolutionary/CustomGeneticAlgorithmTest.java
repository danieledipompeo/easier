package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.ObjectiveEstimator;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.RSolutionListEvaluator;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRCrossover;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRMutation;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomGeneticAlgorithmTest<S extends UMLRSolution> extends CustomAlgorithmTest<S> {

    protected final CrossoverOperator<S> crossoverOperator = new UMLRCrossover<>(
            Configurator.eINSTANCE.getXoverProbabiliy());
    protected final MutationOperator<S> mutationOperator = new UMLRMutation<>(
            Configurator.eINSTANCE.getMutationProbability(), Configurator.eINSTANCE.getDistributionIndex());

    protected final SelectionOperator<List<S>, S> selectionOpertor = new BinaryTournamentSelection<>(
            new RankingAndCrowdingDistanceComparator<>());
//    protected final SolutionListEvaluator<S> solutionListEvaluator = new SequentialSolutionListEvaluator<>();
    protected final SolutionListEvaluator<S> solutionListEvaluator = new RSolutionListEvaluator<>();

    public void updateProgressTest() throws IOException, EasierException {

         List<S> fakePopulation = createFakePopulation(1);

        solutions.addAll(fakePopulation);

        ((AbstractGeneticAlgorithm<S, List<S>>) algorithm).setPopulation(solutions);

    }

    private List<S> createFakePopulation(int numElement) throws EasierException {
        List<S>  fakePop = new ArrayList<>();
        for(int i = 0; i < numElement; i++){
            S solution = p.createSolution();
            new ObjectiveEstimator().computeObjectives(solution);
            new ObjectiveEstimator().setConsideredObjectives(solution);
            fakePop.add(solution);
        }

        return fakePop;
    }


}
