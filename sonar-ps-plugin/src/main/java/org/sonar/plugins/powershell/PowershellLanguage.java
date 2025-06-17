package org.sonar.plugins.powershell;

import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

public class PowershellLanguage extends AbstractLanguage {

    public static final String KEY = "ps";
    public static final String NAME = "Powershell";
    public static final String PROFILE_NAME = "Powershell default rules";
    private static final String[] DEFAULT_FILE_SUFFIXES = new String[] { "ps1", "psm1", "psd1" };

    private final Configuration configuration;

    public PowershellLanguage(Configuration configuration) {
        super(KEY, NAME);
        this.configuration = configuration;
    }

    @Override
    public String[] getFileSuffixes() {
        return configuration.get(Constants.FILE_SUFFIXES)
                .map(s -> s.split("\\s*,\\s*"))
                .filter(arr -> arr.length > 0)
                .orElse(DEFAULT_FILE_SUFFIXES);
    }
}
