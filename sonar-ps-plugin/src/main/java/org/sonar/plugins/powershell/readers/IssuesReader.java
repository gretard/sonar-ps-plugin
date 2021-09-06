package org.sonar.plugins.powershell.readers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.issues.PsIssue;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class IssuesReader {
    private static final Logger LOGGER = Loggers.get(IssuesReader.class);

    public List<PsIssue> read(final File file) throws Throwable {
        final InputStream main = new FileInputStream(file);
        return read(main);
    }

    public List<PsIssue> read(final InputStream main) throws ParserConfigurationException, SAXException, IOException {
        final List<PsIssue> issues = new LinkedList<>();
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(new BOMInputStream(main));
        final NodeList list = doc.getElementsByTagName("Object");
        for (int i = 0; i < list.getLength(); i++) {
            final Node node = list.item(i);
            try {

                final PsIssue issue = new PsIssue();
                issue.ruleId = getNodeByAttributeName(node, "RuleName").getTextContent();

                issue.message = getNodeByAttributeName(node, "Message").getTextContent();
                String line = getNodeByAttributeName(node, "Line").getTextContent();
                if (!StringUtils.isEmpty(line)) {
                    issue.line = Integer.parseInt(line);
                }

                issue.severity = getNodeByAttributeName(node, "Severity").getTextContent();
                issue.file = getNodeByAttributeName(node, "File").getTextContent();
                issues.add(issue);
            } catch (Exception e) {
                LOGGER.warn("Unexpected error reading results from: " + node.getTextContent(), e);
            }
        }
        return issues;
    }

    protected static final Node getNodeByAttributeName(final Node root, final String name) throws Exception {
        final NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child.getAttributes() == null) {
                continue;
            }
            final Node attribute = child.getAttributes().getNamedItem("Name");
            if (attribute != null && attribute.getTextContent().equalsIgnoreCase(name)) {
                return child;
            }
        }
        throw new Exception("Child node with attribute named: '" + name + "' was not found");
    }
}
