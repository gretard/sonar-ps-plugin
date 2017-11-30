package org.sonar.plugins.powershell;

import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;

@Properties({})
public class PowershellPlugin implements Plugin {

	public void define(final Context context) {
		context.addExtension(PropertyDefinition.builder(Constants.SKIP_TOKENIZER).name("Skip tokenizer")
				.description("Flag whether to skip tokenizer").defaultValue("false").type(PropertyType.BOOLEAN)
				.build());

		context.addExtension(PropertyDefinition.builder(Constants.FILE_SUFFIXES).name("Suffixes to analyze")
				.description("Suffixes supported by the plugin").defaultValue(".ps1,.psm1,.psd1")
				.type(PropertyType.STRING).build());

		context.addExtensions(PowershellLanguage.class, PowershellQualityProfile.class);
		context.addExtensions(ScriptAnalyzerRulesDefinition.class, ScriptAnalyzerSensor.class);
		context.addExtension(TokenizerSensor.class);
	}
}