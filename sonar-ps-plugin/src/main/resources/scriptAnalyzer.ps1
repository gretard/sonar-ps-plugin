param( 
[string]$inputDir,
[string]$output
)
Import-Module PSScriptAnalyzer;
(Invoke-ScriptAnalyzer -Path "$inputDir" -Recurse | Select-Object RuleName, Message, Line, Column, Severity, @{Name='File';Expression={$_.Extent.File }} | ConvertTo-Xml).Save("$output")