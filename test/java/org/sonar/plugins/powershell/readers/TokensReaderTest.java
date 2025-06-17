package org.sonar.plugins.powershell.readers;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.impl.utils.JUnitTempFolder;
import org.sonar.plugins.powershell.ast.Tokens;
import org.sonar.plugins.powershell.ast.Tokens.Token;

public class TokensReaderTest {

    @Rule
    public JUnitTempFolder temp = new JUnitTempFolder();

    private final TokensReader sut = new TokensReader();

    @Test
    public void testReadMultipleTokens() throws Throwable {
        File file = temp.newFile("main", "results.xml");
        FileUtils.copyURLToFile(getClass().getResource("/results/tokens.xml"), file);
        Tokens tokens = sut.read(file);
        Assert.assertEquals(76, tokens.getTokens().size());

    }

    @Test
    public void testReadMapping() throws Throwable {
        File file = temp.newFile("main", "results.xml");

        FileUtils.copyURLToFile(getClass().getResource("/results/tokensSingle.xml"), file);
        Tokens tokens = sut.read(file);
        Assert.assertEquals(1, tokens.getTokens().size());
        Assert.assertEquals(10, tokens.getComplexity());
        Token token = tokens.getTokens().get(0);
        Assert.assertEquals("Token", token.getcType());
        Assert.assertEquals(32, token.getEndColumnNumber());
        Assert.assertEquals(2, token.getEndLineNumber());
        Assert.assertEquals(31, token.getEndOffset());
        Assert.assertEquals("Comment", token.getKind());
        Assert.assertEquals(4, token.getStartColumnNumber());
        Assert.assertEquals(1, token.getStartLineNumber());
        Assert.assertEquals("#Import-Module PSScriptAnalyzer", token.getText());
        Assert.assertEquals("ParseModeInvariant", token.getTokenFlags());
        Assert.assertEquals("", token.getValue());

    }
}
