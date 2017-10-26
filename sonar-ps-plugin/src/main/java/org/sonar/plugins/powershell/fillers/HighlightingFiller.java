package org.sonar.plugins.powershell.fillers;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.ast.Token;
import org.sonar.plugins.powershell.ast.Tokens;

public class HighlightingFiller implements IFiller {
	private static final Logger LOGGER = Loggers.get(HighlightingFiller.class);

	public void fill(final SensorContext context, final InputFile f, final Tokens tokens) {

		try {
			final NewHighlighting highlithing = context.newHighlighting().onFile(f);

			for (final Token token : tokens.getToken()) {

				if ("StringExpandableToken".equals(token.getCType())

				) {
					highlithing.highlight(token.getStartOffset(), token.getEndOffset(), TypeOfText.STRING);
				}
				if ("VariableToken".equals(token.getCType())) {
					highlithing.highlight(token.getStartOffset(), token.getEndOffset(), TypeOfText.KEYWORD_LIGHT);

				}
				if ("StringLiteralToken".equals(token.getCType())) {
					highlithing.highlight(token.getStartOffset(), token.getEndOffset(), TypeOfText.KEYWORD);

				}
				if ("Comment".equals(token.getCType())) {
					highlithing.highlight(token.getStartOffset(), token.getEndOffset(), TypeOfText.COMMENT);

				}

			}
			highlithing.save();
		} catch (Throwable e) {
			LOGGER.warn("Exception while running highliting", e);
		}
	}
}
