package org.sonar.plugins.powershell.readers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.impl.utils.JUnitTempFolder;
import org.sonar.plugins.powershell.issues.PsIssue;

public class IssuesReaderTest {
    @org.junit.Rule
    public JUnitTempFolder temp = new JUnitTempFolder();

    private final IssuesReader sut = new IssuesReader();

    @Test
    public void testReadMultipleIssues() throws Throwable {
        File file = temp.newFile("main", "results.xml");
        FileUtils.copyToFile(getFileFromResourceAsStream("./results/psanalyzer.xml"), file);

        List<PsIssue> issues = sut.read(file);
        Assert.assertEquals(4, issues.size());

    }

    @Test
    public void testReadIssuesMapping() throws Throwable {
        File file = temp.newFile("main", "results.xml");

        ClassLoader classLoader = getClass().getClassLoader();

        File file0 = new File(classLoader.getResource("./results/psanalyzerSingle.xml").getFile());

        FileUtils.copyFile(file0, file);
        System.out.println(FileUtils.readFileToString(file));
        List<PsIssue> issues = sut.read(file);
        Assert.assertEquals(1, issues.size());
        PsIssue issue = issues.get(0);
        Assert.assertEquals("C:\\test.ps1", issue.file);
        Assert.assertEquals(5, issue.line);
        Assert.assertEquals("Message", issue.message);
        Assert.assertEquals("PSAvoidUsingPositionalParameters", issue.ruleId);
        Assert.assertEquals("Information", issue.severity);

    }

    private InputStream getFileFromResourceAsStream(String fileName) throws FileNotFoundException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {

            return new FileInputStream(new File(resource.toURI()));
        }

    }

}
