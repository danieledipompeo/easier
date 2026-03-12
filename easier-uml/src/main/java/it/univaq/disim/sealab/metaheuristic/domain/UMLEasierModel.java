package it.univaq.disim.sealab.metaheuristic.domain;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.UMLPackage;

import it.univaq.disim.sealab.epsilon.eol.EOLStandalone;
import it.univaq.disim.sealab.epsilon.eol.EasierUmlModel;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierLogger;
import it.univaq.disim.sealab.metaheuristic.utils.UMLUtil;

public class UMLEasierModel extends EasierModel {

    /**
     * Extracts elements of type Node, Component, or Message from an XMI UML file, excluding reply messages.
     * @param xmlFilePath Path to the XMI UML file
     * @return a map with keys "Node", "Component", "Message" and values as sets of element names
     */
    public java.util.Map<String, java.util.Set<String>> extractUmlElementsFromXmi(String xmlFilePath) {
        java.util.Map<String, java.util.Set<String>> result = new java.util.HashMap<>();
        result.put("Node", new java.util.HashSet<>());
        result.put("Component", new java.util.HashSet<>());
        result.put("Message", new java.util.HashSet<>());
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(new java.io.File(xmlFilePath));
            doc.getDocumentElement().normalize();

            // Extract Nodes
            org.w3c.dom.NodeList nodeList = doc.getElementsByTagName("uml:Node");
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Element el = (org.w3c.dom.Element) nodeList.item(i);
                String name = el.getAttribute("name");
                if (name != null && !name.isEmpty()) {
                    result.get("Node").add(name);
                }
            }

            // Extract Components
            org.w3c.dom.NodeList compList = doc.getElementsByTagName("uml:Component");
            for (int i = 0; i < compList.getLength(); i++) {
                org.w3c.dom.Element el = (org.w3c.dom.Element) compList.item(i);
                String name = el.getAttribute("name");
                if (name != null && !name.isEmpty()) {
                    result.get("Component").add(name);
                }
            }

            // Extract Messages, excluding reply
            org.w3c.dom.NodeList msgList = doc.getElementsByTagName("uml:Message");
            for (int i = 0; i < msgList.getLength(); i++) {
                org.w3c.dom.Element el = (org.w3c.dom.Element) msgList.item(i);
                String name = el.getAttribute("name");
                String messageSort = el.getAttribute("messageSort");
                if (name != null && !name.isEmpty() && (messageSort == null || !messageSort.equals("reply"))) {
                    result.get("Message").add(name);
                }
            }
        } catch (Exception e) {
            EasierLogger.logger_.severe("Error extracting UML elements from XMI: " + e.getMessage());
        }
        return result;
    }

    public UMLEasierModel(String mPath) {
        super(mPath);
    }

    /**
     * Parses an XML file and extracts elements of a given type.
     * 
     * @param xmlFilePath The path to the XML file to parse
     * @param elementType The type of elements to extract
     * @return A set of strings representing the extracted elements
     */
    private Set<org.w3c.dom.Element> parseXmlForElements(String xmlFilePath, String elementType) {
        Set<org.w3c.dom.Element> elements = new HashSet<>();
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(new java.io.File(xmlFilePath));
            doc.getDocumentElement().normalize();
            
            org.w3c.dom.NodeList nodeList = doc.getElementsByTagName(elementType);
            for (int i = 0; i < nodeList.getLength(); i++) {
                elements.add((org.w3c.dom.Element) nodeList.item(i));
            }
            return elements;
        } catch (Exception e) {
            EasierLogger.logger_.severe("Error parsing XML file: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void initMap() {
        targetRefactoringElement = new HashMap<>();
        createdRefactoringElement = new HashMap<>();
        initialElements = new HashMap<>();
        for (String k : List.of(Configurator.NODE_LABEL, Configurator.COMPONENT_LABEL,
                Configurator.OPERATION_LABEL)) {
            this.targetRefactoringElement.put(k, new HashSet<>());
            this.createdRefactoringElement.put(k, new HashSet<>());
        }

        Set<org.w3c.dom.Element> _nodes = parseXmlForElements(modelPath.toString(), "uml:Node");

        // retrieve nodes, components, and operations from the model path
        Set<String> nodes = UMLUtil.getElementsInPackage(modelPath, UMLPackage.Literals.NODE)
                .stream().map(NamedElement.class::cast).map(NamedElement::getName).collect(Collectors.toSet());
        Set<String> components = UMLUtil.getElementsInPackage(modelPath, UMLPackage.Literals.COMPONENT)
                .stream().map(NamedElement.class::cast).map(NamedElement::getName).collect(Collectors.toSet());
        Set<String> operations = UMLUtil.getElementsInPackage(modelPath, UMLPackage.Literals.MESSAGE)
                .stream().map(Message.class::cast).filter(msg -> !msg.getMessageSort().toString().equals("reply"))
                .map(Message::getSignature).map(NamedElement::getName).collect(Collectors.toSet());

        // the immutable map of initial model elements
        initialElements.put(Configurator.NODE_LABEL, Collections.unmodifiableSet(nodes));
        initialElements.put(Configurator.COMPONENT_LABEL, Collections.unmodifiableSet(components));
        initialElements.put(Configurator.OPERATION_LABEL, Collections.unmodifiableSet(operations));

        // fill the element of the model path as candidates for next refactoring actions
        targetRefactoringElement.put(Configurator.NODE_LABEL, new HashSet<>(nodes));
        targetRefactoringElement.put(Configurator.COMPONENT_LABEL, new HashSet<>(components));
        targetRefactoringElement.put(Configurator.OPERATION_LABEL, new HashSet<>(operations));
    }


    public java.util.Collection<?> getAllContents(){

        try (EasierUmlModel model = EOLStandalone.createUmlModel(modelPath.toString())) {

            return model.allContents();

        } catch (URISyntaxException | EolModelLoadingException e) {
            throw new RuntimeException(e);
        }
    }

}
