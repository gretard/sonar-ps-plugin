package org.sonar.plugins.powershell;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class ScriptAnalyzerRulesDefinition implements RulesDefinition {

	protected static final String KEY = "psanalyzer";

	protected static final String NAME = "PsAnalyzer";

	protected static final String rulesDefinition = "/powershell-rules.xml";

	private static final Logger LOGGER = Loggers.get(ScriptAnalyzerRulesDefinition.class);

	private void defineRulesForLanguage(final Context context, final String repositoryKey, final String repositoryName,
			String languageKey) {
		final NewRepository repository = context.createRepository(repositoryKey, languageKey).setName(repositoryName);

		final InputStream rulesXml = this.getClass().getResourceAsStream(rulesDefinition);
		if (rulesXml != null) {
			final RulesDefinitionXmlLoader rulesLoader = new RulesDefinitionXmlLoader();
			rulesLoader.load(repository, rulesXml, StandardCharsets.UTF_8.name());
			LOGGER.info("Loaded: " + repository.rules().size());
		}

		repository.done();
	}

	public void define(final Context context) {
		final String repositoryKey = ScriptAnalyzerRulesDefinition.getRepositoryKeyForLanguage();
		final String repositoryName = ScriptAnalyzerRulesDefinition.getRepositoryNameForLanguage();
		defineRulesForLanguage(context, repositoryKey, repositoryName, PowershellLanguage.KEY);
	}

	public static String getRepositoryKeyForLanguage() {
		return PowershellLanguage.KEY.toLowerCase() + "-" + KEY;
	}

	public static String getRepositoryNameForLanguage() {
		return PowershellLanguage.KEY.toUpperCase() + " " + NAME;
	}

}
