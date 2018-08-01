package org.sonar.plugins.powershell;

import java.io.File;

import javax.xml.bind.JAXBContext;

import org.apache.commons.lang.SystemUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.TempFolder;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.fillers.IssuesFiller;
import org.sonar.plugins.powershell.issues.Objects;

public class ScriptAnalyzerSensor implements org.sonar.api.batch.sensor.Sensor {

	private FileSystem fileSystem;

	private final TempFolder folder;

	private static final String psCommand = "(Invoke-ScriptAnalyzer -Path '%s' -Recurse | Select-Object RuleName, Message, Line, Column, Severity, @{Name='File';Expression={$_.Extent.File }} | ConvertTo-Xml).Save('%s')";

	private static final Logger LOGGER = Loggers.get(ScriptAnalyzerSensor.class);

	private final IssuesFiller issuesFiller;

	public ScriptAnalyzerSensor(final FileSystem fileSystem, final TempFolder folder) {
		this.fileSystem = fileSystem;
		this.folder = folder;
		this.issuesFiller = new IssuesFiller(fileSystem);

	}

	public void describe(final SensorDescriptor descriptor) {
		descriptor.onlyOnLanguage(PowershellLanguage.KEY).name(this.getClass().getSimpleName());
	}

	public void execute(final SensorContext context) {

		if (!SystemUtils.IS_OS_WINDOWS) {
			LOGGER.info("Skipping sensor as OS is not windows");
			return;
		}

		try {
			final File resultsFile = folder.newFile();
			final File sourceDir = fileSystem.baseDir().toPath().toFile();

			final String command = String.format(psCommand, sourceDir.getAbsolutePath(),
					resultsFile.toPath().toFile().getAbsolutePath());

			try {
				LOGGER.info(String.format("Starting running powershell analysis: %s", command));
				final Process process = new ProcessBuilder("powershell.exe", command).start();
				process.waitFor();
				LOGGER.info("Finished running powershell analysis");

			} catch (final Throwable e) {
				LOGGER.warn("Error executing Powershell script analyzer. Maybe Script-Analyzer is not installed?", e);
				return;
			}

			final JAXBContext jaxbContext = JAXBContext.newInstance(Objects.class);
			final Objects issues = (Objects) jaxbContext.createUnmarshaller().unmarshal(resultsFile);
			this.issuesFiller.fill(context, sourceDir, issues);
		} catch (Throwable e) {
			LOGGER.warn("Unexpected exception while running analysis", e);
		}

	}

}
