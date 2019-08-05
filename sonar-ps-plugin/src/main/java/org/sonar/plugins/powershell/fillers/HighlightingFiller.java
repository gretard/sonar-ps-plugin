package org.sonar.plugins.powershell.fillers;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.ast.Tokens;
import org.sonar.plugins.powershell.ast.Tokens.Token;

public class HighlightingFiller implements IFiller {

	private static final Logger LOGGER = Loggers.get(HighlightingFiller.class);

	public void fill(final SensorContext context, final InputFile f, final Tokens tokens) {

		try {
			final NewHighlighting highlighting = context.newHighlighting().onFile(f);
			for (final Token token : tokens.getTokens()) {
				highlightToken(highlighting, token);
			}
			synchronized (context) {
				highlighting.save();
			}

		} catch (Throwable e) {
			LOGGER.warn("Exception while running highlighting", e);
		}
	}

	private static void highlightToken(final NewHighlighting highlighting, final Token token) {
		try {
			final List<String> kinds = Arrays.asList(token.getTokenFlags().toLowerCase().split(","));
			int startLine = token.getStartLineNumber();
			int startLineOffset = token.getStartColumnNumber() - 1;
			int endLine = token.getEndLineNumber();
			int endLineOffset = token.getEndColumnNumber() - 1;
			if (check("comment", token, kinds)) {
				highlighting.highlight(startLine, startLineOffset, endLine, endLineOffset, TypeOfText.COMMENT);
				return;
			}
			if (check("keyword", token, kinds)) {

				highlighting.highlight(startLine, startLineOffset, endLine, endLineOffset, TypeOfText.KEYWORD);
				return;
			}
			if (check("StringLiteral", token, kinds) || check("StringExpandable", token, kinds)) {
				highlighting.highlight(startLine, startLineOffset, endLine, endLineOffset, TypeOfText.STRING);
				return;
			}
			if (check("Variable", token, kinds)) {
				highlighting.highlight(startLine, startLineOffset, endLine, endLineOffset, TypeOfText.KEYWORD_LIGHT);
			}

		} catch (Throwable e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.warn("Exception while adding highlighting for: " + token, e);
			}
		}
	}

	private static boolean check(final String txt, final Token token, final List<String> kinds) {
		return txt.equalsIgnoreCase(token.getKind()) || kinds.contains(txt.toLowerCase());
	}
}
