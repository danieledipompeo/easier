package it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.util;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.fileoutput.FileOutputContext;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class RSolutionListOutput {

	private FileOutputContext varFileContext;
	private FileOutputContext funFileContext;
	private String varFileName = "VAR";
	private String funFileName = "FUN";
	private String separator = ",";
	private List<? extends RSolution<?>> solutionList;
	private List<Boolean> isObjectiveToBeMinimized;

	public RSolutionListOutput(List<? extends RSolution<?>> solutionList) {
		varFileContext = new DefaultFileOutputContext(varFileName);
		funFileContext = new DefaultFileOutputContext(funFileName);
		varFileContext.setSeparator(separator);
		funFileContext.setSeparator(separator);
		this.solutionList = solutionList;
		isObjectiveToBeMinimized = null;
	}

	public RSolutionListOutput setVarFileOutputContext(FileOutputContext fileContext) {
		varFileContext = fileContext;

		return this;
	}

	public RSolutionListOutput setFunFileOutputContext(FileOutputContext fileContext) {
		funFileContext = fileContext;

		return this;
	}

	public RSolutionListOutput setObjectiveMinimizingObjectiveList(List<Boolean> isObjectiveToBeMinimized) {
		this.isObjectiveToBeMinimized = isObjectiveToBeMinimized;

		return this;
	}

	public RSolutionListOutput setSeparator(String separator) {
		this.separator = separator;
		varFileContext.setSeparator(this.separator);
		funFileContext.setSeparator(this.separator);

		return this;
	}

	public void print() {

		if (isObjectiveToBeMinimized == null) {
			printObjectivesToFile(funFileContext, solutionList);
		} else {
			printObjectivesToFile(funFileContext, solutionList, isObjectiveToBeMinimized);
		}
		printVariablesToFile(varFileContext, solutionList);
	}

	public void printVariablesToFile(FileOutputContext context, List<? extends Solution<?>> solutionList) {
		BufferedWriter bufferedWriter = context.getFileWriter();

		try {
			bufferedWriter.write(String.format("solID%soperation%starget%sto%swhere", context.getSeparator(),
					context.getSeparator(), context.getSeparator(), context.getSeparator()));

			bufferedWriter.newLine();
			if (solutionList.size() > 0) {
				int numberOfVariables = solutionList.get(0).getNumberOfVariables();
				for (int i = 0; i < solutionList.size(); i++) {
					for (int j = 0; j < numberOfVariables; j++) {
						bufferedWriter.write(solutionList.get(i).getVariable(j).toString());// +
																							// context.getSeparator());
					}
					bufferedWriter.newLine();
				}
			}

			bufferedWriter.close();
		} catch (IOException e) {
			throw new JMetalException("Error writing data ", e);
		}

	}

	public void printObjectivesToFile(FileOutputContext context, List<? extends RSolution<?>> solutionList) {
		BufferedWriter bufferedWriter = context.getFileWriter();

		try {
			// prints the header of the file
			String header;
			// with PAs
			if (Configurator.eINSTANCE.getObjectivesList().size() == 4)
				header = String.format("solID%sperfQ%s#changes%spas%sreliability", separator, separator, separator,
						separator, separator);
			// without pas
			else
				header = String.format("solID%sperfQ%s#changes%sreliability", context.getSeparator(),
						context.getSeparator(), context.getSeparator(), context.getSeparator());
			bufferedWriter.write(header);
			bufferedWriter.newLine();
			if (solutionList.size() > 0) {
				int numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
				for (int i = 0; i < solutionList.size(); i++) {
					bufferedWriter.write(solutionList.get(i).getName() + separator);
					for (int j = 0; j < numberOfObjectives - 1; j++) {
						bufferedWriter.write(solutionList.get(i).getObjective(j) + separator);
					}
					bufferedWriter.write("" + solutionList.get(i).getObjective(numberOfObjectives - 1));
					bufferedWriter.newLine();
				}
			}

			bufferedWriter.close();
		} catch (IOException e) {
			throw new JMetalException("Error printing objecives to file: ", e);
		}
	}

	public void printObjectivesToFile(FileOutputContext context, List<? extends Solution<?>> solutionList,
			List<Boolean> minimizeObjective) {
		BufferedWriter bufferedWriter = context.getFileWriter();

		try {
			if (solutionList.size() > 0) {
				int numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
				if (numberOfObjectives != minimizeObjective.size()) {
					throw new JMetalException(
							"The size of list minimizeObjective is not correct: " + minimizeObjective.size());
				}
				for (int i = 0; i < solutionList.size(); i++) {
					for (int j = 0; j < numberOfObjectives; j++) {
						if (minimizeObjective.get(j)) {
							bufferedWriter.write(solutionList.get(i).getObjective(j) + context.getSeparator());
						} else {
							bufferedWriter.write(-1.0 * solutionList.get(i).getObjective(j) + context.getSeparator());
						}
					}
					bufferedWriter.newLine();
				}
			}

			bufferedWriter.close();
		} catch (IOException e) {
			throw new JMetalException("Error printing objecives to file: ", e);
		}
	}

	/*
	 * Wrappers for printing with default configuration
	 */
	public void printObjectivesToFile(String fileName) throws IOException {
		printObjectivesToFile(new DefaultFileOutputContext(fileName), solutionList);
	}

	public void printObjectivesToFile(String fileName, List<Boolean> minimizeObjective) throws IOException {
		printObjectivesToFile(new DefaultFileOutputContext(fileName), solutionList, minimizeObjective);
	}

	public void printVariablesToFile(String fileName) throws IOException {
		printVariablesToFile(new DefaultFileOutputContext(fileName), solutionList);
	}

}
