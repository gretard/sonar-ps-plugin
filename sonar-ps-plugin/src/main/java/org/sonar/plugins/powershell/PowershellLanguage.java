package org.sonar.plugins.powershell;

import org.sonar.api.resources.AbstractLanguage;

public class PowershellLanguage extends AbstractLanguage {

	public static final String NAME = "Powershell";
	public static final String KEY = "ps";
	public static final String[] DEFAULT_FILE_SUFFIXES = new String[] { "ps1", "psm1", "psd1" };

	public PowershellLanguage() {
		super(KEY, NAME);
	}

	public String[] getFileSuffixes() {
		return DEFAULT_FILE_SUFFIXES;
	}

}
