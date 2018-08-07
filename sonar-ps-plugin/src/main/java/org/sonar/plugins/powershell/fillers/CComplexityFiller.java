package org.sonar.plugins.powershell.fillers;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.ast.Tokens;

public class CComplexityFiller implements IFiller {
	private static final Logger LOGGER = Loggers.get(CComplexityFiller.class);

	@Override
	public void fill(SensorContext context, InputFile f, Tokens tokens) {
		try {
			context.<Integer>newMeasure().on(f).forMetric(CoreMetrics.COMPLEXITY).withValue(tokens.getComplexity())
					.save();

		} catch (final Throwable e) {
			LOGGER.warn("Exception while saving tokens", e);
		}

	}

}
