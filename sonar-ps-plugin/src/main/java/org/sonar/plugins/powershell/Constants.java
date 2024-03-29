package org.sonar.plugins.powershell;

public final class Constants {
    public static final String PROFILE_NAME = "Sonar Way";

    public static final String SKIP_TOKENIZER = "sonar.ps.tokenizer.skip";

    public static final String TIMEOUT_TOKENIZER = "sonar.ps.tokenizer.timeout";

    public static final String FILE_SUFFIXES = "sonar.ps.file.suffixes";

    public static final String PS_EXECUTABLE = "sonar.ps.executable";

    public static final String SKIP_PLUGIN = "sonar.ps.plugin.skip";

    public static final String EXTERNAL_RULES_SKIP_LIST = "sonar.ps.external.rules.skip";

    public static final String RULES_DEFINITION_FILE = "powershell-rules.xml";

    public static final String KEY = "psanalyzer";

    public static final String NAME = "PsAnalyzer";

    public static final String REPO_KEY = PowershellLanguage.KEY.toLowerCase() + "-" + Constants.KEY;

    public static final String REPO_NAME = PowershellLanguage.KEY.toUpperCase() + " " + Constants.NAME;

    private Constants() {
        throw new IllegalStateException("Constants class");
    }
}
