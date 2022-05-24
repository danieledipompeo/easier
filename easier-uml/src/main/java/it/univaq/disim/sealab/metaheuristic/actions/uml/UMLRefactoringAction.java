package it.univaq.disim.sealab.metaheuristic.actions.uml;

import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;

public interface UMLRefactoringAction extends RefactoringAction {

    void execute(EasierUmlModel model) throws EasierException;

}
