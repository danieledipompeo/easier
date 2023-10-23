package it.univaq.disim.sealab.metaheuristic.utils;


public class NodeType {

    NodeType(){}

    NodeType(String lbl, double perf, double enrg, double cst) {
        label = lbl;
        performance = perf;
        energy = enrg;
        cost = cst;
    }

    String label;
    double performance;
    double energy;
    double cost;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getPerformance() {
        return performance;
    }

    public void setPerformance(double performance) {
        this.performance = performance;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    @Override
    public String toString(){
        return String.format("label: %s, performance: %f, energy: %f, cost: %f", label, performance, energy, cost);
    }
}
