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
				highlightToken(highlithing, token);
			}
			highlithing.save();
		} catch (Throwable e) {
			LOGGER.warn("Exception while running highliting", e);
		}
	}

	private static void highlightToken(final NewHighlighting highlithing, final Token token) {
		try {
		final List<String> kinds = Arrays.asList(token.getTokenFlags().toLowerCase().split(","));

		if (check("comment", token, kinds)) {
			highlithing.highlight(token.getStartOffset(), token.getEndOffset(), TypeOfText.COMMENT);
			return;
		}
/*		if (check("CommandName", token, kinds)) {
			highlithing.highlight(token.getStartOffset(), token.getEndOffset(), TypeOfText.KEYWORD_LIGHT);
			continue;
		}*/

		if (check("keyword", token, kinds)) {
			highlithing.highlight(token.getStartOffset(), token.getEndOffset(), TypeOfText.KEYWORD);
			return;
		}
		if (check("StringLiteral", token, kinds) || check("StringExpandable", token, kinds)) {
			highlithing.highlight(token.getStartOffset(), token.getEndOffset(), TypeOfText.STRING);
			return;
		}
		if (check("Variable", token, kinds)) {
			highlithing.highlight(token.getStartOffset(), token.getEndOffset(), TypeOfText.KEYWORD_LIGHT);
			return;
		}
		
		}
		catch (Throwable e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.warn("Exception while adding highliting for: "+token, e);
			}
		}
	}

	private static boolean check(final String txt, final Token token, final List<String> kinds) {
		return txt.equalsIgnoreCase(token.getKind()) || kinds.contains(txt.toLowerCase());
	}
}
