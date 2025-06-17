package org.sonar.plugins.powershell;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.powershell.sensors.ScriptAnalyzerSensor;
import org.sonar.plugins.powershell.sensors.TokenizerSensor;

public class PowershellPlugin implements Plugin {

    @Override
    public void define(final Context context) {
        context.addExtension(PropertyDefinition.builder(Constants.SKIP_TOKENIZER)
                .name("Skip tokenizer")
                .description("Flag whether to skip tokenizer")
                .defaultValue("false")
                .build());

        context.addExtension(PropertyDefinition.builder(Constants.SKIP_PLUGIN)
                .name("Skip plugin")
                .description("Flag whether to skip plugin")
                .defaultValue("false")
                .build());

        context.addExtension(PropertyDefinition.builder(Constants.PS_EXECUTABLE)
                .name("Path to powershell executable")
                .description("Path to powershell executable")
                .defaultValue("powershell.exe")
                .build());

        context.addExtension(PropertyDefinition.builder(Constants.TIMEOUT_TOKENIZER)
                .name("Tokenizer timeout")
                .description("Max time in seconds to wait until tokenizer finishes")
                .defaultValue("3600")
                .build());

        context.addExtension(PropertyDefinition.builder(Constants.FILE_SUFFIXES)
                .name("Suffixes to analyze")
                .description("Suffixes supported by the plugin")
                .defaultValue(".ps1,.psm1,.psd1")
                .build());

        context.addExtension(PropertyDefinition.builder(Constants.EXTERNAL_RULES_SKIP_LIST)
                .name("External rules reporting skip list")
                .description("List of repoId:ruleId pairs to skip reporting")
                .multiValues(true)
                .build());

        context.addExtensions(
                PowershellLanguage.class,
                ScriptAnalyzerRulesDefinition.class,
                PowershellQualityProfile.class,
                ScriptAnalyzerSensor.class,
                TokenizerSensor.class
        );
    }
}
