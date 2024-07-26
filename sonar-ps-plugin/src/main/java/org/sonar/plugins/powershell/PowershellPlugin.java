package org.sonar.plugins.powershell;

import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.powershell.sensors.ScriptAnalyzerSensor;
import org.sonar.plugins.powershell.sensors.TokenizerSensor;

@Properties({})
public class PowershellPlugin implements Plugin {

    public void define(final Context context) {
        context.addExtension(PropertyDefinition.builder(Constants.SKIP_TOKENIZER).name("Skip tokenizer")
                .description("Flag whether to skip tokenizer").defaultValue("false").type(PropertyType.BOOLEAN)
                .build());
        context.addExtension(PropertyDefinition.builder(Constants.SKIP_PLUGIN).name("Skip plugin")
                .description("Flag whether to skip plugin").defaultValue("false").type(PropertyType.BOOLEAN).build());
        context.addExtension(PropertyDefinition.builder(Constants.PS_EXECUTABLE).name("Path to powershell executable")
                .description("Path to powershell executable").defaultValue("powershell.exe").type(PropertyType.STRING)
                .build());
        context.addExtension(PropertyDefinition.builder(Constants.TIMEOUT_TOKENIZER).name("Tokenizer timeout")
                .description("Max time in seconds to wait until tokenizer finishes").defaultValue("3600")
                .type(PropertyType.INTEGER).build());
        context.addExtension(PropertyDefinition.builder(Constants.FILE_SUFFIXES).name("Suffixes to analyze")
                .description("Suffixes supported by the plugin").defaultValue(".ps1,.psm1,.psd1")
                .multiValues(true).type(PropertyType.STRING).build());

        context.addExtension(PropertyDefinition.builder(Constants.EXTERNAL_RULES_SKIP_LIST)
                .name("External rules reporting skip list").description("List of repoId:ruleId pairs to skip reporting")
                .multiValues(true).type(PropertyType.STRING).build());

        context.addExtensions(PowershellLanguage.class, ScriptAnalyzerRulesDefinition.class);
        context.addExtensions(PowershellQualityProfile.class, ScriptAnalyzerSensor.class);
        context.addExtension(TokenizerSensor.class);
    }
}