package org.sonar.plugins.powershell;

import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

public class PowershellLanguage extends AbstractLanguage {

    public static final String NAME = "Powershell";
    public static final String PROFILE_NAME = "Powershell default rules";

    public static final String KEY = "ps";
    private final Configuration config;
    private static final String[] DEFAULT_FILE_SUFFIXES = new String[] { "ps1", "psm1", "psd1" };

    public PowershellLanguage(final Configuration config) {
        super(KEY, NAME);
        this.config = config;
    }

    public String[] getFileSuffixes() {
        final String[] suffixes = this.config.get("sonar.ps.file.suffixes").map(s -> s.split(",")).orElse(DEFAULT_FILE_SUFFIXES);
        return suffixes;
    }

}
