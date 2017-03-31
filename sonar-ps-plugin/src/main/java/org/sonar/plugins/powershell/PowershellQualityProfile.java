package org.sonar.plugins.powershell;

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.ValidationMessages;

public class PowershellQualityProfile extends ProfileDefinition {

	public RulesProfile createProfile(final ValidationMessages validation) {
		return RulesProfile.create("Powershell Rules", PowershellLanguage.KEY);
	}
}