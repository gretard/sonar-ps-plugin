package org.sonar.plugins.powershell;

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.profiles.XMLProfileParser;
import org.sonar.api.utils.ValidationMessages;

public class PowershellQualityProfile extends ProfileDefinition {
	private XMLProfileParser xmlProfileParser;

	public PowershellQualityProfile(XMLProfileParser xmlProfileParser) {
		this.xmlProfileParser = xmlProfileParser;
	}

	public RulesProfile createProfile(final ValidationMessages validation) {
		return xmlProfileParser.parseResource(getClass().getClassLoader(), "powershell-profile.xml", validation)
				.setName(Constants.PROFILE_NAME).setLanguage(PowershellLanguage.KEY);
	}
}