package it.univaq.disim.sealab.metaheuristic.domain;

import it.univaq.disim.sealab.metaheuristic.actions.RefactoringAction;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;

public class EasierRefactoringActionDAO {

    String name;
    String target;
    String to;
    String where;
    String tagged_value;
    String factor;

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

    public String getTaggedValue() {
        return tagged_value;
    }

    public String getScalingFactor() {
        return factor;
    }

    public void setRefactoringAction(RefactoringAction act) {
        name = act.getName();

        target = act.getTargetElement();

        to = act.getDestination();

        where = act.getDeployment();

        tagged_value = act.getTaggedValue();

        factor = act.getScalingFactor();
    }

}
