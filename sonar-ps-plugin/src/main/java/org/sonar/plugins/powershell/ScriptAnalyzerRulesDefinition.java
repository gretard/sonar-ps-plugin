package org.sonar.plugins.powershell;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class ScriptAnalyzerRulesDefinition implements RulesDefinition {

	private final String rulesDefinition = "/powershell-rules.xml";

	public static final String repositoryName = "PSScriptAnalyzer";

	private static final Logger LOGGER = Loggers.get(ScriptAnalyzerRulesDefinition.class);

	public void define(final Context context) {

		final NewRepository repository = context.createRepository(PowershellLanguage.KEY, PowershellLanguage.NAME)
				.setName(repositoryName);
		try {
			final InputStream input = this.getClass().getResourceAsStream(this.rulesDefinition);
			final RulesDefinitionXmlLoader rulesLoader = new RulesDefinitionXmlLoader();
			rulesLoader.load(repository, input, StandardCharsets.UTF_8.name());

		} catch (final Throwable e) {
			LOGGER.warn("Error occured while adding rules", e);
		}
		repository.done();

	}

}
