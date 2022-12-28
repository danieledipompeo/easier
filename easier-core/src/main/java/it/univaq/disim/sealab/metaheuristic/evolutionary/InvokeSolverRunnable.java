package it.univaq.disim.sealab.metaheuristic.evolutionary;

public class InvokeSolverRunnable implements Runnable {

	private RSolution solution;

	public InvokeSolverRunnable(RSolution solution) {
		this.solution = solution;
	}

	@Override
	public void run() {
		solution.invokeSolver();
	}
}
