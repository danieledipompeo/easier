package it.univaq.disim.sealab.epsilon;

import it.univaq.disim.sealab.epsilon.egl.EGLStandalone;

import java.nio.file.Path;
import java.nio.file.Paths;

public class EpsilonHelper {

	/**
	 * 
	 * @param mmaemiliaFilePath
	 * @param destFilePath
	 * @param ruleFilePath
	 */
	public static synchronized void generateAemFile(Path mmaemiliaFilePath, Path destFilePath) {

		try {
			EGLStandalone eglModule = new EGLStandalone();
			eglModule.setMetamodelPath(Paths.get("/tmp/mmAemlia.ecore"));
			eglModule.setModel(mmaemiliaFilePath);
			eglModule.execute(destFilePath);
			

		} catch (Exception e) {
			System.err.println("mmaemiliaFilePath  --> " + mmaemiliaFilePath);
			System.err.println("destFilePath  --> " + destFilePath);
			System.err.println("GetResource getFile NULL");
			e.printStackTrace();
		}

	}

}
