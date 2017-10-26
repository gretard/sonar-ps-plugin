package org.sonar.plugins.powershell;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.apache.commons.lang.SystemUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.utils.TempFolder;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.issues.Objects;
import org.sonar.plugins.powershell.issues.Objects.Object.Property;

public class ScriptAnalyzerSensor implements org.sonar.api.batch.sensor.Sensor {

	private FileSystem fileSystem;

	private TempFolder folder;

	private final String psCommand = "(Invoke-ScriptAnalyzer -Path '%s' -Recurse | Select-Object RuleName, Message, Line, Column, Severity, @{Name='File';Expression={$_.Extent.File }} | ConvertTo-Xml).Save('%s')";

	private static final Logger LOGGER = Loggers.get(ScriptAnalyzerSensor.class);
	
	
	public ScriptAnalyzerSensor(final FileSystem fileSystem, final TempFolder folder) {
		this.fileSystem = fileSystem;
		this.folder = folder;

	}

	public void describe(final SensorDescriptor descriptor) {
		descriptor.onlyOnLanguage(PowershellLanguage.KEY).name(this.getClass().getSimpleName());
	}


	public void execute(final SensorContext context) {
		
		if (!SystemUtils.IS_OS_WINDOWS) {
			LOGGER.debug("Skipping sensor as OS is not windows");
			return;
		}
		
		try {
			final File resultsFile = folder.newFile();
			final File sourceDir = fileSystem.baseDir().toPath().toFile();
			
			final String command = String.format(this.psCommand,
					sourceDir.getAbsolutePath(),
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

			for (final Objects.Object o : issues.getObject()) {
				try {
					final List<Objects.Object.Property> props = o.getProperty();
					final String ruleName = getProperty("RuleName", props);
					final String initialFile = getProperty("File", props);
					final String fsFile = new PathResolver().relativePath(sourceDir, new File(initialFile));
					final String message = getProperty("Message", props);
					final int line = Integer.parseInt(getProperty("Line", props));
					final RuleKey ruleKey = RuleKey.of(ScriptAnalyzerRulesDefinition.getRepositoryKeyForLanguage(),
							ruleName);
					final NewIssue issue = context.newIssue().forRule(ruleKey);

					final org.sonar.api.batch.fs.InputFile file = fileSystem
							.inputFile(fileSystem.predicates().and(fileSystem.predicates().hasRelativePath(fsFile)));

					if (file == null) {
						LOGGER.debug(String.format("File %s not found", fsFile));
						continue;
					}

					final NewIssueLocation loc = issue.newLocation().message(message).on(file)
							.at(file.selectLine(line));
					issue.at(loc);
					issue.save();
				} catch (final Throwable e) {
					LOGGER.warn("Unexpected exception while adding issue", e);
				}

			}
		} catch (Throwable e) {
			LOGGER.warn("Unexpected exception while running analysis", e);
		}

	}

	private static String getProperty(final String key, final List<Objects.Object.Property> props) {
		for (final Property p : props) {
			if (key.equalsIgnoreCase(p.getName())) {
				return p.getValue();
			}
		}
		return null;
	}
}
