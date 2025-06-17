package org.sonar.plugins.powershell;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ScriptAnalyzerRulesDefinition implements RulesDefinition {

    private static final Logger LOGGER = Loggers.get(ScriptAnalyzerRulesDefinition.class);

    @Override
    public void define(Context context) {
        final String repositoryKey = Constants.REPO_KEY;
        final String repositoryName = Constants.REPO_NAME;
        final String languageKey = PowershellLanguage.KEY;

        NewRepository repository = context.createRepository(repositoryKey, languageKey)
            .setName(repositoryName);

        try (InputStream rulesXml = getClass().getClassLoader().getResourceAsStream(Constants.RULES_DEFINITION_FILE)) {
            if (rulesXml == null) {
                LOGGER.warn("Rules definition file not found: {}", Constants.RULES_DEFINITION_FILE);
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDoc = builder.parse(rulesXml);
            NodeList ruleNodes = xmlDoc.getElementsByTagName("rule");

            for (int i = 0; i < ruleNodes.getLength(); i++) {
                try {
                    Node ruleNode = ruleNodes.item(i);
                    String key = getChild(ruleNode, "key");
                    String name = getChild(ruleNode, "name");
                    String description = getChild(ruleNode, "description");
                    String constantPerIssue = getChild(ruleNode, "debtRemediationFunctionCoefficient");
                    String severity = getChild(ruleNode, "severity");

                    NewRule rule = repository.createRule(key)
                        .setName(name)
                        .setMarkdownDescription(description)
                        .setSeverity(severity);

                    rule.setDebtRemediationFunction(
                        rule.debtRemediationFunctions().constantPerIssue(constantPerIssue)
                    );

                } catch (Exception e) {
                    LOGGER.warn("Error registering rule at index " + i, e);
                }
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to load or parse rule definitions from: " + Constants.RULES_DEFINITION_FILE, e);
        }

        repository.done();
    }

    private static String getChild(Node parent, String key) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (key.equalsIgnoreCase(node.getNodeName())) {
                return node.getTextContent().trim();
            }
        }
        return "";
    }
}
