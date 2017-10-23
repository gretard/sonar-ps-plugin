param( 
[string]$inputName,
 [string]$output,
 [int] $depth = 9999999 )

$text = Get-Content -Path "$inputName" -Raw
$tokens = $null
$errors = $null
$ast = [Management.Automation.Language.Parser]::ParseInput($text , [ref]$tokens, [ref]$errors);




   $xmlWriter = New-Object System.XMl.XmlTextWriter($output , $Null);
    
    
    
    # Write the XML Declaration
    $xmlWriter.WriteStartDocument();

	       # Start Issues XML Element
    $xmlWriter.WriteStartElement("Tokens");
   foreach ($item in $tokens) {
	
		$xmlWriter.WriteStartElement("Token");
		
			$xmlWriter.WriteElementString("Text", $item.Text);
	$xmlWriter.WriteElementString("Value", $item.Value);
		$xmlWriter.WriteElementString("TokenFlags", $item.TokenFlags);
				$xmlWriter.WriteElementString("Kind", [System.Management.Automation.Language.TokenKind]::GetName([System.Management.Automation.Language.TokenKind], $item.Kind.value__));
				$xmlWriter.WriteElementString("StartLineNumber", $item.Extent.StartLineNumber);
						$xmlWriter.WriteElementString("CType", $item.GetType().Name);
								$xmlWriter.WriteElementString("EndLineNumber", $item.Extent.EndLineNumber);
										$xmlWriter.WriteElementString("StartOffset", $item.Extent.StartOffset);
	$xmlWriter.WriteElementString("EndOffset", $item.Extent.EndOffset);
		$xmlWriter.WriteElementString("StartColumnNumber", $item.Extent.StartColumnNumber);
				$xmlWriter.WriteElementString("EndColumnNumber", $item.Extent.EndColumnNumber);
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
