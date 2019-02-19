package org.sonar.plugins.powershell.fillers;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.ast.Tokens;
import org.sonar.plugins.powershell.ast.Tokens.Token;

public class CommentLinesFiller implements IFiller {

	private static final Logger LOGGER = Loggers.get(CommentLinesFiller.class);

	private static final List<String> skipTypes = Arrays.asList("EndOfInput", "NewLine");

	private static final int COMMENT = 1;
	private static final int CODE = 2;

	@Override
	public void fill(final SensorContext context, final InputFile f, final Tokens tokens) {
		try {

			final long[] lines = new long[f.lines() + 1];

			for (final Token token : tokens.getToken()) {

				if (skipTypes.contains(token.getKind()) || token.getText() == null) {
					continue;
				}

				if ("Comment".equalsIgnoreCase(token.getKind())) {
					for (int i = token.getStartLineNumber(); i <= token.getEndLineNumber(); i++) {
						lines[i] |= COMMENT;
					}
				} else {
					for (int i = token.getStartLineNumber(); i <= token.getEndLineNumber(); i++) {
						lines[i] |= CODE;
					}
				}
			}

			int commentLineCount = 0;
			int nonCommentLineCount = 0;

			for (int i = 0; i < lines.length; i++) {
				if ((lines[i] == COMMENT)) {
					commentLineCount++;
					continue;
				}
				if ((lines[i] & CODE) == CODE) {
					nonCommentLineCount++;
				}
			}

			context.<Integer>newMeasure().on(f).forMetric(CoreMetrics.COMMENT_LINES).withValue(commentLineCount).save();
			context.<Integer>newMeasure().on(f).forMetric(CoreMetrics.NCLOC).withValue(nonCommentLineCount).save();
		} catch (final Throwable e) {
			LOGGER.warn("Exception while calculating comment lines ", e);
		}

	}

}
