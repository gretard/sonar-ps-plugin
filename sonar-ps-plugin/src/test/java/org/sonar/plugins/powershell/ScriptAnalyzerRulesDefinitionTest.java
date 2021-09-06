package org.sonar.plugins.powershell;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Context;

public class ScriptAnalyzerRulesDefinitionTest {

    @Test
    public void testDefine() {
        Context context = new RulesDefinition.Context();
        ScriptAnalyzerRulesDefinition sut = new ScriptAnalyzerRulesDefinition();
        sut.define(context);
        Assert.assertEquals(65, context.repository(Constants.REPO_KEY).rules().size());
    }
}
