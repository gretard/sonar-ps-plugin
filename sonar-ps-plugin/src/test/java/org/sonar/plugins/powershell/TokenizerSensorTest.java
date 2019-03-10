package org.sonar.plugins.powershell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.powershell.sensors.TokenizerSensor;

public class TokenizerSensorTest extends BaseTest {

	@Test
	public void testIfFileIsSkipped() throws IOException {
		final String key = ".scannerwork.ps1";
		SensorContextTester ctxTester = SensorContextTester.create(folder.getRoot().getAbsoluteFile().toPath());
		ctxTester.settings().setProperty(Constants.PS_EXECUTABLE, getDefaultPowerShellExecutable());
		File baseFile = folder.newFile(key);

		FileUtils.copyURLToFile(getClass().getResource("/testFiles/test.ps1"), baseFile);
		DefaultInputFile ti = new TestInputFileBuilder(folder.getRoot().getAbsolutePath(), key)
				.initMetadata(new String(Files.readAllBytes(baseFile.toPath()))).setLanguage(PowershellLanguage.KEY)
				.build();

		ctxTester.fileSystem().add(ti);
		final TokenizerSensor sut = new TokenizerSensor(temp);
		sut.execute(ctxTester);

		Assert.assertNull(ctxTester.cpdTokens(ti.key()));
		Assert.assertTrue(ctxTester.measures(ti.key()).isEmpty());
		Assert.assertTrue(ctxTester.highlightingTypeAt(ti.key(), 1, 30).isEmpty());

	}

}
