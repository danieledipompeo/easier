package it.univaq.disim.sealab.metaheuristic.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class Energy {

	/**
	 * Parse an XML file and return a org.w3c.dom.Document
	 */
	public static Document parseXML(final String filename) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			return builder.parse(new File(filename));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			EasierLogger.logger_.severe("Cannot parse: " + filename + " because " + e.getMessage());
		}
		return null;
	}

	/**
	 * Compute the service time of each task by dividing the utilization by the
	 * throughput. Sum the service times of all the task deployed on a node.
	 * Return a Map with the node name as key and the service time as value.
	 */
	public static Map<String, Double> extractServiceTimes(final String lqxo) {
		// Read the lqxo as an XML file
		final Document doc = parseXML(lqxo);

		if(doc == null)
			return null;

		// Get the processors
		final NodeList processors = doc.getElementsByTagName("processor");

		final Map<String, Double> serviceTimes = new HashMap<>();
		for (int i = 0; i < processors.getLength(); i++) {
			// Get the tasks deployed on the processor
			final Element processor = (Element) processors.item(i);
			final NodeList tasks = processor.getElementsByTagName("result-task");

			// Sum the service times computed for each task
			Double serviceTime = 0.0;
			for (int j = 0; j < tasks.getLength(); j++) {
				final Element task = (Element) tasks.item(j);
				serviceTime += Double.parseDouble(task.getAttribute("utilization")) /
							   Double.parseDouble(task.getAttribute("throughput"));
			}
			serviceTimes.put(processor.getAttribute("name"), serviceTime);
		}

		return serviceTimes;
	}
	
	/**
	 * Read the 'energy' tag of the GRM:ResourceUsage stereotype from the UML file.
	 * Return a Map with the node name as key and the energy coefficient as value.
	 */
	public static Map<String, Double> extractEnergyCoefficients(final String uml) {
		// Read the uml as an XML file
		final Document doc = parseXML(uml);

		// Get the stereotypes applications
		final NodeList resourceUsages = doc.getElementsByTagName("GRM:ResourceUsage");

		final Map<String, Double> energyCoefficients = new HashMap<>();
		for (int i = 0; i < resourceUsages.getLength(); i++) {
			// Get the energy coefficient of the processor
			final Element resourceUsage = (Element) resourceUsages.item(i);
			final Double energy = Double.parseDouble(resourceUsage.getElementsByTagName("energy")
					.item(0).getFirstChild().getNodeValue());

			// Get the ID of the element the stereotype is applied to
			final String resourceUsageTarget = resourceUsage.getAttribute("base_NamedElement");
			
			// Search the model for a UML Node with such ID
			final NodeList packagedElements = doc.getElementsByTagName("packagedElement");
			for (int j = 0; j < packagedElements.getLength(); j++) {
				final Element packagedElement = (Element) packagedElements.item(j);
				if (packagedElement.getAttribute("xmi:type").equals("uml:Node") &&
					packagedElement.getAttribute("xmi:id").equals(resourceUsageTarget)) {
					energyCoefficients.put(packagedElement.getAttribute("name"), energy);
					break;
				}
			}
		}

		return energyCoefficients;
	}

	/**
	 * Compute the energy for the entire system by multiplying the energy coefficients
	 * of each node by the sum of the service times of all the components deployed on
	 * that node, and then summing these values.
	 * 
	 * We assume that the service time is available for all the components (tasks in
	 * the LQN), but the energy coefficient is provided only for a subset of them.
	 * Therefore, we compute energy consumption only for those node that have an
	 * energy coefficient set.
	 */
	static Double systemEnergy(
			final Map<String, Double> serviceTimes,
			final Map<String, Double> energyCoefficients) {

		if(serviceTimes == null || energyCoefficients == null)
			return Double.MAX_VALUE;

		return serviceTimes.entrySet().stream()
				.filter(e -> energyCoefficients.containsKey(e.getKey()))
				.mapToDouble(e -> e.getValue() * energyCoefficients.get(e.getKey()))
				.sum();
	}

	public static double computeSystemEnergy(final String uml, final String lqxo) {
		return systemEnergy(extractServiceTimes(lqxo), extractEnergyCoefficients(uml));
	}

	public static double computeSystemPower(final String uml, final String lqxo, final double k) {
		return systemPower(extractUtilization(lqxo), extractEnergyCoefficients(uml), k);
	}

	/**
	 * Compute the energy for the entire system by multiplying the energy coefficients
	 * of each node by the utilization of the corresponding processor in LQN,
	 * and then summing these values. We assume that when the processor is utilized,
	 * it consumes the maximum energy, while it is otherwise idle.
	 * This model comes from: https://doi.org/10.1007/s10586-023-04030-w.
	 * We consider the value of the energy specified in UML Node as the maximum
	 * energy consumption of the node (p_max).
	 * k is the factor (0, 1) by which the idle energy consumption is scaled from p_max.
	 *
	 * We assume that the utilization is available for all the nodes (processors in
	 * the LQN), but the energy coefficient is provided only for a subset of them.
	 * Therefore, we compute energy consumption only for those node that have an
	 * energy coefficient set.
	 */
	public static Double systemPower(
			final Map<String, Double> utilizations,
			final Map<String, Double> energyCoefficients,
			final double k) {

		if(utilizations == null || energyCoefficients == null)
			return Double.MAX_VALUE;

		return utilizations.entrySet().stream()
				.filter(e -> energyCoefficients.containsKey(e.getKey()))
				.mapToDouble(e -> {
					Double p_max = energyCoefficients.get(e.getKey());
					// k * p_max + (1 - k) * p_max * u
					return k * p_max + (1 - k) * p_max * e.getValue();
				})
				.sum();
	}


	/**
	 * Get the utilization of each processor and return a Map with the node name
	 * as key and the utilization as value.
	 */
	public static Map<String, Double> extractUtilization(final String lqxo) {
		// Read the lqxo as an XML file
		final Document doc = parseXML(lqxo);

		if( doc == null)
			return null;

		// Get the processors
		final NodeList processors = doc.getElementsByTagName("processor");

		final Map<String, Double> utilizations = new HashMap<>();
		for (int i = 0; i < processors.getLength(); i++) {
			// Get the "result-processor" node from each processor.
			// The "utilization" tag of that element will contain
			// the actual utilization of the processor.
			final Element processor = (Element) processors.item(i);
			final Element result = (Element) processor.getElementsByTagName("result-processor").item(0);
			final Double utilization = Double.parseDouble(result.getAttribute("utilization"));
			utilizations.put(processor.getAttribute("name"), utilization);
		}

		return utilizations;
	}

	public static void main(String[] args) {

		// Expect two files in input: a UML model and a lxqo file
		if (args.length != 2) {
			System.out.println("Usage: java -jar energy.jar <uml_model> <lqxo_file>");
			System.exit(1);
		}

		final String uml = args[0];
		final String lqxo = args[1];

		final Map<String, Double> serviceTimes = extractServiceTimes(lqxo);
		final Map<String, Double> energyCoefficients = extractEnergyCoefficients(uml);
		final Double systemEnergy = systemEnergy(serviceTimes, energyCoefficients);

		// print
		serviceTimes.forEach((node, serviceTime) -> {
			System.out.println(node + ": " + serviceTime);
		});
		
		System.out.println("---------------------");

		energyCoefficients.forEach((node, energy) -> {
			System.out.println(node + ": " + energy);
		});

		System.out.println("---------------------");
		
		System.out.println("System energy: " + systemEnergy);
	}

}
