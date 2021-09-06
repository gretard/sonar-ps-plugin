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

    public void define(final Context context) {
        final String repositoryKey = Constants.REPO_KEY;
        final String repositoryName = Constants.REPO_NAME;
        final String languageKey = PowershellLanguage.KEY;
        defineRulesForLanguage(context, repositoryKey, repositoryName, languageKey);
    }

    private void defineRulesForLanguage(final Context context, final String repositoryKey, final String repositoryName,
            String languageKey) {
        final NewRepository repository = context.createRepository(repositoryKey, languageKey).setName(repositoryName);

        try (final InputStream rulesXml = this.getClass().getClassLoader()
                .getResourceAsStream(Constants.RULES_DEFINITION_FILE)) {

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document xmlDoc = builder.parse(rulesXml);

            final NodeList nodes = xmlDoc.getElementsByTagName("rule");
            for (int i = 0; i < nodes.getLength(); i++) {
                try {
                    final Node node = nodes.item(i);
                    final String key = getChild(node, "key");
                    final String name = getChild(node, "name");
                    final String description = getChild(node, "description");

                    final String constantPerIssue = getChild(node, "debtRemediationFunctionCoefficient");
                    final String severity = getChild(node, "severity");
                    final NewRule rule = repository.createRule(key).setName(name).setMarkdownDescription(description)
                            .setSeverity(severity);
                    rule.setDebtRemediationFunction(rule.debtRemediationFunctions().constantPerIssue(constantPerIssue));
                } catch (final Exception e) {
                    LOGGER.warn(String.format("Unexpected error while registering rule: %s", i), e);

                }
            }

        } catch (final Exception e1) {
            LOGGER.warn("Unexpected error while registering rules", e1);
        }
        repository.done();

    }

    private static String getChild(final Node parent, final String key) {
        final NodeList nodes = parent.getChildNodes();
        final int cnt = nodes.getLength();
        for (int i = 0; i < cnt; i++) {
            var node = nodes.item(i);
            if (key.equalsIgnoreCase(node.getNodeName())) {
                return node.getTextContent();
            }
        }
        return "";
    }

}
