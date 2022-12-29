package it.univaq.disim.sealab.epsilon.eol;

import it.univaq.disim.sealab.epsilon.EpsilonStandalone;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;

import java.nio.file.Path;
import java.util.ArrayList;

public class EOLStandalone extends EpsilonStandalone {

	public EOLStandalone() {
		module = createModule();
		model = new ArrayList<>();
	}

	@Override
	public IEolModule createModule() {
		return new EolModule();
	}

	@Override
	public Path getSource() {
		return source;
	}

	public Object getTarget() {
		return module.getContext().getFrameStack().get("target").getValue();
	}
}
