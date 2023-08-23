package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;

public class EasierRefactoringActionDAO {

    String name;
    String target;
    String to;
    String where;

    public String getName() {
        return name;
    }

    public String getTarget() {
        return target;
    }

    public String getTo() {
        return to;
    }

    public String getWhere() {
        return where;
    }

    public void setRefactoringAction(RefactoringAction act) throws EasierException {
        String actAsCSV = act.toCSV();
        String[] actFields = actAsCSV.split(",");
        if (actFields.length < 3)
            throw new EasierException("Action to CSV has generated a wrong String");
        // operation,target,to,where
        name = actFields[0];
        target = actFields[1];
        to = actFields[2];
        where = actFields.length == 4 ? actFields[3] : "";
    }

}
