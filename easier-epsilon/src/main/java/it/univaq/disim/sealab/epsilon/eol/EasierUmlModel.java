package it.univaq.disim.sealab.epsilon.eol;

import com.masdes.dam.Core.CorePackage;
import com.masdes.dam.DAM.DAMPackage;
import com.masdes.dam.Maintenance.MaintenancePackage;
import com.masdes.dam.Threats.ThreatsPackage;
import it.univaq.disim.sealab.epsilon.utility.Energy;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.epsilon.emc.uml.UmlModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.papyrus.MARTE.MARTE_AnalysisModel.GQAM.GQAMPackage;
import org.eclipse.papyrus.MARTE.MARTE_DesignModel.HLAM.HLAMPackage;
import org.eclipse.papyrus.MARTE.MARTE_DesignModel.HRM.HRMPackage;
import org.eclipse.papyrus.MARTE.MARTE_DesignModel.HRM.HwPhysical.HwLayout.HwLayoutPackage;
import org.eclipse.papyrus.MARTE.MARTE_DesignModel.HRM.HwPhysical.HwPhysicalPackage;
import org.eclipse.papyrus.MARTE.MARTE_Foundations.Alloc.AllocPackage;
import org.eclipse.papyrus.MARTE.MARTE_Foundations.CoreElements.CoreElementsPackage;
import org.eclipse.papyrus.MARTE.MARTE_Foundations.GRM.GRMPackage;
import org.eclipse.papyrus.MARTE.MARTE_Foundations.NFPs.NFPsPackage;
import org.eclipse.papyrus.MARTE.MARTE_Foundations.Time.TimePackage;
import org.eclipse.uml2.common.util.CacheAdapter;
import org.eclipse.uml2.uml.*;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class EasierUmlModel extends UmlModel {


    // MARTE
    private static final String MARTE_BASE_PATHMAP = "pathmap://Papyrus_PROFILES/";
    private static final String MARTE_PROFILE = "MARTE.profile.uml#";
    private static final String MARTE_NFP_FRAGMENT = "_U_GAoAPMEdyuUt-4qHuVvQ";
    private static final String MARTE_TIME_FRAGMENT = "_WStkoAPMEdyuUt-4qHuVvQ";
    private static final String MARTE_GRM_FRAGMENT = "_XVWGUAPMEdyuUt-4qHuVvQ";
    private static final String MARTE_ALLOC_FRAGMENT = "_ar8OsAPMEdyuUt-4qHuVvQ";
    private static final String MARTE_CORE_ELEMENTS_FRAGMENT = "_-wEewECLEd6UTJZnztgOLw";
    private static final String MARTE_GQAM_FRAGMENT = "_4bV20APMEdyuUt-4qHuVvQ";
    private static final String MARTE_HLAM_FRAGMENT = "_yNSZIAPMEdyuUt-4qHuVvQ";
    private static final String MARTE_HW_LAYOUT_FRAGMENT = "_uAf6gBJwEdygQ5HMNSpiZw";
    private static final String MARTE_HW_PHYCAL_FRAGMENT = "_R7sL8BJwEdygQ5HMNSpiZw";
    // DAM
    private static final String DAM_BASE_PATHMAP = "pathmap://DAM_PROFILES/";
    private static final String DAM_PROFILE = "DAM.profile.uml#";
    private static final String DAM_PROFILE_FRAGMENT = "_dYZGQOI-EeKRk-i8_Z91aQ";
    private static final String DAM_CORE_FRAGMENT = "_DchGAOSiEeKuSu-I2xDxSA";
    private static final String DAM_THREATS_FRAGMENT = "_G1-xoOShEeKuSu-I2xDxSA";
    private static final String DAM_MAINTENANCE_FRAGMENT = "_rsXqkOShEeKuSu-I2xDxSA";

    private ResourceSet resourceSet;


    /**
     * It has been inspired by the solution proposed in this post
     *
     * <p>
     * <a href="https://www.eclipse.org/forums/index.php/m/1701551/?srch=standalone#msg_1701551">proposed solution</a>
     * </p>
     * <p>
     * We are using MARTE and DAM profiles versions:
     * <ul>
     * <li>MARTE :
     * org.eclipse.papyrus.marte.static.profile_1.2.0.201606080903, downloaded
     * <a href="http://download.eclipse.org/modeling/mdt/papyrus/updates/releases/neon"> MARTE </a>
     * </li>
     * <li>DAM :
     * com.masdes.dam.static.profile_0.13.1.201801221725.jar, downloaded
     * <a href="https://github.com/dice-project/DICE-Profiles"> DAM </a>
     * </li>
     * </ul>
     * </p>
     * <p>
     * TAKE If the version of that plugin changes, this link must change as
     * well.
     * </p>
     */
    @Override
    protected ResourceSet createResourceSet() {
        resourceSet = super.createResourceSet();

        resourceSet = UMLResourcesUtil.init(resourceSet);

        // stores UML model and UML profile extensions to the ExtensionToFactoryMap
        // {@see
        // org.eclipse.emf.ecore.resource.Resource.Factory.Registry.getExtensionToFactoryMap()}
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.PROFILE_FILE_EXTENSION,
                UMLResource.Factory.INSTANCE);

        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION,
                UMLResource.Factory.INSTANCE);

        resourceSet = initMARTE(resourceSet);
        resourceSet = initDAM(resourceSet);

        return resourceSet;
    }

    /**
     * @param resourceSet is the resource set to be initialized for using MARTE
     * @return Maps physical resource and the pathmap schema. Stores every needed
     * package used in the model
     */
    private ResourceSet initMARTE(ResourceSet resourceSet) {
        final URI marteURI = URI.createURI(getClass().getResource("/marte").toString());
        resourceSet.getURIConverter().getURIMap().put(URI.createURI(MARTE_BASE_PATHMAP),
                marteURI.appendSegment(""));
        final String MARTE_PROFILES_PATHMAP = MARTE_BASE_PATHMAP + MARTE_PROFILE;

        // NFP
        UMLPlugin.getEPackageNsURIToProfileLocationMap()
                .put(NFPsPackage.eNS_URI, URI.createURI(MARTE_PROFILES_PATHMAP + MARTE_NFP_FRAGMENT));
        resourceSet.getPackageRegistry().put(NFPsPackage.eNS_URI, NFPsPackage.eINSTANCE);

        // TIME
        UMLPlugin.getEPackageNsURIToProfileLocationMap()
                .put(TimePackage.eNS_URI, URI.createURI(MARTE_PROFILES_PATHMAP + MARTE_TIME_FRAGMENT));
        resourceSet.getPackageRegistry().put(TimePackage.eNS_URI, TimePackage.eINSTANCE);

        // GRM
        UMLPlugin.getEPackageNsURIToProfileLocationMap()
                .put(GRMPackage.eNS_URI, URI.createURI(MARTE_PROFILES_PATHMAP + MARTE_GRM_FRAGMENT));
        resourceSet.getPackageRegistry().put(GRMPackage.eNS_URI, GRMPackage.eINSTANCE);

        // Alloc
        UMLPlugin.getEPackageNsURIToProfileLocationMap()
                .put(AllocPackage.eNS_URI, URI.createURI(MARTE_PROFILES_PATHMAP + MARTE_ALLOC_FRAGMENT));
        resourceSet.getPackageRegistry().put(AllocPackage.eNS_URI, AllocPackage.eINSTANCE);

        // Core_Elements
        UMLPlugin.getEPackageNsURIToProfileLocationMap()
                .put(CoreElementsPackage.eNS_URI, URI.createURI(MARTE_PROFILES_PATHMAP + MARTE_CORE_ELEMENTS_FRAGMENT));
        resourceSet.getPackageRegistry().put(CoreElementsPackage.eNS_URI, CoreElementsPackage.eINSTANCE);

        // GQAM
        UMLPlugin.getEPackageNsURIToProfileLocationMap()
                .put(GQAMPackage.eNS_URI, URI.createURI(MARTE_PROFILES_PATHMAP + MARTE_GQAM_FRAGMENT));
        resourceSet.getPackageRegistry().put(GQAMPackage.eNS_URI, GQAMPackage.eINSTANCE);

        //HLAM
        UMLPlugin.getEPackageNsURIToProfileLocationMap()
                .put(HLAMPackage.eNS_URI, URI.createURI(MARTE_PROFILES_PATHMAP + MARTE_HLAM_FRAGMENT));
        resourceSet.getPackageRegistry().put(HLAMPackage.eNS_URI, HLAMPackage.eINSTANCE);

        //HwPhysical
        UMLPlugin.getEPackageNsURIToProfileLocationMap()
                .put(HwPhysicalPackage.eNS_URI, URI.createURI(MARTE_PROFILES_PATHMAP + MARTE_HW_PHYCAL_FRAGMENT));
        resourceSet.getPackageRegistry().put(HwPhysicalPackage.eNS_URI, HwPhysicalPackage.eINSTANCE);


        //HwLayout
        UMLPlugin.getEPackageNsURIToProfileLocationMap()
                .put(HwLayoutPackage.eNS_URI, URI.createURI(MARTE_PROFILES_PATHMAP + MARTE_HW_LAYOUT_FRAGMENT));
        resourceSet.getPackageRegistry().put(HwLayoutPackage.eNS_URI, HwLayoutPackage.eINSTANCE);

        return resourceSet;
    }

    /**
     * @param resourceSet is the resource set to be initialized for using DAM
     * @return Maps physical resource and the pathmap schema. Stores every needed
     * package used in the model
     */
    private ResourceSet initDAM(ResourceSet resourceSet) {
        final URI damURI = URI.createURI(getClass().getResource("/dam").toString());
        resourceSet.getURIConverter().getURIMap().put(URI.createURI(DAM_BASE_PATHMAP),
                damURI.appendSegment(""));
        final String DAM_PROFILES_PATHMAP = DAM_BASE_PATHMAP + DAM_PROFILE;

        // DAM_Profile
        UMLPlugin.getEPackageNsURIToProfileLocationMap()
                .put(DAMPackage.eNS_URI, URI.createURI(DAM_PROFILES_PATHMAP + DAM_PROFILE_FRAGMENT));
        resourceSet.getPackageRegistry().put(DAMPackage.eNS_URI, DAMPackage.eINSTANCE);

        // Core
        UMLPlugin.getEPackageNsURIToProfileLocationMap()
                .put(CorePackage.eNS_URI, URI.createURI(DAM_PROFILES_PATHMAP + DAM_CORE_FRAGMENT));
        resourceSet.getPackageRegistry().put(CorePackage.eNS_URI, CorePackage.eINSTANCE);

        // Threats
        UMLPlugin.getEPackageNsURIToProfileLocationMap()
                .put(ThreatsPackage.eNS_URI, URI.createURI(DAM_PROFILES_PATHMAP + DAM_THREATS_FRAGMENT));
        resourceSet.getPackageRegistry().put(ThreatsPackage.eNS_URI, ThreatsPackage.eINSTANCE);

        // Maintenance
        UMLPlugin.getEPackageNsURIToProfileLocationMap()
                .put(MaintenancePackage.eNS_URI, URI.createURI(DAM_PROFILES_PATHMAP + DAM_MAINTENANCE_FRAGMENT));
        resourceSet.getPackageRegistry().put(MaintenancePackage.eNS_URI, MaintenancePackage.eINSTANCE);

        return resourceSet;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EasierUmlModel other = (EasierUmlModel) obj;
        if (resourceSet == null) {
            if (other.resourceSet != null)
                return false;
        } else if (!resourceSet.equals(other.resourceSet))
            return false;
        return true;
    }

    @Override
    public void disposeModel() {
        super.disposeModel();

        CacheAdapter cAdapter = CacheAdapter.getInstance();

        if (resourceSet != null) {
            while (resourceSet.getResources().size() > 0) {
                Resource res = resourceSet.getResources().get(0);

                cAdapter.clear(res);

                resourceSet.getResources().remove(res);
            }
        }
        resourceSet = null;

    }

    /**
     * Return the elements of the model of the given type and stereotype
     * @param type is the metaclass of the element
     * @param stereotype is the fully qualified stereotype name
     * @return a list of elements of the given type and stereotype
     * @throws EolModelElementTypeNotFoundException
     */
    public List<EObject> filterByStereotype(String type, String stereotype) throws EolModelElementTypeNotFoundException {

            Collection<EObject> eObjects = this.getAllOfType(type);

            return eObjects.stream().filter(e -> ((Element) e).getAppliedStereotype(stereotype) != null)
                    .collect(Collectors.toList());

    }

    /**
     * Compute the perfQ between the refactored model and the source one
     * @param refactoredModel is the model obtained after the refactoring
     * @return the performance quality average over all the performance indeces
     * @throws EolModelElementTypeNotFoundException
     */
    public double computePerfQ(final EasierUmlModel refactoredModel) throws EolModelElementTypeNotFoundException {

        String gqamNamespace = "MARTE::MARTE_AnalysisModel::GQAM::";
        String gaScenarioTag = gqamNamespace + "GaScenario";
        String gaExecHostTag = gqamNamespace + "GaExecHost";
        List<EObject> nodes = this.filterByStereotype("Node", gaExecHostTag);
        List<EObject> scenarios = this.filterByStereotype("UseCase", gaScenarioTag);

        List<EObject> sourceElements = new ArrayList<>(nodes);
        sourceElements.addAll(scenarios);

        int numberOfMetrics = 0;

        // Variable representing the perfQ value
        double value = 0d;

        // for each elements of the source model, it is picked the element with the same
        // id in the refactored one
        for (EObject element : sourceElements) {

            String id = ((XMLResource) this.getResource()).getID(element);
            EObject correspondingElement = (EObject) refactoredModel.getElementById(id);

            if (element instanceof UseCase) {
                value += -1 * this.computePerfQValue((Element) element, (Element) correspondingElement, gaScenarioTag,
                        "respT");
                value += this.computePerfQValue((Element) element, (Element) correspondingElement, gaScenarioTag,
                        "throughput");
                numberOfMetrics += 2;
            } else if (element instanceof Node) {
                value += -1 * this.computePerfQValue((Element) element, (Element) correspondingElement, gaExecHostTag,
                        "utilization");
                numberOfMetrics++;
            }
        }

        return numberOfMetrics!=0 ? value / numberOfMetrics : Double.MAX_VALUE;
    }

    /**
     * Compute the perfQ value for a specific metric
     * @param sourceElement is the source UML element
     * @param refactoredElement is the refactored UML element
     * @param stereotypeName is the fully qualified stereotype name
     * @param taggedValue is the tagged value
     * @return the perfQ value for the specific metric
     */
    private double computePerfQValue(Element sourceElement, Element refactoredElement, String stereotypeName, String taggedValue){

        //String gqamNamespace = "MARTE::MARTE_AnalysisModel::GQAM::";

        Stereotype stereotype = sourceElement.getAppliedStereotype(stereotypeName);
        EList<?> values = (EList<?>) sourceElement.getValue(stereotype, taggedValue);

        double sourceValue = 0d;
        if (!values.isEmpty())
            sourceValue = Double.parseDouble(values.get(0).toString());

        stereotype = refactoredElement.getAppliedStereotype(stereotypeName);
        values = (EList<?>) refactoredElement.getValue(stereotype, taggedValue);

        double refValue = 0d;
        if (!values.isEmpty())
            refValue = Double.parseDouble(values.get(0).toString());

        return (refValue + sourceValue) == 0 ? 0d : (refValue - sourceValue) / (refValue + sourceValue);
    }

    /**
     * Compute the system response time of the model.
     * The system response time is the sum of the response time of each use case
     * @return the system response time
     * @throws EolModelElementTypeNotFoundException
     */
    public double computeSystemResponseTime()
            throws EolModelElementTypeNotFoundException {
        AtomicReference<Double> sysRespT = new AtomicReference<>(0d);

        String gqamNamespace = "MARTE::MARTE_AnalysisModel::GQAM::";

        String gaScenarioTag = gqamNamespace + "GaScenario";

        List<EObject> scenarios = this.filterByStereotype("UseCase", gaScenarioTag);

        scenarios.stream().map(Element.class::cast).collect(Collectors.toList()).forEach(scenario -> {
            Stereotype stereotype = scenario.getAppliedStereotype(gaScenarioTag);
            sysRespT.updateAndGet(v -> (v +
                    Double.parseDouble(((EList<?>) scenario.getValue(stereotype, "respT")).get(0).toString())));
        });

        return sysRespT.get();
    }

    /**
     * Compute the energy of the model.
     * It returns Double.MAX_VALUE, if either the UML model or the LQN is not well-formed
     * @return the system energy
     */
    public double computeEnergy() {
        /*AtomicReference<Double> energy = new AtomicReference<>(0d);
        String grmNamespace = "MARTE::MARTE_Foundations::GRM::";
        String grmResourceUsageTag = grmNamespace + "ResourceUsage";
        List<EObject> nodes = this.filterByStereotype("Node", grmResourceUsageTag);

        nodes.stream().map(NamedElement.class::cast).collect(Collectors.toList()).forEach(node -> {
            Stereotype grmStereotype= node.getAppliedStereotype(grmResourceUsageTag);
            Stereotype gqamStereotype = node.getAppliedStereotype("MARTE::MARTE_AnalysisModel::GQAM::GaExecHost");
            energy.updateAndGet(
                    v -> (v +
                            Double.parseDouble(((EList<?>) node.getValue(grmStereotype, "energy")).get(0).toString()) *
                                    Double.parseDouble(((EList<?>) node.getValue(gqamStereotype, "utilization")).get(0)
                                            .toString())));
        });
        return energy.get();
        */

        String umlFile = this.getModelFile();
        String lqxoFile = Paths.get(umlFile).getParent().resolve("output.lqxo").toString();
        return Energy.computeSystemEnergy(umlFile, lqxoFile);

    }

}