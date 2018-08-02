package org.sonar.plugins.powershell.fillers;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.ast.Token;
import org.sonar.plugins.powershell.ast.Tokens;

public class CpdFiller implements IFiller {
	private static final Logger LOGGER = Loggers.get(CpdFiller.class);
	private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

	@Override
	public void fill(SensorContext context, InputFile f, Tokens tokens) {
		try {
			final NewCpdTokens highlithing = context.newCpdTokens().onFile(f);

			for (final Token token : tokens.getToken()) {
				if (StringUtils.isBlank(token.getText())) {
					continue;
				}
				try {
					highlithing.addToken(token.getStartLineNumber(), token.getStartColumnNumber(),
							token.getEndLineNumber(), token.getEndColumnNumber(), token.getText());
				} catch (final Throwable e) {
					if (isDebugEnabled) {
						LOGGER.debug("Exception while adding token", e);
					}
				}
			}
			highlithing.save();
		} catch (final Throwable e) {
			LOGGER.warn("Exception while saving tokens", e);
		}

	}

}
