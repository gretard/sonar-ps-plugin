# sonar-ps-plugin
Repository for Powershell language plugin for Sonar

## Description ##
Currently plug-in supports:

- Rules by [PSScriptAnalyser](https://github.com/PowerShell/PSScriptAnalyzer)

## Usage ##
1. Download and install SonarQube
2. Download plugin from the [releases](https://github.com/gretard/sonar-ps-plugin/releases) and copy it to sonarqube's extensions\plugins directory
3. Start SonarQube and enable rules
4. Install [PSScriptAnalyser](https://github.com/PowerShell/PSScriptAnalyzer) into your build machine where you plan to run sonar scanner


## Configuration ##

Currently there is a possibility to override the following options either on server in the Administration tab or on the project configuration files:

- **sonar.ps.tokenizer.skip** - if set to true - skips tokenizer, which might be time consuming, defaults to *false*
- **sonar.ps.file.suffixes** - allows to specify which files should be detected as Powershell files, defaults to *.ps1,.psm1,.psd1*