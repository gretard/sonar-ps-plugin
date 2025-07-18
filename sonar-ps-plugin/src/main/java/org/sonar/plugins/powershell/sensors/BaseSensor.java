package org.sonar.plugins.powershell.sensors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.Constants;
import org.sonar.plugins.powershell.PowershellLanguage;

public abstract class BaseSensor implements org.sonar.api.batch.sensor.Sensor {

    private static final Logger LOGGER = Loggers.get(BaseSensor.class);

    @Override
    public void describe(final SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage(PowershellLanguage.KEY).name(this.getClass().getSimpleName());
    }

    @Override
    public void execute(final SensorContext context) {
        final Configuration config = context.config();
        final boolean skipPlugin = config.getBoolean(Constants.SKIP_PLUGIN).orElse(false);

        if (skipPlugin) {
            LOGGER.debug("Skipping sensor as skip plugin flag is set: " + Constants.SKIP_PLUGIN);
            return;
        }

        innerExecute(context);

    }

    protected abstract void innerExecute(final SensorContext context);

    protected static String read(Process process) throws IOException {
        return "input: " + read(process.getInputStream()) + " error: " + read(process.getErrorStream());
    }

    protected static String read(InputStream stream) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        final StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        reader.close();
        return builder.toString();

    }
}
