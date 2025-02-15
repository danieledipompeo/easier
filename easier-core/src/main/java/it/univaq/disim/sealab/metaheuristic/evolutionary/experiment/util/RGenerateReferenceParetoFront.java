//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
// package org.uma.jmetal.util.experiment.component;

package it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.util;

import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.component.ExperimentComponent;
import org.uma.jmetal.lab.experiment.component.impl.ExecuteAlgorithms;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import org.uma.jmetal.util.point.PointSolution;
import org.uma.jmetal.util.solutionattribute.impl.GenericSolutionAttribute;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This class computes a reference Pareto front from a set of files. Once the
 * algorithms of an experiment have been executed through running an instance of
 * class {@link ExecuteAlgorithms}, all the obtained fronts of all the
 * algorithms are gathered per problem; then, the dominated solutions are
 * removed and the final result is a file per problem containing the reference
 * Pareto front.
 *
 * By default, the files are stored in a directory called "referenceFront",
 * which is located in the experiment base directory. Each front is named
 * following the scheme "problemName.rf".
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class RGenerateReferenceParetoFront implements ExperimentComponent {
	private final Experiment<?, ?> experiment;

	public RGenerateReferenceParetoFront() {
		experiment = null;
	}

	public RGenerateReferenceParetoFront(Experiment<?, ?> experimentConfiguration) {
		this.experiment = experimentConfiguration;
		experiment.removeDuplicatedAlgorithms();
	}

	/**
	 * The run() method creates de output directory and compute the fronts
	 */
	@Override
	public void run() throws IOException {
		String outputDirectoryName = experiment.getReferenceFrontDirectory();

		createOutputDirectory(outputDirectoryName);

		List<String> referenceFrontFileNames = new LinkedList<>();
		for (ExperimentProblem<?> problem : experiment.getProblemList()) {
			NonDominatedSolutionListArchive<RPointSolution> nonDominatedSolutionArchive = new NonDominatedSolutionListArchive<>();

			for (ExperimentAlgorithm<?, ?> algorithm : experiment.getAlgorithmList()) {
				String problemDirectory = experiment.getExperimentBaseDirectory() + "/data/"
						+ algorithm.getAlgorithmTag() + "/" + problem.getTag();

				for (int i = 0; i < experiment.getIndependentRuns(); i++) {
					String frontFileName = problemDirectory + "/" + experiment.getOutputParetoFrontFileName() + i;
					frontFileName += "__" + problem.getTag() + ".csv";

					List<RPointSolution> solutionList = generateRPointSolutionList(frontFileName);

					GenericSolutionAttribute<RPointSolution, String> solutionAttribute = new GenericSolutionAttribute<>();

					for (RPointSolution solution : solutionList) {
						solutionAttribute.setAttribute(solution, algorithm.getAlgorithmTag());
						nonDominatedSolutionArchive.add(solution);
					}
				}
			}
			String referenceSetFileName = outputDirectoryName + "/" + problem.getTag() + ".csv";
			referenceFrontFileNames.add(problem.getTag() + ".csv");

			printReferenceFrontToFile(getFileWriter(referenceSetFileName),
					nonDominatedSolutionArchive.getSolutionList());

			writeFilesWithTheSolutionsContributedByEachAlgorithm(outputDirectoryName, problem,
					nonDominatedSolutionArchive.getSolutionList());
		}

	}

	public List<RPointSolution> generateRPointSolutionList(String varFileName) {

		List<RPointSolution> ptList = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(varFileName))) {

			String sCurrentLine;
			final int numObjs = Configurator.eINSTANCE.getObjectivesList().size();
			while ((sCurrentLine = br.readLine()) != null) {

				if (!sCurrentLine.contains("solID")) {
					String[] split = sCurrentLine.split(",");

					ptList.add(new RPointSolution(numObjs).setID(Integer.parseInt(split[0]))
							.setPointSolution(Arrays.asList((Arrays.copyOfRange(split, 1, numObjs+1)))));
				}
			}
		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
		}
		return ptList;
	}

	private File createOutputDirectory(String outputDirectoryName) {
		File outputDirectory;
		outputDirectory = new File(outputDirectoryName);
		if (!outputDirectory.exists()) {
			boolean result = new File(outputDirectoryName).mkdir();
			JMetalLogger.logger.info("Creating " + outputDirectoryName + ". Status = " + result);
		}

		return outputDirectory;
	}

	private void writeFilesWithTheSolutionsContributedByEachAlgorithm(String outputDirectoryName,
			ExperimentProblem<?> problem, List<RPointSolution> nonDominatedSolutions) {
		GenericSolutionAttribute<PointSolution, String> solutionAttribute = new GenericSolutionAttribute<>();

		for (ExperimentAlgorithm<?, ?> algorithm : experiment.getAlgorithmList()) {
			List<RPointSolution> solutionsPerAlgorithm = new ArrayList<>();
			for (RPointSolution solution : nonDominatedSolutions) {
				if (algorithm.getAlgorithmTag().equals(solutionAttribute.getAttribute(solution))) {
					solutionsPerAlgorithm.add(solution);
				}
			}

			printReferenceFrontToFile(getFileWriter(
					outputDirectoryName + "/" + problem.getTag() + "." + algorithm.getAlgorithmTag() + ".csv"), solutionsPerAlgorithm);
		}
	}

	private BufferedWriter getFileWriter(String fileName) {
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			throw new JMetalException("Exception when calling method getFileWriter()", e);
		}
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

		return new BufferedWriter(outputStreamWriter);
	}

	public void printReferenceFrontToFile(BufferedWriter bufferedWriter, List<RPointSolution> solutionList) {

		String separator = ",";

		try {
			// prints the header of the file
			String header;
			if (Configurator.eINSTANCE.getObjectivesList().size() == 4)
				header = String.format("solID%sperfQ%s#changes%spas%sreliability", separator, separator, separator,
						separator, separator);
			else
				header = String.format("solID%sperfQ%s#changes%sreliability", separator, separator, separator,
						separator);
			bufferedWriter.write(header);
			bufferedWriter.newLine();
			if (solutionList.size() > 0) {
				int numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
				for (int i = 0; i < solutionList.size(); i++) {
					bufferedWriter.write(solutionList.get(i).getID() + separator);
					for (int j = 0; j < numberOfObjectives-1; j++) {
						bufferedWriter.write(solutionList.get(i).getObjective(j) + separator);
					}
					bufferedWriter.write(""+solutionList.get(i).getObjective(numberOfObjectives - 1));
					bufferedWriter.newLine();
				}
			}

			bufferedWriter.close();
		} catch (IOException e) {
			throw new JMetalException("Error printing objecives to file: ", e);
		}
	}


}
