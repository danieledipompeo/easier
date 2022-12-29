package it.univaq.disim.sealab.epsilon.evl;

import it.univaq.disim.sealab.epsilon.EpsilonStandalone;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.evl.EvlModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EVLStandalone extends EpsilonStandalone {

	/**
	 * It retrieves the evl file from the resources and then copies it to the tmp
	 * folder
	 */
	public EVLStandalone() {
		module = new EvlModule();
		model = new ArrayList<>();
	}

	@Override
	public IEolModule createModule() {
		return new EvlModule();
	}


	/**
	 * Extracts a map<performance antipattern type, map<target.name, fuzzy_value>>
	 * 	  
	 * @return map<performance antipattern type, map<target.name, fuzzy_value>>
	 */
	public Map<String, Map<String, Double>> extractFuzzyValues() {
		try {
			execute();
		} catch (Exception e) {
			System.err.println("Error in Performance antipattern detection using the file " + model.toString());
			e.printStackTrace();
		}

		// mapOfPas is a Map<String, EolMap<String, EolMap<String,Double>>>
		org.eclipse.epsilon.eol.types.EolMap<?, ?> mapOfPas = ((org.eclipse.epsilon.eol.types.EolMap<?, ?>) ((EvlModule) this.module)
				.getContext().getFrameStack().get("fuzzy_values").getValue());

		Map<String, Map<String, Double>> perfAntipaternsClassification = new HashMap<>();
		for (Object key : mapOfPas.keySet()) {
			Map<String, Double> perfAntipatternMap = new HashMap<>();
			org.eclipse.epsilon.eol.types.EolMap<?, ?> antipatternMap = ((org.eclipse.epsilon.eol.types.EolMap<?, ?>) mapOfPas.get(key));

			for(Object modelElement : antipatternMap.keySet()) {
				double fuzzyValue = (double) antipatternMap.get(modelElement);
				perfAntipatternMap.put((String) modelElement, fuzzyValue < 1 ? fuzzyValue : 1);
			}

			perfAntipaternsClassification.put((String)key, perfAntipatternMap);
		}
		return perfAntipaternsClassification;
	}

}
