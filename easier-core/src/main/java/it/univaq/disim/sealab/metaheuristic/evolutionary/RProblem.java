package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.uma.jmetal.problem.AbstractGenericProblem;

import java.nio.file.Path;

public abstract class RProblem<S> extends AbstractGenericProblem<S> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int refactoringLength;
	protected int allowedFailures;


	protected Path sourceFolderPath;
	protected Path sourceModelPath;


	public RProblem(Path srcModelPath) {

		this.setNumberOfObjectives(Configurator.eINSTANCE.getObjectives());

		this.setNumberOfConstraints(0);
		this.setNumberOfVariables(1);

		this.sourceFolderPath = srcModelPath.getParent();
		this.sourceModelPath = srcModelPath;

		this.refactoringLength = Configurator.eINSTANCE.getLength();
		this.allowedFailures = Configurator.eINSTANCE.getAllowedFailures();

	}

	@Override
	public String toString() {
		return this.getName();
	}
}
