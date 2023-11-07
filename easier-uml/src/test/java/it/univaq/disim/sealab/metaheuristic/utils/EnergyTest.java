package it.univaq.disim.sealab.epsilon.utility;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;

public class EnergyTest {
	
	private final String testPath = "src/test/resources";
	private final String umlFile = Paths.get(testPath, "cocome.uml").toString();
	private final String lqxoFile = Paths.get(testPath, "output.lqxo").toString();

	private final Map<String, Double> serviceTimesOracle = Map.ofEntries(
		entry("StockManager", 33.11387358270637),
		entry("EnterpriseClient", 33.67878282884435),
		entry("CashBox", 2.9388254645857406),
		entry("LightDisplay", 0.010028529120502162),
		entry("EnterpriseServer", 1.5608627592215563),
		entry("CashDeskPC", 2.1203476919322224),
		entry("Printer", 0.010024307183191724),
		entry("BarCodeScanner", 45.897858472136065),
		entry("EnterpriseManager", 2.5650233417124095),
		entry("Cashier", 979.1026850259299),
		entry("StoreServer", 31.095036176930964)
	);

	private final Map<String, Double> energyCoefficientsOracle = Map.ofEntries(
		entry("EnterpriseClient", 2.31),
		entry("CashBox", 4.58),
		entry("LightDisplay", 3.67),
		entry("EnterpriseServer", 1.25),
		entry("CashDeskPC", 3.12),
		entry("Printer", 2.98),
		entry("BarCodeScanner", 3.26),
		entry("StoreServer", 4.0)
	);

	private final Map<String, Double> utilizations = Map.ofEntries(
		entry("EnterpriseClient", .31),
		entry("CashBox", .58),
		entry("LightDisplay", .67),
		entry("EnterpriseServer", .25),
		entry("CashDeskPC", .12),
		entry("Printer", .98),
		entry("BarCodeScanner", .26),
		entry("StoreServer", .1)
	);
	
	private final Double systemEnergyOracle = 373.89821267445416;
	private final Double systemPowerOracle = 5.791542;

	@Test
	void extractServiceTimesTest() {
		assertEquals(serviceTimesOracle, Energy.extractServiceTimes(lqxoFile));
	}

	@Test
	void extractEnergyCoefficientsTest() {
		assertEquals(energyCoefficientsOracle, Energy.extractEnergyCoefficients(umlFile));
	}

	@Test
	void computeSystemPowerTest() {
		double k = 0.66;
		assertEquals(systemPowerOracle, Energy.systemPower(energyCoefficientsOracle, utilizations, k));
	}

	@Test
	void computeSystemEnergyTest() {
		assertEquals(systemEnergyOracle, Energy.systemEnergy(serviceTimesOracle, energyCoefficientsOracle));
	}
}
