package org.sonar.plugins.powershell.sensors;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;

import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
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
import org.sonar.plugins.powershell.fillers.LineMeasuresFiller;

public class TokenizerSensor extends BaseSensor implements org.sonar.api.batch.sensor.Sensor {

	private static final Logger LOGGER = Loggers.get(TokenizerSensor.class);

	private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

	private final IFiller[] fillers = new IFiller[] { new LineMeasuresFiller(), new CpdFiller(),
			new HighlightingFiller(), new HalsteadComplexityFiller(), new CComplexityFiller() };

	private final TempFolder folder;

	public TokenizerSensor(final TempFolder folder) {
		this.folder = folder;
	}

	@Override
	protected void innerExecute(final SensorContext context) {

		final Settings settings = context.settings();
		final boolean skipAnalysis = settings.getBoolean(Constants.SKIP_TOKENIZER);

		if (skipAnalysis) {
			LOGGER.debug("Skipping tokenizer as skip flag is set");
			return;
		}

		final String powershellExecutable = settings.getString(Constants.PS_EXECUTABLE);

		final File parserFile = folder.newFile("ps", "parser.ps1");

		try {
			FileUtils.copyURLToFile(getClass().getResource("/parser.ps1"), parserFile);
		} catch (final Throwable e1) {
			LOGGER.warn("Exception while copying tokenizer script", e1);
			return;
		}
		final String scriptFile = parserFile.getAbsolutePath();
		final org.sonar.api.batch.fs.FileSystem fs = context.fileSystem();
		final FilePredicates p = fs.predicates();
		ExecutorService service = Executors.newWorkStealingPool();
		final Iterable<InputFile> inputFiles = fs.inputFiles(p.and(p.hasLanguage(PowershellLanguage.KEY)));
		for (final InputFile inputFile : inputFiles) {
			final String analysisFile = String.format("'%s'", inputFile.file().getAbsolutePath());

			// skip reporting temp files
			if (analysisFile.contains(".scannerwork")) {
				continue;
			}

			service.submit(new Runnable() {

				@Override
				public void run() {
					try {
						final String resultsFile = folder.newFile().toPath().toFile().getAbsolutePath();

						final String[] args = new String[] { powershellExecutable, scriptFile, "-inputFile",
								analysisFile, "-output", resultsFile };
						if (isDebugEnabled) {
							LOGGER.debug(String.format("Running %s command", Arrays.toString(args)));
						}
						final Process process = new ProcessBuilder(args).inheritIO().redirectOutput(Redirect.PIPE)
								.redirectErrorStream(true).start();

						final int pReturnValue = process.waitFor();

						if (pReturnValue != 0) {
							LOGGER.warn(String.format("Tokenizer did not run successfully on %s file. Error was: %s",
									analysisFile, read(process)));
							return;
						}
						final File tokensFile = new File(resultsFile);
						if (!tokensFile.exists() || tokensFile.length() <= 0) {
							LOGGER.warn(String.format(
									"Tokenizer did not run successfully on %s file. Please check file contents.",
									analysisFile));
							return;
						}

						final Tokens tokens = readTokens(tokensFile);
						for (final IFiller filler : fillers) {
							filler.fill(context, inputFile, tokens);
						}
						if (isDebugEnabled) {
							LOGGER.debug(String.format("Running analysis for %s to %s finished.", analysisFile,
									resultsFile));
						}
					} catch (final Throwable e) {
						LOGGER.warn(String.format("Unexpected exception while running tokenizer on %s", inputFile), e);
					}
				}
			});

		}
		try {

			long timeout = settings.getLong(Constants.TIMEOUT_TOKENIZER) == 0 ? 3600
					: settings.getLong(Constants.TIMEOUT_TOKENIZER);
			LOGGER.info("Waiting for file analysis to finish for " + timeout + " seconds");
			service.shutdown();
			service.awaitTermination(timeout, TimeUnit.SECONDS);
			service.shutdownNow();
		} catch (final InterruptedException e) {
			LOGGER.warn("Unexpected error while running waiting for executor service to finish", e);

		}

	}

	private static Tokens readTokens(final File file) throws Exception {
		final JAXBContext jaxbContext = JAXBContext.newInstance(Tokens.class);
		final Tokens issues = (Tokens) jaxbContext.createUnmarshaller().unmarshal(file);
		return issues;

	}

}
