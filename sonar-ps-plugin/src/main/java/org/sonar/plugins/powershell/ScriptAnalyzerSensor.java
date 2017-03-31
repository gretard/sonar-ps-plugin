package org.sonar.plugins.powershell;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.utils.TempFolder;
import org.sonar.plugins.powershell.issues.Objects;
import org.sonar.plugins.powershell.issues.Objects.Object.Property;

public class ScriptAnalyzerSensor implements org.sonar.api.batch.sensor.Sensor {

	private FileSystem fileSystem;
	private TempFolder folder;

	public ScriptAnalyzerSensor(final FileSystem fileSystem, final TempFolder folder) {
		this.fileSystem = fileSystem;
		this.folder = folder;

	}

	public void describe(final SensorDescriptor descriptor) {
		descriptor.onlyOnLanguage(PowershellLanguage.KEY).name(this.getClass().getSimpleName());
	}

	private final String psCommand = "(Invoke-ScriptAnalyzer -Path '%s' -Recurse | Select-Object RuleName, Message, Line, Column, Severity, @{Name='File';Expression={$_.Extent.File }} | ConvertTo-Xml).Save('%s')";

	public void execute(final SensorContext context) {

		try {
			final File results = folder.newFile();
			File baseDir = fileSystem.baseDir().toPath().toFile();
			final String command = String.format(this.psCommand,
					fileSystem.baseDir().toPath().toFile().getAbsolutePath(),
					results.toPath().toFile().getAbsolutePath());

			final Process process = new ProcessBuilder("powershell.exe", command).start();
			final int returnVal = process.waitFor();

			JAXBContext jaxbContext = JAXBContext.newInstance(Objects.class);

			Objects issues = (Objects) jaxbContext.createUnmarshaller().unmarshal(results);

			for (final Objects.Object o : issues.getObject()) {
				try {
					final List<Objects.Object.Property> props = o.getProperty();
					final String ruleName = getProperty("RuleName", props);
					String initialFile = getProperty("File", props);
					final String fsFile = new PathResolver().relativePath(baseDir, new File(initialFile));

					final String message = getProperty("Message", props);
					final int line = Integer.parseInt(getProperty("Line", props));
					RuleKey ruleKey = RuleKey.of(ScriptAnalyzerRulesDefinition.repositoryName, ruleName);
					NewIssue issue = context.newIssue().forRule(ruleKey);

					org.sonar.api.batch.fs.InputFile file = fileSystem
							.inputFile(fileSystem.predicates().and(fileSystem.predicates().hasRelativePath(fsFile)));

					if (file == null) {

						continue;
					}

					NewIssueLocation loc = issue.newLocation().message(message).on(file).at(file.selectLine(line));

					issue.at(loc);
					issue.save();
				} catch (Throwable e) {
					e.printStackTrace();
				}

			}
		} catch (Throwable e) {
			e.printStackTrace();
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
