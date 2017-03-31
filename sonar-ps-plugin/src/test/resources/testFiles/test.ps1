#Import-Module PSScriptAnalyzer
foreach ($rule in Get-ScriptAnalyzerRule) {
	write-output "<rule>"
		write-output "<key>"$rule.RuleName"</key>"
		write-output "<description><![CDATA["$rule.Description"]]></description>"
		write-output "<name>"$rule.RuleName"</name>"
		write-output "<severity>"$rule.Severity"</severity>"
		write-output "<debt>5min</debt>"
			write-output "<tags></tags>"
				write-output "<debt2></debt2>"
		write-output "<debtCalcType>constant</debtCalcType>"
				write-output "<status>BETA</status>"
								write-output "<debtType>ARCHITECTURE_RELIABILITY</debtType>"
								write-output "<url></url>"
	write-output "</rule>"

}

