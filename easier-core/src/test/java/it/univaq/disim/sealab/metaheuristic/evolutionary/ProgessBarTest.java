package it.univaq.disim.sealab.metaheuristic.evolutionary;


import org.junit.jupiter.api.Test;

public class ProgessBarTest {
	
	@Test
	public void showBarTest() throws InterruptedException {
		
		System.out.println("TEST Progress Bar");
		int max = 30;
		for(int i = 1; i < max+1; i+=10) {
			ProgressBar.showBar(i, max);
		}
		
		
	}

}
