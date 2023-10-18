package it.univaq.disim.sealab.epsilon.utility;

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
			// TODO Use a logger
			System.out.println("Cannot parse: " + filename);
			e.printStackTrace();
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
	public static Double computeSystemEnergy(
			final Map<String, Double> serviceTimes,
			final Map<String, Double> energyCoefficients) {

		if(serviceTimes == null || energyCoefficients == null)
			return Double.MAX_VALUE;

		return serviceTimes.entrySet().stream()
				.filter(e -> energyCoefficients.containsKey(e.getKey()))
				.mapToDouble(e -> e.getValue() * energyCoefficients.get(e.getKey()))
				.sum();
	}

	public static Double computeSystemEnergy(final String uml, final String lqxo) {
		return computeSystemEnergy(extractServiceTimes(lqxo), extractEnergyCoefficients(uml));
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
		final Double systemEnergy = computeSystemEnergy(serviceTimes, energyCoefficients);

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
