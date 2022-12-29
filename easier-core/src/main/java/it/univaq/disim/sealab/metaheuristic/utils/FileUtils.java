package it.univaq.disim.sealab.metaheuristic.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.domain.EasierPopulationDAO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class FileUtils {

    private FileUtils() {
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

    /**
     * Prints the line into the solution_dump.csv file. The header of the file is
     * "algorithm,problem_tag,solID,perfQ,#changes,pas,reliability"
     *
     * @param line is the CVS conversion of a RSolution
     */
    public static void solutionDumpToCSV(String line) {
        String fileName = "solution_dump.csv";
        String header = "algorithm,problem_tag,solID,perfQ,#changes,pas,reliability";
        dumpToFile(fileName, header, line);
    }

    /**
     * Prints the line into the solution_dump.csv file. The header of the file is
     * "algorithm,problem_tag,search_busget,iteration,max_iteration"
     *
     * @param line is the CSV representation of the search budget data
     */
    public static void searchBudgetDumpToCSV(String line) {
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
    public static void algoPerfStatsDumpToCSV(String line) {
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
    public static void refactoringDumpToCSV(String line) {
        String fileName = "refactoring_composition.csv";
        String header = "solID,operation,target,to,where";

        dumpToFile(fileName, header, line);
    }

    /**
     * Prints the line into the performance_antipatter_dump.csv file. The header of
     * the file is
     * "algorithm,problem_tag,performance_antipattern,target_element,fuzziness"
     *
     * @param line is the CSV representation of the performance antipatern data
     */
    public static void performanceAntipatternDumpToCSV(String line) {
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
    public static void backAnnotationErrorLogToCSV(String line) {
        String fileName = "back_annotation_error_log.csv";
        String header = "solID,message,actions";
        dumpToFile(fileName, header, line);

    }

    public static void etlErrorLogToCSv(String line) {
        String fileName = "etlErrorLog.csv";
        String header = "solID;message;actions";
        dumpToFile(fileName, header, line);
    }

    public static void reliabilityErrorLogToCSV(String line) {
        String fileName = "relErrorLog.csv";
        String header = "solID;message;actions";
        dumpToFile(fileName, header, line);
    }

    public static void failedSolutionLogToCSV(String line) {
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
    private static void dumpToFile(String fileName, String header, String line) {
        if (!Files.exists(Configurator.eINSTANCE.getOutputFolder()))
            try {
                Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
            } catch (IOException e) {
                EasierLogger.logger_.severe("Unable to create the output folder");
                throw new RuntimeException(e);
            }

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

    public static void experimentToJSON(EasierExperimentDAO experimentDAO) {
        Path jsonFile = Configurator.eINSTANCE.getOutputFolder().resolve("experiment.json");
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(),
                    experimentDAO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void experimentToJSON(EasierExperimentDAO experimentDAO, int runID) {
        Path jsonFile = Configurator.eINSTANCE.getOutputFolder().resolve("experiment" + runID + ".json");
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(),
                    experimentDAO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void populationToJSON(EasierPopulationDAO populationDAO, int prefix) {
        Path jsonFile = Configurator.eINSTANCE.getOutputFolder().resolve("population" + prefix + ".json");
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(),
                    populationDAO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Remove all files from the output folder, then remove the output folder itself
     */
    public static void removeOutputFolder(){
        if (Files.exists(Configurator.eINSTANCE.getOutputFolder())) {

            try (Stream<Path> paths = Files.walk(Configurator.eINSTANCE.getOutputFolder())) {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                EasierLogger.logger_.severe("Unable to clean the output folder. \r" + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

}
