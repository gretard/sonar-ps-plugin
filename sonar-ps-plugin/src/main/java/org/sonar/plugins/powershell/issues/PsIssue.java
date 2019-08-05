package org.sonar.plugins.powershell.issues;

public class PsIssue {
	public String ruleId;
	public String message;
	public int line = 0;
	public String severity = "MAJOR";
	public String file;
	
}
