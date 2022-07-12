package it.univaq.disim.sealab.metaheuristic.utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EasierResourcesLogger {

    private static ArrayList<Long> execTimeMillisi = new ArrayList<>();
    private static ArrayList<long[]> memoryOccupation = new ArrayList<>();
    private static long totalBefore, freeBefore;
    private static List<String> labels = new ArrayList<>();
    private static List<String> steps = new ArrayList<>();
    private static List<Integer> iterationIDs = new ArrayList<>();
    private static int ITERATION_COUNTER = 0;

//    public EasierResourcesLogger(String aName) {
//        freeBefore = Runtime.getRuntime().freeMemory();
//        totalBefore = Runtime.getRuntime().totalMemory();
//    }

    /**
     * Compute the execution time
     */
    private static void computeExecutionTime() {
        execTimeMillisi.add(Instant.now().toEpochMilli());
    }

    /**
     * Compute the memory usage
     */
    private static void computeMemoryOccupation() {
        long[] memory = new long[4];

        // memory state before the step
        memory[0] = freeBefore;
        memory[1] = totalBefore;

        // occupied memory at each step
        memory[2] = Runtime.getRuntime().freeMemory(); // free after
        memory[3] = Runtime.getRuntime().totalMemory(); // total after

        memoryOccupation.add(memory);

        // store the checkpoint
        freeBefore = memory[2];
        totalBefore = memory[3];
    }

    /**
     * Store execution time and the memory usage
     */
    public static void checkpoint(String label, String stp) {
        labels.add(label);
        iterationIDs.add(ITERATION_COUNTER);
        steps.add(stp);
        computeExecutionTime();
        computeMemoryOccupation();
    }

    public static void iterationCheckpointStart(String label, String stp) {
        ITERATION_COUNTER++;
        checkpoint(label, stp);
//        checkpoint("ID_" + ITERATION_COUNTER + "_" + stp);
    }

    /**
     * Dump resource usages and execution times in a CSV file.
     */
    public static void dumpToCSV() {
        // duration and memoryOccupation should have the same length by construction
        FileUtils fUtil = new FileUtils();
        for (int i = 0; i < execTimeMillisi.size(); i++) {
            fUtil.algoPerfStatsDumpToCSV(String.format("%s,%s,%s,%s,%s,%s,%s,%s", iterationIDs.get(i), labels.get(i),
                    steps.get(i), execTimeMillisi.get(i), memoryOccupation.get(i)[1], memoryOccupation.get(i)[0], memoryOccupation.get(i)[3], memoryOccupation.get(i)[2]));
        }
    }


}
