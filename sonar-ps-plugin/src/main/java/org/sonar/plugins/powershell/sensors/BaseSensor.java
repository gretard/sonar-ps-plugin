package org.sonar.plugins.powershell.sensors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.Constants;
import org.sonar.plugins.powershell.PowershellLanguage;

public abstract class BaseSensor implements org.sonar.api.batch.sensor.Sensor {

	private static final Logger LOGGER = Loggers.get(BaseSensor.class);

	protected static String read(Process process) throws IOException {
		return "input: " + read(process, process.getInputStream()) + " error: "
				+ read(process, process.getErrorStream());
	}

	protected static String read(Process process, InputStream stream) throws IOException {
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

	public static String formatAsFilePathArgument(String absolutePath) {
		String filePath = absolutePath;
		if(System.getProperty("os.name").startsWith("Win"))
		{
			filePath = String.format("'%s'", absolutePath);
		}
		return filePath;
	}

	@Override
	public void describe(final SensorDescriptor descriptor) {
		descriptor.onlyOnLanguage(PowershellLanguage.KEY).name(this.getClass().getSimpleName());
	}

	@Override
	public void execute(final SensorContext context) {
		final Settings settings = context.settings();
		final boolean skipPlugin = settings.getBoolean(Constants.SKIP_PLUGIN);

		if (skipPlugin) {
			LOGGER.debug("Skipping sensor as skip plugin flag is set: " + Constants.SKIP_PLUGIN);
			return;
		}

		innerExecute(context);

	}

	protected abstract void innerExecute(final SensorContext context);
}
