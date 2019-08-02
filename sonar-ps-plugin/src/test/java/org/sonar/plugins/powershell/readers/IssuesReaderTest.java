package org.sonar.plugins.powershell.readers;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.utils.internal.JUnitTempFolder;
import org.sonar.plugins.powershell.issues.PsIssue;

public class IssuesReaderTest {
	@org.junit.Rule
	public JUnitTempFolder temp = new JUnitTempFolder();

	private final IssuesReader sut = new IssuesReader();

	@Test
	public void testReadMultipleIssues() throws Throwable {
		File file = temp.newFile("main", "results.xml");

		FileUtils.copyURLToFile(getClass().getResource("/results/psanalyzer.xml"), file);
		List<PsIssue> issues = sut.read(file);
		Assert.assertEquals(4, issues.size());

	}
	
	@Test
	public void testReadIssuesMapping() throws Throwable {
		File file = temp.newFile("main", "results.xml");

		FileUtils.copyURLToFile(getClass().getResource("/results/psanalyzerSingle.xml"), file);
		List<PsIssue> issues = sut.read(file);
		Assert.assertEquals(1, issues.size());
		PsIssue issue = issues.get(0);
		Assert.assertEquals("C:\\test.ps1", issue.file);
		Assert.assertEquals(5, issue.line);
		Assert.assertEquals("Message", issue.message);
		Assert.assertEquals("PSAvoidUsingPositionalParameters", issue.ruleId);
		Assert.assertEquals("Information", issue.severity);

	}

}
