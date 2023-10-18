package it.univaq.disim.sealab.metaheuristic.actions;

import it.univaq.disim.sealab.metaheuristic.domain.EasierModel;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface RefactoringAction extends Cloneable {

	RefactoringAction clone();

	String getTargetType();

	Map<String, Set<String>> getTargetElements();

	Map<String, Set<String>> getCreatedElements();

	String toCSV();

	String getName();

	void setIndependent(Map<String, Set<String>> initialElements);

	boolean isIndependent();

	void updateAvailableElements(EasierModel model);

	void restoreAvailableElements(EasierModel model);

	double getRefactoringCost();

	String getTargetElement();

	String getDestination();

	String getDeployment();

	String getTaggedValue();

	String getScalingFactor();

}
