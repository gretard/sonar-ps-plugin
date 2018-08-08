package org.sonar.plugins.powershell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.utils.internal.JUnitTempFolder;

public class ScriptAnalyzerSensorTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@org.junit.Rule
	public JUnitTempFolder temp = new JUnitTempFolder();

	@Test
	public void testExecute() throws IOException {

	
		SensorContextTester ctxTester = SensorContextTester.create(folder.getRoot());
		ctxTester.settings().setProperty(Constants.PS_EXECUTABLE, "powershell.exe");
		File baseFile = folder.newFile("test.ps1");
		FileUtils.copyURLToFile(getClass().getResource("/testFiles/test.ps1"), baseFile);
		DefaultInputFile ti = new TestInputFileBuilder("test", "test.ps1")
				.initMetadata(new String(Files.readAllBytes(baseFile.toPath()))).build();
		ctxTester.fileSystem().add(ti);

		ScriptAnalyzerSensor s = new ScriptAnalyzerSensor(temp);
		s.execute(ctxTester);
		
		Assert.assertEquals(4, ctxTester.allIssues().size());

	}

}
