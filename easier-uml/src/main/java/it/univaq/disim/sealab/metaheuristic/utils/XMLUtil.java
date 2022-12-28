package it.univaq.disim.sealab.metaheuristic.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class XMLUtil {

	final static String INITIAL_TAG = "lqn-model";
	final static String INITIAL_TAG_TO_CHANGE = "LqnModelType";

	public static void conformanceChecking(Path file) {

		List<String> result = null;
		try {
			result = Files.readAllLines(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (result != null) {
			result.set(1, result.get(1).replace(INITIAL_TAG_TO_CHANGE, INITIAL_TAG));
			result.set(result.size() - 1, result.get(result.size() - 1).replace(INITIAL_TAG_TO_CHANGE, INITIAL_TAG));

			try (FileWriter writer = new FileWriter(file.toFile())) {
				result.forEach(st -> {
					try {
						writer.write(st);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
