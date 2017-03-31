package org.sonar.plugins.powershell;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Context;
import org.sonar.plugins.powershell.PowershellLanguage;
import org.sonar.plugins.powershell.ScriptAnalyzerRulesDefinition;

public class ScriptAnalyzerRulesDefinitionTest {

	@Test
	public void testDefine() {
		Context t = new RulesDefinition.Context();
		ScriptAnalyzerRulesDefinition def = new ScriptAnalyzerRulesDefinition();
		def.define(t);
		Assert.assertEquals(1, t.repository(PowershellLanguage.KEY).rules().size());
	}
}
