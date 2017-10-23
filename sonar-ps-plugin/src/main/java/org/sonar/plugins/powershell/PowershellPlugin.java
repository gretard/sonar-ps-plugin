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

		context.addExtensions(PowershellLanguage.class, PowershellQualityProfile.class);
		context.addExtensions(ScriptAnalyzerRulesDefinition.class, ScriptAnalyzerSensor.class);
		context.addExtension(TokenizerSensor.class);
	}
}