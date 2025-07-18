package org.sonar.plugins.powershell.sensors;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
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
import org.sonar.plugins.powershell.readers.TokensReader;

public class TokenizerSensor extends BaseSensor implements org.sonar.api.batch.sensor.Sensor {

    private static final Logger LOGGER = Loggers.get(TokenizerSensor.class);

    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private final IFiller[] fillers = new IFiller[] { new LineMeasuresFiller(), new CpdFiller(),
            new HighlightingFiller(), new HalsteadComplexityFiller(), new CComplexityFiller() };
    private final TokensReader reader = new TokensReader();
    private final TempFolder folder;

    public TokenizerSensor(final TempFolder folder) {
        this.folder = folder;
    }

    @Override
    protected void innerExecute(final SensorContext context) {
        final Configuration config = context.config();
        final boolean skipAnalysis = config.getBoolean(Constants.SKIP_TOKENIZER).orElse(false);
        if (skipAnalysis) {
            LOGGER.debug("Skipping tokenizer as skip flag is set");
            return;
        }
        final String powershellExecutable = config.get("sonar.ps.executable").orElse("powershell.exe");

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

            final String analysisFile = SystemUtils.IS_OS_WINDOWS
                    ? String.format("'%s'", inputFile.file().getAbsolutePath())
                    : inputFile.file().getAbsolutePath();

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

                        final Tokens tokens = reader.read(tokensFile);
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

            long timeout = config.get("sonar.ps.tokenizer.timeout").map(Long::parseLong).orElse(3600L);
            LOGGER.info("Waiting for file analysis to finish for " + timeout + " seconds");
            service.shutdown();
            service.awaitTermination(timeout, TimeUnit.SECONDS);
            service.shutdownNow();
        } catch (final InterruptedException e) {
            LOGGER.warn("Unexpected error while running waiting for executor service to finish", e);
        }
    }

}
