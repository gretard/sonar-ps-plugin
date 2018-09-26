package org.sonar.plugins.powershell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.utils.internal.JUnitTempFolder;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.sensors.ScriptAnalyzerSensor;

@RunWith(Parameterized.class)
public class ScriptAnalyzerSensorTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@org.junit.Rule
	public JUnitTempFolder temp = new JUnitTempFolder();

	private String file;

	private int count;

	@Parameters(name = "{0} cnt: {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {

				{ "/testFiles/test.ps1", 4 }, { "/testFiles/test2.ps1", 2 }, { "/testFiles/test3.ps1", 2 },
				{ "/testFiles/test4.ps1", 2 }

		});
	}

	public ScriptAnalyzerSensorTest(String file, int count) {
		this.file = file;
		this.count = count;

	}

	@Test
	public void testExecute() throws IOException {
		Loggers.get(ScriptAnalyzerSensor.class).setLevel(LoggerLevel.DEBUG);
		SensorContextTester ctxTester = SensorContextTester.create(folder.getRoot());
		ctxTester.settings().setProperty(Constants.PS_EXECUTABLE, "powershell.exe");
		File baseFile = folder.newFile("test.ps1");

		FileUtils.copyURLToFile(getClass().getResource(this.file), baseFile);
		DefaultInputFile ti = new TestInputFileBuilder("test", "test.ps1")
				.initMetadata(new String(Files.readAllBytes(baseFile.toPath()))).build();
		ctxTester.fileSystem().add(ti);

		ScriptAnalyzerSensor s = new ScriptAnalyzerSensor(temp);
		s.execute(ctxTester);
		for (org.sonar.api.batch.sensor.issue.Issue is : ctxTester.allIssues()) {
			System.out.println(is);
		}
		Assert.assertEquals(this.count, ctxTester.allIssues().size());

	}

}
