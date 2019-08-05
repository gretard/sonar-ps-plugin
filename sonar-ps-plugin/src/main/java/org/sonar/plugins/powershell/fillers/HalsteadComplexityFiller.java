package org.sonar.plugins.powershell.fillers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.ast.Tokens;
import org.sonar.plugins.powershell.ast.Tokens.Token;

public class HalsteadComplexityFiller implements IFiller {
	private static final Logger LOGGER = Loggers.get(HalsteadComplexityFiller.class);

	private static final List<String> skipTypes = Arrays.asList("EndOfInput", "NewLine");

	private static final List<String> operandTypes = Arrays.asList("StringExpandable", "Variable", "SplattedVariable",
			"StringLiteral", "HereStringExpandable", "HereStringLiteral");

	@Override
	public void fill(final SensorContext context, final InputFile f, final Tokens tokens) {
		try {
			final List<String> uniqueOperands = new LinkedList<>();
			final List<String> uniqueOperators = new LinkedList<>();
			int totalOperands = 0;
			int totalOperators = 0;

			for (final Token token : tokens.getTokens()) {
				if (skipTypes.contains(token.getKind()) || token.getText() == null) {
					continue;
				}

				final String text = token.getText().toLowerCase();
				if (operandTypes.contains(token.getKind())) {
					totalOperands++;
					if (!uniqueOperands.contains(text)) {
						uniqueOperands.add(text);
					}
					continue;
				}
				totalOperators++;
				if (!uniqueOperators.contains(text)) {
					uniqueOperators.add(text);
				}

			}
			int difficulty = (int) ((int) Math.ceil(uniqueOperators.size() / 2.0)
					* ((totalOperands * 1.0) / uniqueOperands.size()));
			synchronized (context) {
				context.<Integer>newMeasure().on(f).forMetric(CoreMetrics.COGNITIVE_COMPLEXITY).withValue(difficulty)
						.save();
			}

		} catch (final Throwable e) {
			LOGGER.warn("Exception while saving cognitive complexity metric", e);
		}

	}

}
