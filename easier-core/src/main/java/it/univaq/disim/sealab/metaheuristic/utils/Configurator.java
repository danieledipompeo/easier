package it.univaq.disim.sealab.metaheuristic.utils;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.IParameterSplitter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Configurator {

	public static final String PERF_Q_LABEL = "perfq";
	public static final String PAS_LABEL = "pas";
	public static final String ENERGY_LABEL = "energy";
	public static final String RELIABILITY_LABEL = "reliability";
	public static final String CHANGES_LABEL = "changes";
	public static final String SYS_RESP_T_LABEL = "sysRespT";
	public static final String POWER_LABEL = "power";
	public static final String ECONOMIC_COST = "economicCost";

	public static final String OPERATION_LABEL = "operation";
	public static final String COMPONENT_LABEL = "component";
	public static final String NODE_LABEL = "node";

	public static Configurator eINSTANCE = new Configurator();

	@Parameter
	private List<String> parameters = new ArrayList<>();

	@Parameter(names = { "-h", "--help" }, help = true)
	private boolean help;

	@Parameter(names = { "-m", "--models" }, required = true, description = "List of models")
	private List<String> modelsPath = new ArrayList<>();

	@Parameter(names = { "--solver" }, description = "Set the solver {TTKernel, LQN} path")
	private String solver = "/usr/local/bin/lqns";

	@Parameter(names = { "-p", "--pareto" }, description = "Give the Reference pareto front file path")
	private String paretoFront;

	@Parameter(names = { "-r", "--independent_runs" }, description = "Set the number of independent runs")
	private int independetRuns = 31;

	@Parameter(names = { "--cleaningTmp" }, description = "Set to true for removing all temporary files")
	private boolean cleaningTmp = false;

	@Parameter(names = { "-outF", "--outputFolder" }, required = true, description = "Set the output root folder")
	private String outputFolder="/tmp/easier-output-test";

	@Parameter(names = { "-maxEval", "--maxEvaluation" }, required = true, description = "Set the maximum evaluations")
	private List<Integer> maxEval = List.of(72);

	@Parameter(names = { "-popSize", "--populationSize" }, required = true, description = "Set the population size")
	private int popSize = 2;

	@Parameter(names = { "-xover", "--xoverProb" }, description = "Set the crossover probability")
	private double xover = 0.8;

	@Parameter(names = { "-mutation", "--mutationProb" }, description = "Set the mutation probability")
	private double mutation = 0.2;

	@Parameter(names = { "-dIndex",
			"--distributionIndex" }, description = "Set the distribution index for the mutation operator")
	private double distributionIndex = 20;

	@Parameter(names = { "-l", "--sequenceLength" }, description = "The length of a sequence")
	private int length = 4;

	@Parameter(names = { "-af", "--allowedFaiulures" }, description = "Set the maximunm number of failures")
	private int aw = 100;

	@Parameter(names = { "-tmpF", "--tempFolder" }, required = true, description = "It is the temporary file folder")
	private String tmpF = "/tmp/easier-test";

	@Parameter(names = { "-algo", "--algorithm" }, required = true, description = "List of algorithms")
	private String algorithm = "nsgaii";

	@Parameter(names = { "-qI", "--quality_indicator" }, required = true, description = "List of quality indicators")
	private List<String> qI = List.of("SPREAD","IGD+","EPSILON","HYPER_VOLUME","GENERALIZED_SPREAD");

	@Parameter(names = { "-rf", "--refereceFront" }, description = "The absolut path to the reference front file (.rf)")
	private List<String> referenceFront;

	@Parameter(names = { "-genRF", "--generate_reference_front" }, description = "It allows the generation of reference front by FUN files")
	private boolean generateRF = false;
	
	/*@Parameter(names = {"--objectives", "--objs"}, description = "Number of objectives" )
	private int objectives = 4;*/
	
	@Parameter(names = {"--ref_points"}, description = "List of reference points for R-NSGA algorithm")
	private List<Double> referencePoints = new ArrayList<>();
	
	@Parameter(names = {"--epsilon"},  description = "The epsilon value for the R-NSGA algorithm")
	private double epsilon = 0.3d;
	
	@Parameter(names = {"-SB" , "--search-budget"}, description = "It enables the search budget. It supports: byTime, byPrematureConvergence, byBoth" )
	private String searchBudget = "none";
	
	@Parameter(names = {"-sbTimeTh","--searchBudgetTimeThreshold"}, description = "The search budget stopping criterion by time.")
	private long searchBudgetTimeThreshold = 3_600_000;
	
	@Parameter(names = {"-sbPCTh","--searchBudgetPrematureConvergenceThreshold"}, description = "The search budget stopping criterion by premature convergence.")
	private float searchBudgetPrematureConvergenceThreshold = 0.50f;
	
	//It is a positional List where: 0=ePas,1=eRel,2=ePerfQ,3=eChanges
	@Parameter(names = {"-sbPCEpsilon", "--searchBudgetPrematureConvergenceEpsilon"}, description = "The epsilon neighborhood for Premature Convergence.")
	private List<Double> optimalPointEpsilon = List.of(1d,1.15d,1.15d,1.3d);

	// For testing purposes it does not contain the tactics
	@Parameter(names = {"-brf","--baselineRefactoringFactor"},  splitter = SemiColonSplitter.class, description = "The ordered list of baseline refactoring factors of Refactoring actions")
	private List<String> brfs_list = List.of("clone:1.23","moc:1.23","mcnn:1.23","moncnn:1.23");

	@Parameter(names = {"-probPAS","--probToBePerfAntipattern"}, description = "The probability to be a performance antipattern")
	private double probPas = 0.95f;

	@Parameter(names= {"-initialModelPath", "--initModelPath"}, description = "The file path of the initial model " +
			"used by the perfQ evaluator.")
	private String initialModelPath = "cocome/simplified-cocome/cocome.uml";


	@Parameter(names= {"--initialChanges", "-iChanges"}, description = "The architectural changes computed in the " +
			"previous iteration step. Default: 0.")
	private double initialChanges = 0d;

	@Parameter(names = {"-objs", "--objectives"}, description = "The objectives")
	private List<String> objectivesList = List.of("sysRespT", "changes", "reliability", "energy");

	@Parameter(names = {"-nodeChar", "--nodeCharacteristics"}, splitter = SemiColonSplitter.class, description = "The" +
			" node characteristics")
	private String nodeTypes = "[{\"label\":\"small\",\"performance\":1.0,\"energy\":1.5,\"cost\":1000.0}, " +
			"{\"label\":\"medium\",\"performance\":2.5,\"energy\":3.5,\"cost\":2500.0}]";

	@Parameter(names = {"--power-ratio", "-pwr"}, description = "k is the ratio of power idle to power max.")
	private double powerRatio = 0.66;

	public long getStoppingCriterionTimeThreshold() {
		return searchBudgetTimeThreshold;
	}
	
	public float getStoppingCriterionPrematureConvergenceThreshold() {
		return searchBudgetPrematureConvergenceThreshold;
	}
	
	public String getSearchBudgetType() {
		return searchBudget;
	}
	
	public boolean isSearchBudgetByTime() {
		return searchBudget.equals("byTime");
	}
	
	public boolean isSearchBudgetByPrematureConvergence() {
		return searchBudget.equals("byPrematureConvergence");
	}
	
	public boolean isSearchBudgetByPrematureConvergenceAndTime() {
		return searchBudget.equals("byBoth");
	}
	
	
	public List<String> getBrfList(){
		return brfs_list;
	}
	
	public double getBRF(String key) {
		for (String s : brfs_list)
			if(s.split(":")[0].contains(key))			
				return Double.parseDouble(s.split(":")[1]);
		return 1.23d;
	}
	
	public String getSearchBudget() {
		return searchBudget;
	}
	
	public String getSearchBudgetThreshold() {
		if("searchBudgetPrematureConvergenceThreshold".equals(searchBudget))
			return String.valueOf(searchBudgetPrematureConvergenceThreshold);
		if ("byBoth".equals(searchBudget))
			return String.valueOf(searchBudgetTimeThreshold) + "-" +  String.valueOf(searchBudgetPrematureConvergenceThreshold);
		return String.valueOf(searchBudgetTimeThreshold);		
	}
	
	
	public double getEpsilon() {
		return epsilon;
	}
	
	public List<Double> getReferencePoints(){
		return referencePoints;
	}
	
	public List<Path> getReferenceFront() {
		List<Path> paths = new ArrayList<>();
		if (referenceFront == null)
			return null;
		for (String s : referenceFront)
			paths.add(Paths.get(s));
		return paths;
	}

	public boolean generateRF() {
		return generateRF;
	}

	public List<String> getQualityIndicators() {
		return qI;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public Path getTmpFolder() {
		return Paths.get(tmpF);
	}

	public int getAllowedFailures() {
		return aw;
	}

	public int getLength() {
		return length;
	}

	public double getXoverProbabiliy() {
		return xover;
	}

	public double getMutationProbability() {
		return mutation;
	}

	public int getPopulationSize() {
		return popSize;
	}

	public List<Integer> getMaxEvaluation() {
		return maxEval;
	}

	public Path getOutputFolder() {
		return Paths.get(outputFolder);
	}

	public int getIndependetRuns() {
		return independetRuns;
	}

	public List<Path> getModelsPath() {
		List<Path> paths = new ArrayList<>();
		
		modelsPath.forEach(m -> paths.add(Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..", "easier-uml2lqnCaseStudy", m)));
		
//		modelsPath.forEach(m -> paths.add(Paths.get(m)));
		return paths;
	}

	public Path getSolver() {
		return Paths.get(solver);
	}

	public double getDistributionIndex() {
		return 0;
	}

	public List<String> listOfActions() {
		List<String> listOfActions = new ArrayList<>();
		for (String s : brfs_list){
			listOfActions.add(s.split(":")[0]);
		}
		return listOfActions;
	}

	public double getPowerRatioIdleMax() {
		return powerRatio;
	}

	public static class SemiColonSplitter implements IParameterSplitter {
	    public List<String> split(String value) {
	      return Arrays.asList(value.split(";"));
	    }
	}

	public Double getProbPas() {
		return probPas;
	}

	public double[] getLocalOptimalPointEpsilon() {
		return optimalPointEpsilon.stream().mapToDouble(Number::doubleValue).toArray();
	}

	public Path getInitialModelPath() {
		return Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..", "easier" +
				"-uml2lqnCaseStudy", initialModelPath);
	}

	public double getInitialChanges(){
		return initialChanges;
	}

	public List<String> getObjectivesList(){
		return objectivesList;
	}

	// Extract the node characteristics from the configurator
	public List<NodeType> getNodeCharacteristics() {
		ObjectMapper objectMapper = new ObjectMapper();
		List<NodeType> listNodeTypes = null;
		try {
			listNodeTypes = objectMapper.readValue(nodeTypes, new TypeReference<>() {});
		} catch (JsonProcessingException e) {
			EasierLogger.logger_.severe("Error when parsing the node characteristics: " + e.getMessage());
			EasierLogger.logger_.severe("The default node characteristics will be used.");
			e.printStackTrace();
			listNodeTypes = List.of(new NodeType("small", 1, 1.5, 1000));
		}
		return listNodeTypes;
	}
}


