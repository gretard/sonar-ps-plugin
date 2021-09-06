package org.sonar.plugins.powershell;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

public class PowershellQualityProfileTest {

    @Test
    public void testDefine() {
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
        PowershellQualityProfile sut = new PowershellQualityProfile();
        sut.define(context);
        Assert.assertEquals(1, context.profilesByLanguageAndName().size());
    }

}
