package org.sonar.plugins.powershell.fillers;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.powershell.ast.Tokens;

public interface IFiller {
	void fill(final SensorContext context, final InputFile f, final Tokens tokens);
}
