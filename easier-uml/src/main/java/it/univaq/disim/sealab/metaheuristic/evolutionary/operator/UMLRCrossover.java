package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.actions.UMLRefactoring;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//public class UMLRCrossover<S extends UMLRSolution> extends RCrossover<S> {
public class UMLRCrossover<S extends UMLRSolution> extends RCrossover<S> {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * List of the offspring solutions that are candidates for crossover
     */
    private static final ArrayList<UMLRSolution> crossoverCandidates = new ArrayList<>();

    /**
     * Constructor
     */
    public UMLRCrossover(double crossoverProbability) {
        super(crossoverProbability);

//        easierResourcesLogger = new EasierResourcesLogger("UMLCrossoverOperator");
    }

    int crossoverPoint;
    /**
     * Perform the crossover operation.
     * <p>
     * The result can be
     * <ul>
     *     <li>parent1, parent2 : when any crossover operation did not take place</li>
     *     <li>child1,child2 : when the crossover operation took place</li>
     * </ul>
     *
     * @param probability Crossover setProbability
     * @param parent1     The first parent
     * @param parent2     The second parent
     * @return A list containing the two solutions
     */
    public List<UMLRSolution> doCrossover(double probability, UMLRSolution parent1, UMLRSolution parent2) {

        // Store elapsed time and consumed memory before applying the crossover
        easierResourcesLogger.checkpoint("UMLCrossoverOperator", "doCrossover_start");

        List<UMLRSolution> offspring = new ArrayList<>(2);

        UMLRSolution parent1copy = new UMLRSolution(parent1);
        offspring.add(parent1copy);

        UMLRSolution parent2copy = new UMLRSolution(parent2);
        offspring.add(parent2copy);

        if (JMetalRandom.getInstance().nextDouble() < probability) {
            // Get the length of a solution
            int refactoringLength = Configurator.eINSTANCE.getLength();

            Path sourceModelPath = parent1.getSourceModelPath();
            String problemName = parent1.getProblemName();

            // Create the offspring
            UMLRSolution child1 = new UMLRSolution(sourceModelPath, problemName);
            UMLRSolution child2 = new UMLRSolution(sourceModelPath, problemName);

            // Set offspring parents
            child1.setParents(parent1, parent2);
            child2.setParents(parent2, parent1);

            // Extract all possible independent sequence of refactoring actions for the subsequent crossover operation
            Map<Integer, List<List<RefactoringAction>>> parent1IndependentSequence = independentSequence(parent1);
            Map<Integer, List<List<RefactoringAction>>> parent2IndependentSequence = independentSequence(parent2);

            // Find a feasible crossover point.
            crossoverPoint = extractCrossoverPoint(refactoringLength, parent1IndependentSequence, parent2IndependentSequence);

            // Check if a crossover point exists. If the crossover point is -1, it will return the offspring with parent1, and parent2
            if (crossoverPoint == -1) {
                JMetalLogger.logger.warning(String.format("Impossible to find a feasible crossover point for solution : %s \t %S", parent1.getName(), parent2.getName()));
                return offspring;
            }

            // Create offspring refactoring by combining the two parents using the crossover point
            Refactoring child1Refactoring = createChild(refactoringLength, crossoverPoint, child1.getModelPath().toString(), parent1IndependentSequence, parent2IndependentSequence);
            Refactoring child2Refactoring = createChild(refactoringLength, crossoverPoint, child2.getModelPath().toString(), parent2IndependentSequence, parent1IndependentSequence);

            // Safety check
            if (child1Refactoring == null || child2Refactoring == null) {
                JMetalLogger.logger.warning(String.format("At least one child of solutions (%s, %s) is unfeasible.", parent1.getName(), parent2.getName()));
                return offspring;
            }

            child1Refactoring.setSolutionID(child1.getName());
            child1.setVariable(0, child1Refactoring);
            child1.setCrossovered(true);

            child2Refactoring.setSolutionID(child2.getName());
            child2.setVariable(0, child2Refactoring);
            child2.setCrossovered(true);

            // Remove the copy of the parent1, and parent2 from the offspring
            offspring.set(0, child1);
            offspring.set(1, child2);

            // Add the offsprings to the list of candidates
            crossoverCandidates.addAll(offspring);
        }

        // Store elapsed time and consumed memory by the crossover operator
        easierResourcesLogger.checkpoint("UMLCrossoverOperator", "do_crossover_end");

//        easierResourcesLogger.toCSV();

        // It can be equal to parent1, parent2; child1,child2;
        return offspring;
    }

    /**
     * Check if a crossover point exist. It tries every combination among possible crossover points.
     *
     * @param refactoringLength
     * @param parent1IndependentSequence
     * @param parent2IndependentSequence
     * @return either the feasible crossopoint or -1
     */
    private int extractCrossoverPoint(int refactoringLength, Map<Integer, List<List<RefactoringAction>>> parent1IndependentSequence,
                                      Map<Integer, List<List<RefactoringAction>>> parent2IndependentSequence) {

        // Generate a crossover point until it is a feasible crossoverPoint
        List<Integer> possibleCrossoverPoints = IntStream.range(1, refactoringLength).boxed().collect(Collectors.toList());
        // Try all possible crossover points
        while (!possibleCrossoverPoints.isEmpty()) {
            // Extract a crossover point randomly
            int selected = JMetalRandom.getInstance().nextInt(0, possibleCrossoverPoints.size()-1);
            int crossoverPoint = possibleCrossoverPoints.get(selected);
            possibleCrossoverPoints.remove(selected);

            // Check if crossoverPoint is a feasible point. If not, the point is removed from the possibleCrossoverPoints list
            if (!parent1IndependentSequence.get(crossoverPoint).isEmpty() && !parent2IndependentSequence.get(refactoringLength - crossoverPoint).isEmpty()
                    && !parent2IndependentSequence.get(crossoverPoint).isEmpty() && !parent1IndependentSequence.get(refactoringLength - crossoverPoint).isEmpty())
                return crossoverPoint;
        }
        return -1;
    }

    /**
     * Generate a refactoring by combining the first #crossoverpoint actions of parent1, and the last #(refactoringLength - crossoverPoint) of parent2
     *
     * @param refactoringLength is the length of the sequence of refactoring actions
     * @param crossoverPoint    is a random point where parent1 and parent2 will be split
     * @param modelPath         is the file path of the subject model
     * @param parent1           independent sequence of refactoring actions
     * @param parent2           independent sequence of refactoring actions
     * @return a Refactoring that is the combination of parent1, and parent2 refactoring actions
     */
    private Refactoring createChild(int refactoringLength,
                                    int crossoverPoint,
                                    String modelPath,
                                    Map<Integer, List<List<RefactoringAction>>> parent1,
                                    Map<Integer, List<List<RefactoringAction>>> parent2) {
        Refactoring child = new UMLRefactoring(modelPath);

        // Get the sequences of length specified by the crossoverPoint
        List<List<RefactoringAction>> parent1Sequences = parent1.get(crossoverPoint);
        List<List<RefactoringAction>> parent2Sequences = parent2.get(refactoringLength - crossoverPoint);

        // Generate all the possible combinations of sequences
        List<int[]> candidates = new ArrayList<>();
        for (int i = 0; i < parent1Sequences.size(); i++) {
            for (int j = 0; j < parent2Sequences.size(); j++) {
                candidates.add(new int[]{i, j});
            }
        }

        while (!candidates.isEmpty()) {
            // Randomly select one, and remove it from the list
            int selected = JMetalRandom.getInstance().nextInt(0, candidates.size()-1);
            int[] childSequences = candidates.get(selected);
            candidates.remove(selected);

            // Create the actual child
            child.getActions().addAll(parent1Sequences.get(childSequences[0]));
            child.getActions().addAll(parent2Sequences.get(childSequences[1]));

            // Check if it is feasible
            if (child.isFeasible()) {
                return child;
            }
        }

        return null;
    }

    /**
     * Generates a list of subsets by incrementing the given subset of one element.
     * The new element must occur after the subset in the refactoring sequence.
     *
     * @param root     Starting subset to increment for the generation of new subsets
     * @param position Position at which the root subset was generated
     * @return List of new subsets generated from root.
     */
    List<AbstractMap.SimpleEntry<Integer, List<RefactoringAction>>> generateSubsetsByIncrement(List<RefactoringAction> root, int position, UMLRSolution sol) {

        // refactoring length
        int refactoringLength = Configurator.eINSTANCE.getLength();

        // Entire refactoring sequence
        List<RefactoringAction> sequence = sol.getVariable(0).getActions();

        // New subsets to be generated
        List<AbstractMap.SimpleEntry<Integer, List<RefactoringAction>>> subsets = new ArrayList<>();

        // Check if there are still actions in the sequence we can take starting after the given position
        int start = position + 1;
        if (start > refactoringLength)
            return subsets;

        // Loop over the refactoring sequence after position
        for (int j = start; j < sequence.size(); j++) {
            List<RefactoringAction> newSet = new ArrayList<>();

            // Add all the elements already in the root subset,
            // that is the subset we use as a base for the generation.
            newSet.addAll(root);

            // Add one more element from the refactoring sequence
            newSet.add(sequence.get(j));

            subsets.add(new AbstractMap.SimpleEntry<>(j, newSet));
        }

        return subsets;
    }


    /**
     * Compute the list of all the possible subsets of the refactoring sequence that are independent.
     *
     * @return List of independent subsets.
     */
    Map<Integer, List<List<RefactoringAction>>> independentSequence(UMLRSolution sol) {

        // Entire refactoring sequence
        List<RefactoringAction> sequence = sol.getVariable(0).getActions();

        // Map that will contain all the possible subsequences (subsets) of a given length.
        // The SimpleEntry keeps the position at which the subset was generated and the subset itself.
        Map<Integer, List<AbstractMap.SimpleEntry<Integer, List<RefactoringAction>>>> possibleSubsets = new HashMap<>();

        // Initialize the map by generating the subsequences of length 1
        possibleSubsets.put(1, new ArrayList<>(generateSubsetsByIncrement(new ArrayList<>(), -1, sol)));


        // Loop over the possible subsets length after length 1
        for (int subsetLength = 2; subsetLength < sequence.size(); subsetLength++) {

            // Generate the subsequences of length +1 from the last ones
            possibleSubsets.put(subsetLength, possibleSubsets.get(subsetLength - 1).stream()
                    .flatMap(set -> generateSubsetsByIncrement(set.getValue(), set.getKey(), sol).stream())
                    .collect(Collectors.toList()));
        }

        // The Map of independent subsequences
        return possibleSubsets.keySet().stream()
                .collect(Collectors.<Integer, Integer, List<List<RefactoringAction>>>toMap(
                        k -> k,
                        k -> possibleSubsets.get(k).stream().map(AbstractMap.SimpleEntry::getValue)
                                .filter(sol::isIndependent)
                                .collect(Collectors.toList())
                ));
    }

    /**
     * Writes the crossover report on the 'CrossoverReport.txt' file.
     */
    public void writeCrossoverReport(final String baseDirectory) {
        JMetalLogger.logger.info("Writing the crossover report.");
        final String crossoverReportFile = baseDirectory + "/CrossoverReport.txt";
        final long valid = crossoverCandidates.stream().filter(UMLRSolution::isRefactored).count();
        final String crossoverReport = String.format("Crossover probability: %f. Valid offsprings: %d / %d.",
                crossoverProbability, valid, crossoverCandidates.size());
        try (final FileWriter fw = new FileWriter(crossoverReportFile)) {
            fw.write(crossoverReport);
        } catch (IOException e) {
            JMetalLogger.logger.warning("Unable to write to " + crossoverReportFile);
            e.printStackTrace();
        }
    }
}
