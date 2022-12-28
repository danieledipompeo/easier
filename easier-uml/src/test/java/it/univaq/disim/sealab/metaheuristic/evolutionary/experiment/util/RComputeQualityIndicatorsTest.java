package it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.util;

import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.factory.FactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.impl.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.PointSolution;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Disabled
public class RComputeQualityIndicatorsTest<S extends Solution> {

	List<GenericIndicator<S>> qIndicators;

	@BeforeEach
	public void setup() {
		List<String> qualityIndicators = Arrays.asList("SPREAD", "IGD+", "IGD", "EPSILON", "HYPER_VOLUME",
				"GENERALIZED_SPREAD");

		qIndicators = new ArrayList<>();
		FactoryBuilder<UMLRSolution> factory = new FactoryBuilder<>();
		for (String qI : qualityIndicators) {
			GenericIndicator<S> ind = (GenericIndicator<S>) factory.createQualityIndicators(qI);
			if (ind != null)
				qIndicators.add(ind);
		}
	}

	@Test
	public void run() throws FileNotFoundException {

		String baseDir = "/mnt/store/research/easier/uml_case_studies/performance_comparison";
		String[] algorithms = { "nsga_", "spea_", "pesa_" };
		int[] iterations = { 72, 82, 102 };
		String referenceFrontName = "/mnt/store/research/easier/uml_case_studies/performance_comparison/super-reference-pareto.csv";
		for (GenericIndicator<S> indicator : qIndicators) {
			for (int a = 0; a < algorithms.length; a++) {
				for (int i = 0; i < iterations.length; i++) {
					Front referenceFront = new ArrayFront(removeSolID(referenceFrontName), ",");
					FrontNormalizer frontNormalizer = new FrontNormalizer(referenceFront);
					Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);
					
					String qualityIndicatorFile = String.format("%s/new_%s%d__%s.csv", baseDir, algorithms[a],
							iterations[i], indicator.getName());
					indicator.setReferenceParetoFront(normalizedReferenceFront);
					
					double[] indicatorValues = new double[3];
					final String algo = algorithms[a];
					final int iteration = iterations[i];
					IntStream.range(0, 3).forEach(run -> {
						String frontFileName = String.format("%s/%s%d__FUN%d.csv", baseDir, algo,
								iteration,run);
						Front front = null;
						try {
							front = new ArrayFront(removeSolID(frontFileName), ",");
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						Front normalizedFront = frontNormalizer.normalize(front);
						List<PointSolution> normalizedPopulation = FrontUtils
								.convertFrontToSolutionList(normalizedFront);
						Double indicatorValue = indicator.evaluate((List<S>) normalizedPopulation);
						indicatorValues[run] = indicatorValue;
					});
					
					for (double indicatorValue : indicatorValues) {
						writeQualityIndicatorValueToFile(indicatorValue, qualityIndicatorFile);
					}
				}
			}

		}
	}
	
	public String removeSolID(String frontFileName) {

		File tmpFile;
		String tmpFileName = null;
		try {
			tmpFile = File.createTempFile("front", "");
			tmpFile.deleteOnExit();
			tmpFileName = tmpFile.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		String readLine = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(frontFileName));
				BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
			while ((readLine = reader.readLine()) != null) {
				if (!readLine.contains("perfQ")) {
//				} else {
					String line = readLine.split(",", 2)[1];
					writer.write(line);
					writer.newLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tmpFileName;
	}
	
	private void writeQualityIndicatorValueToFile(Double indicatorValue, String qualityIndicatorFile) {
		FileWriter os;
		try {
			os = new FileWriter(qualityIndicatorFile, true);
			os.write("" + indicatorValue + "\n");
			os.close();
		} catch (IOException ex) {
			throw new JMetalException("Error writing indicator file" + ex);
		}
	}

}
