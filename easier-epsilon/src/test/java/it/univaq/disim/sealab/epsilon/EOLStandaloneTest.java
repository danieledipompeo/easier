package it.univaq.disim.sealab.epsilon;

import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.uml2.common.util.CacheAdapter;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

public class EOLStandaloneTest {

	@Before
	public void init() {
	}

	@Test
	public void generateEasierModel() throws EolModelLoadingException, URISyntaxException {

		String modelPath = getClass().getResource("/agv/automatedGuidedVehicle.uml").getFile();

		for (int i = 0; i < 10; i++) {
			EasierUmlModel model = EpsilonStandalone.createUmlModel(modelPath);
			model.disposeModel();
		}
		CacheAdapter.getInstance().clear();

	}

}
