package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ocl.ParserException;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import it.univaq.disim.sealab.metaheuristic.actions.Refactoring;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;

//public class UMLRCrossover<S extends UMLRSolution> extends RCrossover<S> {
public class UMLRCrossover extends RCrossover<UMLRSolution> {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** List of the offspring solutions that are candidates for crossover */
    private static final ArrayList<UMLRSolution> crossoverCandidates = new ArrayList<>();

    /**
     * Constructor
     */
    public UMLRCrossover(double crossoverProbability) {
        super(crossoverProbability);
    }

    /**
     * Perform the crossover operation.
     *
     * The result can be
     * <ul>
     *     <li>parent1, parent2 : when no crossover operation took place</li>
     *     <li>parent1,child2 : when child1 is not applicable</li>
     *     <li>child1,parent2 : when child2 is not applicable</li>
     *     <li>child1,child2 : when the crossover operation took place</li>
     * </ul>
     *
     * @param probability Crossover setProbability
     * @param parent1     The first parent
     * @param parent2     The second parent
     * @return An array containing the two solutions
     */
    public List<UMLRSolution> doCrossover(double probability, UMLRSolution parent1, UMLRSolution parent2) {

        List<UMLRSolution> offspring = new ArrayList<>(2);
        UMLRSolution parent1copy = (UMLRSolution) parent1.copy();
        offspring.add(parent1copy);

        UMLRSolution parent2copy = (UMLRSolution) parent2.copy();
        offspring.add(parent2copy);

        if (JMetalRandom.getInstance().nextDouble() < probability) {
            // 1. Get the length of a solution
            int length = parent1.getVariable(RSolution.VARIABLE_INDEX).getActions().size();

            // 2. Calculate the point to make the crossover
            // it can be between 1 and length -1
            int crossoverPoint = JMetalRandom.getInstance().nextInt(1, length - 1);

            // 5. Apply the crossover to the variable;
//            UMLRSolution offspring1 = new UMLRSolution(parent1, parent2, crossoverPoint, true);
//            offspring1.setParents(parent1, parent2);
//
//            UMLRSolution offspring2 = new UMLRSolution(parent1, parent2, crossoverPoint, false);
//            offspring2.setParents(parent2, parent1);

            offspring = parent1.createChildren(parent2, crossoverPoint);

            // Add the offsprings to the list of candidates
            crossoverCandidates.addAll(offspring);

            // if both children are feasible then it checks
            // whether each one can be applied to the model
//            if (offspring1.isFeasible() && offspring2.isFeasible()) {
//                if (isApplicable(offspring1)) { // if applicable then substitutes the parent1 with the child1
//                    offspring.set(0, offspring1);
//                    offspring.get(0).setCrossovered();
//                    UMLRSolution.XOverCounter++;
//                } else {
//                    offspring1 = null;
//                }
//                if (isApplicable(offspring2)) {// if applicable then substitutes the parent2 with the child2
//                    offspring.set(1, offspring2);
//                    offspring.get(1).setCrossovered();
//                    UMLRSolution.XOverCounter++;
//                } else {
//                    offspring2 = null;
//                }
//            }
        }

//        if (!offspring.get(0).equals(parent1copy)) {
//            offspring.get(0).setCrossovered();
//            UMLRSolution.XOverCounter++;
//        }
//
//        if (!offspring.get(1).equals(parent2copy)) {
//            offspring.get(1).setCrossovered();
//            UMLRSolution.XOverCounter++;
//        }

        // It can be equal to parent1, parent2; parent1,child2; child1,parent2; child1,child2;
        return offspring;
    }


    //TODO set specific condition to verify whether the refactoring sequence is applicable or not after it has been crossoveredn
    private boolean isApplicable(RSolution<?> solution) {
        return true;
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
