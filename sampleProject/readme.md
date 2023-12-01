Sample scripts from: https://adamtheautomator.com/powershell-script-examples/ 

# Getting started

- Install sonar-ps plugin into SonarQube by copying jar from https://github.com/gretard/sonar-ps-plugin/releases into sonar ./downloads folder
- Restart sonar server
- Download scanner from https://docs.sonarsource.com/sonarqube/9.9/analyzing-source-code/scanners/sonarscanner/
- Execute : ```sonar-scanner.bat -D"sonar.login=admin" -D"sonar.password=<<PASSWORD>>"```