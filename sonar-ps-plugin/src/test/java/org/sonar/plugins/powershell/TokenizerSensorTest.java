package org.sonar.plugins.powershell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.internal.JUnitTempFolder;

public class TokenizerSensorTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@org.junit.Rule
	public JUnitTempFolder temp = new JUnitTempFolder();

	@Test
	public void testExecute() throws IOException, InterruptedException {

		File baseFile = folder.newFile("test.ps1");

		FileUtils.copyURLToFile(getClass().getResource("/testFiles/test.ps1"), baseFile);

		DefaultFileSystem fs = new DefaultFileSystem(folder.getRoot());
		DefaultInputFile ti = new DefaultInputFile("test", "test.ps1");
		ti.initMetadata(new String(Files.readAllBytes(baseFile.toPath())));
		ti.setLanguage(PowershellLanguage.KEY);
		fs.add(ti);

		SensorContextTester ctxTester = SensorContextTester.create(folder.getRoot());
		ctxTester.setFileSystem(fs);
		TokenizerSensor s = new TokenizerSensor(new Settings(), temp);
		s.execute(ctxTester);
		Assert.assertEquals(16, ctxTester.cpdTokens("test:test.ps1").size());
		Assert.assertEquals(1, ctxTester.highlightingTypeAt("test:test.ps1", 1, 5).size());

	}

}
