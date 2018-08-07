package org.sonar.plugins.powershell.fillers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.MetricFinder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.batch.sensor.measure.NewMeasure;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.ast.Token;
import org.sonar.plugins.powershell.ast.Tokens;

public class HalsteadComplexityFiller implements IFiller {
	private static final Logger LOGGER = Loggers.get(HalsteadComplexityFiller.class);
	
	private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
List<String> skipTypes = Arrays.asList("EndOfInput", "NewLine");

List<String> operandTypes = Arrays.asList("StringExpandable", "Variable", "SplattedVariable", "StringLiteral", "HereStringExpandable","HereStringLiteral" );
	@Override
	public void fill(SensorContext context, InputFile f, Tokens tokens) {
		try {
			List<String> uniqueOperands = new LinkedList<String>();
			List<String> uniqueOperators = new LinkedList<String>();
			int totalOperands = 0;
			int totalOperators = 0;
			
		
			
			for (final Token token : tokens.getToken()) {
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
			int difficulty =  (int) ((int)Math.ceil(uniqueOperators.size() / 2.0) * ((totalOperands * 1.0)/ uniqueOperands.size()));
			
			context.<Integer>newMeasure().on(f).forMetric(CoreMetrics.COGNITIVE_COMPLEXITY).withValue(difficulty)
			.save();
			
		} catch (final Throwable e) {
			LOGGER.warn("Exception while saving tokens", e);
		}

	}
	
}
