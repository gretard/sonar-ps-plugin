$reportFile = Join-path $PSScriptRoot "powershell-rules.xml"

    # Create The Document
    $xmlWriter = New-Object System.XMl.XmlTextWriter($reportFile , $Null);
    
    # Set The Formatting
    $xmlWriter.Formatting = "Indented"
    $xmlWriter.Indentation = "4"
    
    # Write the XML Declaration
    $xmlWriter.WriteStartDocument();
    
    # Start Issues XML Element
    $xmlWriter.WriteStartElement("psrules");

    $powershellRules = Get-ScriptAnalyzerRule;

    foreach ($rule in $powershellRules) 
    { 

     $xmlWriter.WriteStartElement("rule");
            $xmlWriter.WriteElementString("key", $rule.RuleName)
                        $xmlWriter.WriteElementString("internalKey", $rule.RuleName)
         $xmlWriter.WriteElementString("name", $rule.CommonName)
         $xmlWriter.WriteElementString("description", $rule.Description)
           $xmlWriter.WriteElementString("cardinality", "SINGLE")
            $xmlWriter.WriteElementString("remediationFunction", "LINEAR")
            $xmlWriter.WriteElementString("remediationFunctionBaseEffort", "")
         $remediationDefaultTime = "2min";
         $severity = "INFO";
        
         if ($rule.Severity -eq "Information") {
         $severity = "INFO";
         $remediationDefaultTime = "2min";
         }
         if ($rule.Severity -eq "Warning") {
         $severity = "MAJOR";
         $remediationDefaultTime = "5min";
         }
         if ($rule.Severity -eq "ERROR") {
         $severity = "BLOCKER";
         $remediationDefaultTime = "15min";
         }
            $xmlWriter.WriteElementString("debtRemediationFunctionCoefficient", $remediationDefaultTime);
            $xmlWriter.WriteElementString("severity", $severity);
          $xmlWriter.WriteEndElement();
    }

    # End Issues XML element
    $xmlWriter.WriteEndElement();
    
    # End the XML Document
    $xmlWriter.WriteEndDocument();
    
    # Finish The Document
    $xmlWriter.Finalize
    $xmlWriter.Flush
    $xmlWriter.Close();
