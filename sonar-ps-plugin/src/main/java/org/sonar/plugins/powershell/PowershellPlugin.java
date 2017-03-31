package org.sonar.plugins.powershell;

import org.sonar.api.Plugin;
import org.sonar.api.Properties;

/**
 * This class is the entry point for all extensions
 */
@Properties({})
public class PowershellPlugin implements Plugin {

	public void define(final Context context) {
		context.addExtensions(PowershellLanguage.class, PowershellQualityProfile.class);

		context.addExtensions(ScriptAnalyzerRulesDefinition.class, ScriptAnalyzerSensor.class);

	}
}