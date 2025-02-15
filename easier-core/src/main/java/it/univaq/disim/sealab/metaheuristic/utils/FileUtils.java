package it.univaq.disim.sealab.metaheuristic.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.domain.EasierPopulationDAO;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import org.apache.commons.text.StringSubstitutor;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class FileUtils {

    public FileUtils() {
    }

    /**
     * Recursively walk through sub-directories listing Aemilia files.
     *
     * @param folder starting folder
     * @return array of aemilia file paths
     */
    public static Set<File> listFilesRecursively(final File folder) {
        Set<File> files = new HashSet<File>();
        if (folder == null || folder.listFiles() == null) {
            return files;
        }
        for (File entry : folder.listFiles()) {
            if (entry.isFile() && entry.getName().endsWith(".tsv")) {
                files.add(entry);
            } else if (entry.isDirectory()) {
                files.addAll(listFilesRecursively(entry));
            }
        }
        return files;
    }

    /**
     * Recursively walk through sub-directories listing Aemilia files.
     *
     * @param folder starting folder
     * @return array of aemilia file paths
     */
    public static Set<File> listFilesRecursively(final Path folder, String extension) {
        Set<File> files = new HashSet<File>();
        if (folder == null || folder.toFile().listFiles() == null) {
            return files;
        }
        for (File entry : folder.toFile().listFiles()) {
            if (entry.isFile() && entry.getName().endsWith(extension)) {
                files.add(entry);
            } else if (entry.isDirectory()) {
                files.addAll(listFilesRecursively(entry));
            }
        }
        return files;
    }

    /**
     * Recursively walk through subdirectories listing Aemilia files.
     *
     * @param folder starting folder
     * @return array of aemilia file paths
     */
    @Deprecated
    public static Set<File> listFilesRecursively(final File folder, String extension) {
        Set<File> files = new HashSet<File>();
        if (folder == null || folder.listFiles() == null) {
            return files;
        }
        for (File entry : folder.listFiles()) {
            if (entry.isFile() && entry.getName().endsWith(extension)) {
                files.add(entry);
            } else if (entry.isDirectory()) {
                files.addAll(listFilesRecursively(entry));
            }
        }
        return files;
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

    public static List<String> getParetoSolIDs(final List<Path> paretoReferenceFront) {
        List<String> solIDs = new ArrayList<>();
        for (Path path : paretoReferenceFront) {
            try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    solIDs.add(sCurrentLine.split(" ")[0]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return solIDs;
    }

    public static void fillTemplateKeywords(final Path sourceFile, final Path destination,
                                            final Map<String, String> keywords) {
        try {
            String templateString = fileToString(sourceFile, Charset.defaultCharset());
            StringSubstitutor sub = new StringSubstitutor(keywords);
            String resolvedString = sub.replace(templateString);

            File f = destination.toFile();
            f.getParentFile().mkdirs();
            f.createNewFile();

            PrintWriter out = new PrintWriter(destination.toFile());
            out.print(resolvedString);
            out.close();

        } catch (IOException e) {
            System.err.println("Error in filling the threshold and EVL pas checker file!");
            e.printStackTrace();
        }
    }

    public static String fileToString(Path path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded, encoding);
    }

    @Deprecated
    public static String fileToString(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static List<Path> extractModelPaths(Path csvWithSolutions, int worseSolutions) {
        Path repository = csvWithSolutions.getParent().resolve("tmp");
        BufferedReader csvReader;
        List<Path> modelPaths = new ArrayList<>();
        try {
            csvReader = new BufferedReader(new FileReader(csvWithSolutions.toFile()));
            csvReader.close();

            List<String> lines = Files.readAllLines(csvWithSolutions);

            List<Solution> sols = new ArrayList<>();

            // remove the header
            lines.remove(0);
            for (String line : lines) {
                sols.add(new Solution(line));
            }
            Collections.sort(sols);

            Path defualtRew = Paths.get(
                    "/home/peo/git/sealab/easier/easier-aemilia/src/main/resources/models/FTA/workload_5/model.rew");

            for (int i = 0; i < worseSolutions; i++) {
                int id = sols.get(i).id;

                Path targetFolder = repository.resolve(String.valueOf(id / 100)).resolve(String.valueOf(id));
                modelPaths.add(targetFolder);

                // copy the aem file
                Files.copy(targetFolder.resolve(String.valueOf(id + ".aem")), targetFolder.resolve("model.aem"),
                        StandardCopyOption.REPLACE_EXISTING);
                // copy the rew file
                Files.copy(defualtRew, targetFolder.resolve("model.rew"), StandardCopyOption.REPLACE_EXISTING);
                // copy the val file
                Files.copy(targetFolder.resolve(String.valueOf(id + ".aem.val")), targetFolder.resolve("model.val"),
                        StandardCopyOption.REPLACE_EXISTING);
                // copy the rewmapping file
                Files.copy(targetFolder.resolve(String.valueOf(id + ".rewmapping")),
                        targetFolder.resolve("model.rewmapping"), StandardCopyOption.REPLACE_EXISTING);
                // copy the mmaemilia file
                Files.copy(targetFolder.resolve(String.valueOf(id + ".mmaemilia")),
                        targetFolder.resolve("model.mmaemilia"), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            System.err.println("Error while extracting info from the pareto file");
            e.printStackTrace();
        }

        return modelPaths;
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
     * Prints the line into the refactoring_stats_dump.csv file. The header of the
     * file is "operation,target,to,where,exec_time(nanoSec)"
     *
     * @param line is the CSV of the applied refactoring action
     */
    public void refactoringStatsDumpToCSV(String line) {
        String fileName = "refactoring_stats.csv";
        String header = "operation,target,to,where,exec_time(nanoSec)";

        dumpToFile(fileName, header, line);

    }

    /**
     * Prints the line into the refactoring_stats_dump.csv file. The header of the
     * file is "algorithm,problem,step,exec_time(milliSec)"
     *
     * @param line is the CSV of the applied step
     */
    public void processStepStatsDumpToCSV(String line) {
        String fileName = "process_step_stats.csv";
        String header = "algorithm,problem,solID,step,exec_time(milliSec)";

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

    public void etlErrorLogToCSv(String line) {
        String fileName = "etlErrorLog.csv";
        String header = "solID;message;actions";
        dumpToFile(fileName, header, line);
    }

    public void reliabilityErrorLogToCSV(String line) {
        String fileName = "relErrorLog.csv";
        String header = "solID;message;actions";
        dumpToFile(fileName, header, line);
    }

    public void failedSolutionLogToCSV(String line) {
        String fileName = "reportFailedSolution.csv";
        String header = "solID;lqn_solver_message;actions";
        dumpToFile(fileName, header, line);
    }

    public void xoverStatistics(String line) {
        String fileName = "xover_statistics.csv";
        String header = "total,total_xover,failed_xover";
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

    public void experimentToJSON(EasierExperimentDAO experimentDAO) {
        Path jsonFile = Configurator.eINSTANCE.getOutputFolder().resolve("experiment.json");
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writeValue(jsonFile.toFile(),experimentDAO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void experimentToJSON(EasierExperimentDAO experimentDAO, int runID) {
        Path jsonFile = Configurator.eINSTANCE.getOutputFolder().resolve("experiment" + runID + ".json");
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(),
                    experimentDAO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void populationToJSON(EasierPopulationDAO populationDAO, int prefix) {
        Path jsonFile = Configurator.eINSTANCE.getOutputFolder().resolve("population" + prefix + ".json");
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(),
                    populationDAO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
