package org.sonar.plugins.powershell;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.utils.internal.JUnitTempFolder;

public abstract class BaseTest {
    @Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@org.junit.Rule
	public JUnitTempFolder temp = new JUnitTempFolder();

	public String getPowerShellExecutable() {
		return System.getProperty("os.name").startsWith("Win") ? "powershell.exe" : "pwsh";
	}
}