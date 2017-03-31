package org.sonar.plugins.powershell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Context;
import org.sonar.api.utils.internal.JUnitTempFolder;

public class ScriptAnalyzerSensorTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@org.junit.Rule
	public JUnitTempFolder temp = new JUnitTempFolder();

	@Test
	public void testExecute() throws IOException {

		Context t = new RulesDefinition.Context();
		ScriptAnalyzerRulesDefinition def = new ScriptAnalyzerRulesDefinition();
		def.define(t);

		File baseFile = folder.newFile("test.ps1");

		FileUtils.copyURLToFile(getClass().getResource("/testFiles/test.ps1"), baseFile);

		DefaultFileSystem fs = new DefaultFileSystem(folder.getRoot());
		DefaultInputFile ti = new DefaultInputFile("test", "test.ps1");
		ti.initMetadata(new String(Files.readAllBytes(baseFile.toPath())));

		fs.add(ti);

		SensorContextTester ctxTester = SensorContextTester.create(folder.getRoot());
		ScriptAnalyzerSensor s = new ScriptAnalyzerSensor(fs, temp);
		s.execute(ctxTester);

	}

}
