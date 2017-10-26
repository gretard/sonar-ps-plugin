package org.sonar.plugins.powershell;

import static java.lang.String.format;

import java.io.File;

import javax.xml.bind.JAXBContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.TempFolder;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.ast.Token;
import org.sonar.plugins.powershell.ast.Tokens;
import org.sonar.plugins.powershell.fillers.CpdFiller;
import org.sonar.plugins.powershell.fillers.HighlightingFiller;
import org.sonar.plugins.powershell.fillers.IFiller;

public class TokenizerSensor implements org.sonar.api.batch.sensor.Sensor {

	private final String psCommand = "%s -inputName %s -output %s";

	private final TempFolder folder;

	private final Settings settings;
	private static final Logger LOGGER = Loggers.get(TokenizerSensor.class);
	private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
	private final IFiller[] fillers = new IFiller[] { new CpdFiller(), new HighlightingFiller() };

	public TokenizerSensor(final Settings settings, final TempFolder folder) {
		this.settings = settings;
		this.folder = folder;
	}

	@Override
	public void describe(final SensorDescriptor descriptor) {
		descriptor.onlyOnLanguage(PowershellLanguage.KEY).name(this.getClass().getSimpleName());
	}

	private Tokens readTokens(final File file) throws Exception {
		final JAXBContext jaxbContext = JAXBContext.newInstance(Tokens.class);
		final Tokens issues = (Tokens) jaxbContext.createUnmarshaller().unmarshal(file);
		return issues;

	}

	@Override
	public void execute(final SensorContext context) {
		if (!SystemUtils.IS_OS_WINDOWS) {
			LOGGER.debug("Skipping sensor as OS is not windows");
			return;
		}

		final boolean skipAnalysis = this.settings.getBoolean(Constants.SKIP_TOKENIZER);

		if (skipAnalysis) {
			LOGGER.debug(format("Skipping tokennizer as skip flag is set"));
			return;
		}
		final File parserFile = folder.newFile("ps", "parser.ps1");

		try {
			FileUtils.copyURLToFile(getClass().getResource("/parser.ps1"), parserFile);
		} catch (final Throwable e1) {
			LOGGER.warn("Exception while copying tokenizer script", e1);
			return;
		}
		final String scriptFile = parserFile.getAbsolutePath();
		final Iterable<InputFile> inputFiles = context.fileSystem()
				.inputFiles(context.fileSystem().predicates().hasLanguage(PowershellLanguage.KEY));
		for (final InputFile inputFile : inputFiles) {
			try {

				final String analysisFile = inputFile.file().getAbsolutePath();
				final String resultsFile = folder.newFile().toPath().toFile().getAbsolutePath();
				final String command = String.format(this.psCommand, scriptFile, analysisFile, resultsFile);
				if (isDebugEnabled) {
					LOGGER.debug(String.format("Running %s command", command));
				}
				final Process process = new ProcessBuilder("powershell.exe", command).start();
				process.waitFor();
				final Tokens tokens = readTokens(new File(resultsFile));
				for (final IFiller filler : this.fillers) {
					filler.fill(context, inputFile, tokens);
				}
				if (isDebugEnabled) {
					LOGGER.debug(String.format("Running analysis for %s to %s finished", analysisFile, resultsFile));
				}
			} catch (final Throwable e) {
				LOGGER.warn("Exception while running tokenizer", e);
			}
		}

	}
}
