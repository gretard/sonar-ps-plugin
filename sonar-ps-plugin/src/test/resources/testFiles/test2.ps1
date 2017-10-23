# PowerShell example to find and sort Windows log files
Clear-Host
$Directory = "C:\Windows\"
$Phrase = "Error"
$Files = Get-Childitem $Directory -recurse -Include *.log `
-ErrorAction SilentlyContinue
$Files | Select-String $Phrase -ErrorAction SilentlyContinue `
| Group-Object filename | Sort-Object count -descending