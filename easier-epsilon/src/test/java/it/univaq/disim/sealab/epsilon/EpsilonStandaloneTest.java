package it.univaq.disim.sealab.epsilon;

import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.net.URISyntaxException;

public class EOLStandaloneTest {

	@BeforeEach
	public void init() {
	}

	@Test
	public void generateEasierModel() throws EolModelLoadingException, URISyntaxException {

		String modelPath = getClass().getResource("/agv/automatedGuidedVehicle.uml").getFile();

		EasierUmlModel model = EpsilonStandalone.createUmlModel(modelPath);

		Assertions.assertNotNull(model);

		ResourceSet rs = model.getResource().getResourceSet();
		Assertions.assertEquals(14, rs.getPackageRegistry().size());

		Assertions.assertTrue(rs.getPackageRegistry().containsKey("http://www.eclipse.org/papyrus/GQAM/1"), "The GQAM" +
				" package must be loaded.");
		
		Assertions.assertTrue(rs.getPackageRegistry().containsKey("http://com.masdes.dam/profiles/DAM/1.0"), "The DAM" +
				" package must be loaded.");

	}

}
