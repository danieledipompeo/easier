package it.univaq.disim.sealab.metaheuristic.evolutionary.support;

import java.util.Collection;
import java.util.List;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;

/**
 * Stateless helpers for the population-reporting boilerplate (populationToCSV(), clear(), and the
 * run()-tail search-budget dump) duplicated across the Custom* algorithm classes. Every method takes
 * the solution collection as an explicit parameter rather than assuming getPopulation(), since some
 * algorithms (e.g. PESA2) keep their real solutions elsewhere (its archive, via getResult()).
 */
public final class PopulationCsvSupport {

    private PopulationCsvSupport() {
    }

    /**
     * Prints each solution to solution_dump.csv:
     * "algorithm,problem_tag,solID,perfQ,#changes,pas,reliability"
     *
     * @param dumpRefactoring whether to also call each solution's refactoringToCSV() first (NSGAII,
     *                        NSGAIII and IBEA do this in populationToCSV(); SPEA2 and PESA2 do not,
     *                        so callers must state their own behavior explicitly rather than have it
     *                        silently unified here).
     */
    public static void dumpPopulation(String algorithmName, String problemName,
                                       Collection<? extends RSolution<?>> solutions, boolean dumpRefactoring) {
        solutions.forEach(s -> {
            if (dumpRefactoring) {
                s.refactoringToCSV();
            }
            String line = algorithmName + ',' + problemName + ',' + s.objectiveToCSV();
            new FileUtils().solutionDumpToCSV(line);
        });
    }

    /**
     * Detaches parent references and empties the given population/archive list.
     */
    public static void clear(List<? extends RSolution<?>> population) {
        for (RSolution<?> sol : population) {
            sol.setParents(null, null);
        }
        population.clear();
    }

    /**
     * Prints the run()-tail search-budget line to search_budget_stats.csv:
     * "algorithm,problem_tag,search_budget,iteration,max_iteration"
     */
    public static void dumpSearchBudget(String algorithmName, String problemName, String budgetType,
                                        int currentIteration, int maxIteration) {
        new FileUtils().searchBudgetDumpToCSV(String.format("%s,%s,%s,%s,%s", algorithmName, problemName,
                budgetType, currentIteration, maxIteration));
    }
}
