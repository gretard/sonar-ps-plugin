package org.sonar.plugins.powershell.sensors;

import java.io.File;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;

import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.TempFolder;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.Constants;
import org.sonar.plugins.powershell.PowershellLanguage;
import org.sonar.plugins.powershell.fillers.IssuesFiller;
import org.sonar.plugins.powershell.issues.Objects;

public class ScriptAnalyzerSensor extends BaseSensor implements org.sonar.api.batch.sensor.Sensor {

	private final TempFolder folder;

	private static final Logger LOGGER = Loggers.get(ScriptAnalyzerSensor.class);

	private final IssuesFiller issuesFiller;

	public ScriptAnalyzerSensor(final TempFolder folder) {
		this.folder = folder;
		this.issuesFiller = new IssuesFiller();

	}

	public void describe(final SensorDescriptor descriptor) {
		descriptor.onlyOnLanguage(PowershellLanguage.KEY).name(this.getClass().getSimpleName());
	}

	public void execute(final SensorContext context) {

		final Settings settings = context.settings();
		final boolean skipPlugin = settings.getBoolean(Constants.SKIP_PLUGIN);

		if (skipPlugin) {
			LOGGER.debug("Skipping sensor as skip plugin flag is set");
			return;
		}

		final String powershellExecutable = settings.getString(Constants.PS_EXECUTABLE);

		try {
			final File parserFile = folder.newFile("ps", "scriptAnalyzer.ps1");

			try {
				FileUtils.copyURLToFile(getClass().getResource("/scriptAnalyzer.ps1"), parserFile);
			} catch (final Throwable e1) {
				LOGGER.warn("Exception while copying tokenizer script", e1);
				return;
			}

			final String scriptFile = parserFile.getAbsolutePath();

			final FileSystem fileSystem = context.fileSystem();
			final File baseDir = fileSystem.baseDir();
			final String sourceDir = baseDir.toPath().toFile().getAbsolutePath();

			final String outFile = folder.newFile().toPath().toFile().getAbsolutePath();

			final String[] args = new String[] { powershellExecutable, scriptFile, "-inputDir", sourceDir, "-output",
					outFile };

			LOGGER.info(String.format("Starting Script-Analyzer using powershell: %s", Arrays.toString(args)));
			final Process process = new ProcessBuilder(args).inheritIO().start();
			super.read(process);
			final int pReturnValue = process.waitFor();

			if (pReturnValue != 0) {
				LOGGER.info(String.format(
						"Error executing Powershell Script-Analyzer analyzer. Maybe Script-Analyzer is not installed? Error was: %s",
						read(process)));
				return;
			}

			final JAXBContext jaxbContext = JAXBContext.newInstance(Objects.class);
			final Objects issues = (Objects) jaxbContext.createUnmarshaller().unmarshal(new File(outFile));
			this.issuesFiller.fill(context, baseDir, issues);

			LOGGER.info(String.format("Script-Analyzer finished, found %s issues at %s", issues.getObject().size(),
					sourceDir));

		} catch (Throwable e) {
			LOGGER.warn("Unexpected exception while running analysis", e);
		}

	}

}
