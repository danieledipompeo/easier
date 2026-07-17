package it.univaq.disim.sealab.metaheuristic.evolutionary.support;

import java.util.List;
import java.util.function.Supplier;

import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.domain.EasierParetoDAO;
import it.univaq.disim.sealab.metaheuristic.evolutionary.ProgressBar;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;

/**
 * Stateless helpers wrapping the EasierResourcesLogger.checkpoint(...) pairs and
 * iteration-progress reporting duplicated across the Custom* algorithm classes'
 * Template Method hook overrides.
 */
public final class CheckpointPhases {

    private CheckpointPhases() {
    }

    /**
     * Wraps a value-returning phase body with a "<phaseName>_start" / "<phaseName>_end"
     * checkpoint pair.
     */
    public static <T> T phase(String algorithmName, String phaseName, Supplier<T> body) {
        EasierResourcesLogger.checkpoint(algorithmName, phaseName + "_start");
        T result = body.get();
        EasierResourcesLogger.checkpoint(algorithmName, phaseName + "_end");
        return result;
    }

    /**
     * Wraps a void phase body with a "<phaseName>_start" / "<phaseName>_end" checkpoint pair.
     */
    public static void phase(String algorithmName, String phaseName, Runnable body) {
        EasierResourcesLogger.checkpoint(algorithmName, phaseName + "_start");
        body.run();
        EasierResourcesLogger.checkpoint(algorithmName, phaseName + "_end");
    }

    /**
     * Records the current front to the experiment's Pareto-front DAO, which also dumps it to JSON.
     * Kept separate from {@link #printProgress} so callers can preserve the original call order
     * relative to the checkpoint pair wrapping updateProgress()'s super call.
     */
    @SuppressWarnings("unchecked")
    public static void recordParetoFront(List<? extends RSolution<?>> currentFront, int currentCount) {
        EasierExperimentDAO.eINSTANCE.addPareto(new EasierParetoDAO((List<RSolution<?>>) currentFront, currentCount));
    }

    /**
     * Prints the algorithm name and refreshes the console progress bar.
     */
    public static void printProgress(String algorithmName, int currentCount, int maxCount) {
        System.out.println(algorithmName);
        ProgressBar.showBar(currentCount, maxCount);
    }
}
