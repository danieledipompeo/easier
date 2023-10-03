package it.univaq.disim.sealab.metaheuristic.actions;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.actions.uml.RefactoringActionFactory;
import it.univaq.disim.sealab.metaheuristic.actions.uml.UMLRefactoringAction;
import it.univaq.disim.sealab.metaheuristic.domain.UMLEasierModel;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
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


    public boolean execute() {
        try{
            EasierResourcesLogger.checkpoint("UMLRefactoring","execute_start");
            actions.stream().map(UMLRefactoringAction.class::cast).forEach(a -> {
                try (EasierUmlModel model = EOLStandalone.createUmlModel(modelPath)) {
                    model.setStoredOnDisposal(true);
                    a.execute(model);
                } catch (EasierException e) {
                    String msg = String.format("Refactoring of solID %s throw an exception when executing actions", this.solutionID);
                    throw new RuntimeException(msg);
                } catch (URISyntaxException | EolModelLoadingException ex) {
                    throw new RuntimeException(ex);
                }
//                a.updateAvailableElements(easierModel);
//                easierModel.store(a.getCreatedElements());
            });
            EasierResourcesLogger.checkpoint("UMLRefactoring","execute_end");
        } catch (RuntimeException e) {
            JMetalLogger.logger.severe(e.getMessage());
            return false;
        }
        JMetalLogger.logger.info("Refactoring executed");
        return true;
    }

    public boolean tryRandomPush() throws EasierException {

        RefactoringAction candidate;
//        do {
            candidate = RefactoringActionFactory.getRandomAction(easierModel.getAvailableElements(), easierModel.getInitialElements());
//        } while (candidate == null);

        return addRefactoringAction(candidate);
        /*
        getActions().add(candidate);

        if (!this.isFeasible()) {
            getActions().remove(candidate);
            return false;
        }
        return true;*/
    }


    public Refactoring clone(){
        UMLRefactoring umlRefactoring = new UMLRefactoring(this);
        return umlRefactoring;
    }


}
