package it.univaq.disim.sealab.epsilon.etl;

import it.univaq.disim.sealab.epsilon.EpsilonStandalone;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.etl.EtlModule;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ETLStandalone extends EpsilonStandalone {

	public ETLStandalone() {
		module = new EtlModule();
		model = new ArrayList<>();
	}

	@Override
	public IEolModule createModule() {
		return new EtlModule();
	}

	@Override
	public void clearMemory() {
		super.clearMemory();
		((EtlModule)this.module).getTransformationRules().clear();
	}


}
