package it.univaq.disim.sealab.metaheuristic.utils;

import javax.xml.transform.Templates;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;

public class EasierResourcesLogger {

    private final Instant startingTime;
    private ArrayList<Duration> duration;
    private Instant timeCheckPoint;

    private ArrayList<long[]> memoryOccupation;
    private long totalBefore, freeBefore;

    private String algorithmName, problemName;

    public EasierResourcesLogger(String aName, String pName) {
        duration = new ArrayList<>();
        memoryOccupation = new ArrayList<>();
        algorithmName = aName;
        problemName = pName;

        startingTime = Instant.now();
        timeCheckPoint = startingTime;

        freeBefore = Runtime.getRuntime().freeMemory();
        totalBefore = Runtime.getRuntime().totalMemory();
    }

    /**
     * Compute the execution time
     */
    private void computeExecutionTime() {
        Instant now = Instant.now();
        duration.add(Duration.between(timeCheckPoint, now));
        timeCheckPoint = now;
    }

    /**
     * Compute the memory usage
     */
    private void computeMemoryOccupation() {
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
     *  Store execution time and the memory usage
     */
    public void checkpoint() {
        computeExecutionTime();
        computeMemoryOccupation();
    }

    /**
     * Dump resources usages and execution times to a CSV file.
     */
    public void toCSV() {
        // duration and memoryOccupation should have the same length by construction
        FileUtils fUtil = new FileUtils();
        for (int i = 0; i < duration.size(); i++) {
            fUtil.algoPerfStatsDumpToCSV(String.format("%s,%s,%s,%s,%s,%s,%s", this.algorithmName,
                    this.problemName, duration.get(i).toMillis(), memoryOccupation.get(i)[1], memoryOccupation.get(i)[0], memoryOccupation.get(i)[3], memoryOccupation.get(i)[2]));
        }
    }


}
