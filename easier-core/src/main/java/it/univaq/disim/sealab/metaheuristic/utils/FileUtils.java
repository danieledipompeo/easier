package it.univaq.disim.sealab.metaheuristic.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.domain.EasierPopulationDAO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    public FileUtils() {
        if (!Files.exists(Configurator.eINSTANCE.getOutputFolder())) {
            try {
                Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
            } catch (IOException e) {
                EasierLogger.logger_.severe("[ERROR] Cannot create output folder \n:" + e.getMessage());
            }
        }
    }

    public static synchronized void moveTmpFile(final Path sourceFolder, final Path destFolder) {
        destFolder.toFile().mkdirs();
        try {
            org.apache.commons.io.FileUtils.copyDirectory(sourceFolder.toFile(), destFolder.toFile());
        } catch (IOException e) {
            EasierLogger.logger_.warning("[WARNING] Copying tmp folder failed!!!");
            e.printStackTrace();
            return;
        }
        try {
            org.apache.commons.io.FileUtils.cleanDirectory(sourceFolder.toFile());
        } catch (IOException e) {
            EasierLogger.logger_.warning("[WARNING] Cleaning tmp folder failed!!!");
            e.printStackTrace();
        }

    }

    private static void toJson(Object obj, Path jsonFile) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(),
                    obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prints the line into the solution_dump.csv file. The header of the file is
     * "algorithm,problem_tag,solID,perfQ,#changes,pas,reliability"
     *
     * @param line is the CVS conversion of a RSolution
     */
    public void solutionDumpToCSV(String line) {
        String fileName = "solution_dump.csv";
        //String header = "algorithm,problem_tag,solID,perfQ,#changes,pas,reliability";
        String header = "algorithm,problem_tag,solID,perfQ,#changes,energy,reliability";
        dumpToFile(fileName, header, line);
    }

    /**
     * Prints the line into the solution_dump.csv file. The header of the file is
     * "algorithm,problem_tag,search_busget,iteration,max_iteration"
     *
     * @param line is the CSV representation of the search budget data
     */
    public void searchBudgetDumpToCSV(String line) {
        String fileName = "search_budget_stats.csv";
        String header = "algorithm,problem_tag,search_budget,iteration,max_iteration";

        dumpToFile(fileName, header, line);
    }

    /**
     * Prints the line into the solution_dump.csv file. The header of the file is
     * "algorithm,problem_tag,execution_time(ms),total_memory_before(B),free_memory_before(B),total_memory_after(B),free_memory_after(B)"
     *
     * @param line is the CSV representation of the performance data of a run of an
     *             algorithm
     */
    public void algoPerfStatsDumpToCSV(String line) {
        String fileName = "algo_perf_stats.csv";
        String header =
                "iteration_id,label,step,execution_time(ms),total_memory_before(B),free_memory_before(B),total_memory_after(B),free_memory_after(B)";

        dumpToFile(fileName, header, line);
    }

    /**
     * Prints the line into the refactoring_dump.csv file. The header of the file is
     * "solID,operation,target,to,where"
     *
     * @param line is the CSV representation of a refactoring composition
     */
    public void refactoringDumpToCSV(String line) {
        String fileName = "refactoring_composition.csv";
        String header = "solID,operation,target,to,where,tagged_value,factor";

        dumpToFile(fileName, header, line);
    }

    /**
     * Prints the line into the performance_antipatter_dump.csv file. The header of
     * the file is
     * "algorithm,problem_tag,performance_antipattern,target_element,fuzziness"
     *
     * @param line is the CSV representation of the performance antipatern data
     */
    public void performanceAntipatternDumpToCSV(String line) {
        String fileName = "performance_antipatter_dump.csv";
        String header = "solID,problem_tag,performance_antipattern,target_element,fuzziness";

        dumpToFile(fileName, header, line);

    }

    /**
     * Prints the line into the back_annotation_error_log.csv file.
     * The header of the file is "solID,message,actions"
     * The line is a comma separated string, with the last
     * field as a semicolon separated string
     *
     * @param line
     */
    public void backAnnotationErrorLogToCSV(String line) {
        String fileName = "back_annotation_error_log.csv";
        String header = "solID,message,actions";
        dumpToFile(fileName, header, line);

    }

    public void failedSolutionLogToCSV(String line) {
        String fileName = "reportFailedSolution.csv";
        String header = "solID;lqn_solver_message;actions";
        dumpToFile(fileName, header, line);
    }

    /**
     * If fileName does not exist, it will dump the header. Write line into fileName
     *
     * @param fileName
     * @param header
     * @param line
     */
    private void dumpToFile(String fileName, String header, String line) {
        if (!Files.exists(Configurator.eINSTANCE.getOutputFolder().resolve(fileName))) {
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(Configurator.eINSTANCE.getOutputFolder().resolve(fileName).toString()))) {
                writer.write(header);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(Configurator.eINSTANCE.getOutputFolder().resolve(fileName).toString(), true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void performanceMetricsToJSON(Object performanceMetrics) {
        Path jsonFile = Configurator.eINSTANCE.getOutputFolder().resolve("algo_perf_stats.json");
        toJson(performanceMetrics, jsonFile);
        EasierLogger.logger_.info("Performance Metrics data written to: " + jsonFile);
    }

    public void experimentToJSON(EasierExperimentDAO experimentDAO) {
        Path jsonFile = Configurator.eINSTANCE.getOutputFolder().resolve("experiment.json");
        toJson(experimentDAO, jsonFile);
        EasierLogger.logger_.info("Experiment data written to: " + jsonFile);
    }

    public void populationToJSON(EasierPopulationDAO populationDAO, int suffix) {
        Path jsonFile = Configurator.eINSTANCE.getOutputFolder().resolve("population__" + suffix + ".json");
        toJson(populationDAO, jsonFile);
        EasierLogger.logger_.info("Population data written to: " + jsonFile);
    }

    // aimed at sorting solutions within csv file
    // at 0 --> solution id
    // at 1 --> perfQ
    private static class Solution implements Comparable<Solution> {

        int id;
        double perfQ;

        Solution(String line) {

            id = Integer.valueOf(line.split(";")[0]);
            perfQ = Double.valueOf(line.split(";")[1]);
        }

        @Override
        public int compareTo(Solution s) {
            return (s.perfQ < this.perfQ) ? 1 : -1;

        }

    }

}
