package it.univaq.disim.sealab.metaheuristic.actions;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.actions.uml.RefactoringActionFactory;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLRefactoringAction;
import it.univaq.disim.sealab.metaheuristic.domain.UMLEasierModel;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import it.univaq.disim.sealab.metaheuristic.utils.EasierLogger;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.uma.jmetal.util.JMetalLogger;

import java.net.URISyntaxException;

public class UMLRefactoring extends Refactoring {

    public UMLRefactoring(String mPath) {
        super(mPath);
        easierModel = new UMLEasierModel(mPath);
    }

    public UMLRefactoring(Refactoring rfSource) {
        super(rfSource);
        easierModel = rfSource.easierModel.clone();
    }

    /**
     * Apply the refactoring actions to the model.
     *
     * @return true when the refactoring has been applied successfully, false otherwise.
     */
    public boolean execute() {
        boolean failed = false;
        try {
            EasierResourcesLogger.checkpoint("UMLRefactoring", "execute_start");
            actions.stream().map(UMLRefactoringAction.class::cast).forEach(a -> {
                try (EasierUmlModel model = EOLStandalone.createUmlModel(modelPath)) {
                    model.setStoredOnDisposal(true);
                    a.execute(model);
                } catch (EasierException | URISyntaxException | EolModelLoadingException e) {
                    String msg = String.format("Refactoring of solID: %s throws an exception when executing " +
                            "refactoring actions " +
                            "due to: %s", this.solutionID, e.getMessage());
                    throw new RuntimeException(msg);
                }
            });
        } catch (RuntimeException e) {
            EasierLogger.logger_.severe(e.getMessage());
            failed = true;
        }

        EasierResourcesLogger.checkpoint("UMLRefactoring", "execute_end");
        EasierLogger.logger_.info("Refactoring executed on solID: " + this.solutionID);
        return !failed;
    }

    public boolean tryRandomPush() throws EasierException {

        RefactoringAction candidate;
            candidate = RefactoringActionFactory.getRandomAction(easierModel.getAvailableElements(),
                    easierModel.getInitialElements(), easierModel.getAllContents());

        return addRefactoringAction(candidate);
    }


    public Refactoring clone(){
        UMLRefactoring umlRefactoring = new UMLRefactoring(this);
        return umlRefactoring;
    }


}
