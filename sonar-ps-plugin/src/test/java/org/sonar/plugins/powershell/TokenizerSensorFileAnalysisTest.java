package org.sonar.plugins.powershell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.powershell.sensors.TokenizerSensor;

@RunWith(Parameterized.class)
public class TokenizerSensorFileAnalysisTest extends BaseTest {

	private final String key;

	public TokenizerSensorFileAnalysisTest(String key) {
		this.key = key;
	}

	@Parameters(name = "{index}: file: {0}")
	public static Iterable<? extends Object> data() {
		return Arrays.asList("test.ps1", "te st.ps1", ".test.ps1");
	}

	@Test
	public void testExecute() throws IOException {
		SensorContextTester ctxTester = SensorContextTester.create(folder.getRoot().getAbsoluteFile().toPath());
		ctxTester.settings().setProperty(Constants.PS_EXECUTABLE, getPowerShellExecutable());

		File baseFile = folder.newFile(key);
		FileUtils.copyURLToFile(getClass().getResource("/testFiles/test.ps1"), baseFile);
		DefaultInputFile ti = new TestInputFileBuilder(folder.getRoot().getAbsolutePath(), key)
				.initMetadata(new String(Files.readAllBytes(baseFile.toPath()))).setLanguage(PowershellLanguage.KEY)
				.build();

		ctxTester.fileSystem().add(ti);
		final TokenizerSensor sut = new TokenizerSensor(temp);
		sut.execute(ctxTester);

		Assert.assertEquals(18, ctxTester.cpdTokens(ti.key()).size());
		Assert.assertEquals(4, ctxTester.measures(ti.key()).size());
		Assert.assertEquals(1, ctxTester.highlightingTypeAt(ti.key(), 1, 30).size());

	}

}
