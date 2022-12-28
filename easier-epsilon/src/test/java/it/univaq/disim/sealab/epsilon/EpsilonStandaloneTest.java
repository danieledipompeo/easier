package it.univaq.disim.sealab.epsilon;

import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.epsilon.evl.EVLStandalone;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;

public class EpsilonStandaloneTest {

	String refactoringLibraryModule, uml2lqnModule, GQAM_NAMESPACE;
	String modelPath;
	EVLStandalone evlModule;

	@BeforeEach
	public void setUp() {
		
		refactoringLibraryModule = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
				"easier-refactoringLibrary", "evl", "AP-UML-MARTE.evl").toString();
		uml2lqnModule = Paths.get(FileSystems.getDefault().getPath("").toAbsolutePath().toString(), "..",
				"easier-uml2lqn", "org.univaq.uml2lqn").toString();

		GQAM_NAMESPACE = "MARTE::MARTE_AnalysisModel::GQAM::";
		
		modelPath = getClass().getResource("/simplified-cocome/cocome.uml").getFile();
		
		evlModule = new EVLStandalone();
		EasierUmlModel uml = null;
		try {
			uml = EpsilonStandalone.createUmlModel(modelPath);
		} catch (EolModelLoadingException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		evlModule.setModel(uml);
		evlModule.setSource(Paths.get(refactoringLibraryModule));
		evlModule.setParameter(0.95f, "float", "prob_to_be_pa");

	}


	// requires the updated module

	@Disabled
	@Test
	public void extractFuzzyValuesTest() {
		evlModule.extractFuzzyValues();
	}

}
