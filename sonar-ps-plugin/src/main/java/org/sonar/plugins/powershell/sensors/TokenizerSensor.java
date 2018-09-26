package org.sonar.plugins.powershell.sensors;

import java.io.File;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;

import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.TempFolder;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.Constants;
import org.sonar.plugins.powershell.PowershellLanguage;
import org.sonar.plugins.powershell.ast.Tokens;
import org.sonar.plugins.powershell.fillers.CComplexityFiller;
import org.sonar.plugins.powershell.fillers.CpdFiller;
import org.sonar.plugins.powershell.fillers.HalsteadComplexityFiller;
import org.sonar.plugins.powershell.fillers.HighlightingFiller;
import org.sonar.plugins.powershell.fillers.IFiller;

public class TokenizerSensor extends BaseSensor implements org.sonar.api.batch.sensor.Sensor {

	private static final Logger LOGGER = Loggers.get(TokenizerSensor.class);

	private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

	private final IFiller[] fillers = new IFiller[] { new CpdFiller(), new HighlightingFiller(),
			new HalsteadComplexityFiller(), new CComplexityFiller() };

	private final TempFolder folder;

	public TokenizerSensor(final TempFolder folder) {
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

		final Settings settings = context.settings();
		final boolean skipAnalysis = settings.getBoolean(Constants.SKIP_TOKENIZER);
		final boolean skipPlugin = settings.getBoolean(Constants.SKIP_PLUGIN);

		if (skipPlugin) {
			LOGGER.debug("Skipping sensor as skip plugin flag is set");
			return;
		}

		final String powershellExecutable = settings.getString(Constants.PS_EXECUTABLE);

		if (skipAnalysis) {
			LOGGER.debug("Skipping tokenizer as skip flag is set");
			return;
		}

		final File parserFile = folder.newFile("ps", "parser.ps1");

		try {
			FileUtils.copyURLToFile(getClass().getResource("/parser.ps1"), parserFile);
		} catch (final Throwable e1) {
			LOGGER.warn("Exception while copying tokenizer script", e1);
			return;
		}
		LOGGER.info("Tokenizer sensor started");
		final String scriptFile = parserFile.getAbsolutePath();
		final Iterable<InputFile> inputFiles = context.fileSystem()
				.inputFiles(context.fileSystem().predicates().hasLanguage(PowershellLanguage.KEY));
		for (final InputFile inputFile : inputFiles) {
			try {

				final String fileUnderAnalysis = inputFile.file().getAbsolutePath();
				final String resultsFile = folder.newFile().toPath().toFile().getAbsolutePath();
				final String[] args = new String[] { powershellExecutable, "-File", String.format("\"%s\"", scriptFile),
						"-inputFile", String.format("\"%s\"", fileUnderAnalysis), "-output",
						String.format("\"%s\"", resultsFile)

				};
				if (isDebugEnabled) {
					LOGGER.debug(String.format("Running %s command", Arrays.toString(args)));
				}
				final Process process = new ProcessBuilder(args).start();

				final int pReturnValue = process.waitFor();

				final File tokensFile = new File(resultsFile);
				if (pReturnValue != 0 || !tokensFile.exists() || tokensFile.length() == 0) {
					LOGGER.info(String.format(
							"Something went wrong while running  tokenizer. Return code was: %s. File was: %s. Error output was: %s. Actual output was: %s",
							pReturnValue, resultsFile, read(process.getErrorStream()), read(process.getInputStream())));
					continue;
				}

				final Tokens tokens = readTokens(tokensFile);
				for (final IFiller filler : this.fillers) {
					filler.fill(context, inputFile, tokens);
				}
				if (isDebugEnabled) {
					LOGGER.debug(
							String.format("Running analysis for %s to %s finished.", fileUnderAnalysis, resultsFile));
				}
			} catch (final Throwable e) {
				LOGGER.warn(String.format("Unexpected exception while running tokenizer on %s", inputFile), e);
			}

		}
		LOGGER.info("Tokenizer sensor finished");

	}

}
