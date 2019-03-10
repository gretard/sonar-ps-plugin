# sonar-ps-plugin
Repository for Powershell language plugin for Sonar

## Description ##
Currently plug-in supports:

- Reporting of issues found by [PSScriptAnalyzer](https://github.com/PowerShell/PSScriptAnalyzer)
- Cyclomatic and cognitive complexity metrics (since version 0.3.0)
- Reporting number of lines of code and comment lines metrics  (since version 0.3.2)

## Donating ##
You can support this [project and others](https://github.com/gretard) via [Paypal](https://www.paypal.me/greta514284/)

[![Support via PayPal](https://cdn.rawgit.com/twolfson/paypal-github-button/1.0.0/dist/button.svg)](https://www.paypal.me/greta514284/)

## Usage ##
1. Download and install SonarQube
2. Download plugin from the [releases](https://github.com/gretard/sonar-ps-plugin/releases) and copy it to sonarqube's extensions\plugins directory
3. Start SonarQube and enable rules
4. Install [PSScriptAnalyzer](https://github.com/PowerShell/PSScriptAnalyzer) into your build machine where you plan to run sonar scanner

## Configuration ##
Currently there is a possibility to override the following options either on server in the Administration tab or on the project configuration files:

- **sonar.ps.tokenizer.skip** - if set to true - skips tokenizer, which might be time consuming, defaults to *false*
- **sonar.ps.file.suffixes** - allows to specify which files should be detected as Powershell files, defaults to *.ps1,.psm1,.psd1*
- **sonar.ps.executable** - allows to specify powershell executable, defaults to *powershell.exe* (since version 0.3.0)
- **sonar.ps.plugin.skip** - if set to true - skips plugin in general, meaning that no sensors are run, defaults to *false* (since version 0.3.0)



## Requirements ##
Different plugin versions supports the following:

- 0.3.0 - Sonarqube version 6.3+ and PSScriptAnalyzer version 1.17.1 rules
- 0.2.2 - Sonarqube 5.6+ version and PSScriptAnalyzer version 1.17.1 rules

