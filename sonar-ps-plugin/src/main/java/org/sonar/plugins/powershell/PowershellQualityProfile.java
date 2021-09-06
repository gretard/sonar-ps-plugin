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
    public void define(final Context context) {

        final NewBuiltInQualityProfile profile = context
                .createBuiltInQualityProfile(Constants.PROFILE_NAME, PowershellLanguage.KEY).setDefault(true);
        final String repositoryKey = Constants.REPO_KEY;

        try (final InputStream rulesXml = this.getClass().getClassLoader()
                .getResourceAsStream(Constants.RULES_DEFINITION_FILE)) {

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document xmlDoc = builder.parse(rulesXml);
            final NodeList nodes = xmlDoc.getElementsByTagName("key");
            for (int i = 0; i < nodes.getLength(); i++) {
                final Node node = nodes.item(i);
                final String key = node.getTextContent();
                profile.activateRule(repositoryKey, key);
            }

        } catch (Exception e) {
            LOGGER.warn("Unexpected error while registering rules", e);
        }
        profile.done();

    }

}