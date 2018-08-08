package org.sonar.plugins.powershell.fillers;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.ast.Tokens;
import org.sonar.plugins.powershell.ast.Tokens.Token;

public class CpdFiller implements IFiller {
	private static final Logger LOGGER = Loggers.get(CpdFiller.class);

	private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

	@Override
	public void fill(SensorContext context, InputFile f, Tokens tokens) {
		try {
			final NewCpdTokens cpdTokens = context.newCpdTokens().onFile(f);

			for (final Token token : tokens.getToken()) {
				if (StringUtils.isBlank(token.getText())) {
					continue;
				}
				tryAddToken(cpdTokens, token);
			}

			cpdTokens.save();
		} catch (final Throwable e) {
			LOGGER.warn("Exception while saving tokens", e);
		}

	}

	private static void tryAddToken(final NewCpdTokens cpdTokens, final Token token) {
		try {
			cpdTokens.addToken(token.getStartLineNumber(), token.getStartColumnNumber(),
					token.getEndLineNumber(), token.getEndColumnNumber(), token.getText());
		} catch (final Throwable e) {
			if (isDebugEnabled) {
				LOGGER.debug("Exception while adding token", e);
			}
		}
	}

}
