package org.sonar.plugins.powershell;

import org.sonar.api.config.Settings;
import org.sonar.api.resources.AbstractLanguage;

public class PowershellLanguage extends AbstractLanguage {

    public static final String NAME = "Powershell";
    public static final String PROFILE_NAME = "Powershell default rules";

    public static final String KEY = "ps";
    private final Settings settings;
    private static final String[] DEFAULT_FILE_SUFFIXES = new String[] { "ps1", "psm1", "psd1" };

    public PowershellLanguage(final Settings settings) {
        super(KEY, NAME);
        this.settings = settings;

    }

    public String[] getFileSuffixes() {
        final String[] suffixes = this.settings.getStringArray(Constants.FILE_SUFFIXES);
        if (suffixes == null || suffixes.length == 0) {
            return DEFAULT_FILE_SUFFIXES;
        }
        return suffixes;
    }

}
