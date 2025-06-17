package org.sonar.plugins.powershell;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PowershellQualityProfile implements BuiltInQualityProfilesDefinition {

    private static final Logger LOGGER = Loggers.get(PowershellQualityProfile.class);

    @Override
    public void define(Context context) {
        final NewBuiltInQualityProfile profile = context
            .createBuiltInQualityProfile(Constants.PROFILE_NAME, PowershellLanguage.KEY)
            .setDefault(true);

        final String repositoryKey = Constants.REPO_KEY;

        try (InputStream rulesXml = getClass().getClassLoader().getResourceAsStream(Constants.RULES_DEFINITION_FILE)) {
            if (rulesXml == null) {
                LOGGER.warn("Rules definition file not found: {}", Constants.RULES_DEFINITION_FILE);
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDoc = builder.parse(rulesXml);

            NodeList nodes = xmlDoc.getElementsByTagName("key");
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String key = node.getTextContent();
                profile.activateRule(repositoryKey, key);
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to register PowerShell rules from file: " + Constants.RULES_DEFINITION_FILE, e);
        }

        profile.done();
    }
}
