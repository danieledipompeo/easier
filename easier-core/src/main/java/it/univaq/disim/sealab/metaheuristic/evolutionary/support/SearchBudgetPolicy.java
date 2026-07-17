package it.univaq.disim.sealab.metaheuristic.evolutionary.support;

import java.io.Serializable;

import it.univaq.disim.sealab.metaheuristic.utils.Configurator;

/**
 * Encapsulates the time-based search-budget stopping condition shared by the
 * Custom* algorithm classes. One instance is held per algorithm instance.
 *
 * <p>Implements Serializable because every Custom* holder extends a jMetal
 * Algorithm/AbstractEvolutionaryAlgorithm (both Serializable, hence their
 * explicit serialVersionUID fields); the two fields this class replaced
 * (durationThreshold, iterationStartingTime) were plain long/float and always
 * serialized trivially, so this preserves that round-trip behavior exactly.
 */
public class SearchBudgetPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long durationThreshold;
    private long iterationStartingTime;

    public SearchBudgetPolicy() {
        this.durationThreshold = Configurator.eINSTANCE.getStoppingCriterionTimeThreshold();
    }

    /**
     * Marks the start of the search, to be called from initProgress().
     */
    public void start() {
        iterationStartingTime = System.currentTimeMillis();
    }

    public boolean isTimeExceeded() {
        long currentComputingTime = System.currentTimeMillis() - iterationStartingTime;
        return currentComputingTime > durationThreshold;
    }
}
