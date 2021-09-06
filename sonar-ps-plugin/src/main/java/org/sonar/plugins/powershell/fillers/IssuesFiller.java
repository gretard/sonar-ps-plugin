package org.sonar.plugins.powershell.fillers;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.Constants;
import org.sonar.plugins.powershell.issues.PsIssue;

public class IssuesFiller {

    private static final Logger LOGGER = Loggers.get(IssuesFiller.class);

    public void fill(final SensorContext context, final File sourceDir, final List<PsIssue> issues) {
        final FileSystem fileSystem = context.fileSystem();
        final List<String> skipRulesTemp = Arrays.asList(
                context.config().getStringArray(org.sonar.plugins.powershell.Constants.EXTERNAL_RULES_SKIP_LIST));
        final List<String> skipRules = new LinkedList<>();
        skipRulesTemp.forEach(s -> skipRules.add(s.toLowerCase()));
        for (final PsIssue issue : issues) {
            try {
                final String ruleName = issue.ruleId;
                final String repoKey = Constants.REPO_KEY;
                final String uniqueId = repoKey + ":" + ruleName;
                if (skipRules.contains(uniqueId.toLowerCase())) {
                    continue;
                }
                final String initialFile = issue.file;

                // skip reporting temp files
                if (initialFile.contains(".scannerwork")) {
                    continue;
                }
                final String fsFile = new PathResolver().relativePath(sourceDir, new File(initialFile));

                final String message = issue.message;
                int issueLine = issue.line;

                final RuleKey ruleKey = RuleKey.of(repoKey, ruleName);

                final org.sonar.api.batch.fs.InputFile file = fileSystem
                        .inputFile(fileSystem.predicates().and(fileSystem.predicates().hasRelativePath(fsFile)));

                if (file == null) {
                    LOGGER.debug(String.format("File '%s' not found in system to add issue %s", initialFile, ruleKey));
                    continue;
                }
                final NewIssue newIssue = context.newIssue().forRule(ruleKey);
                final NewIssueLocation loc = newIssue.newLocation().message(message).on(file);
                if (issueLine > 0) {
                    loc.at(file.selectLine(issueLine));
                }
                newIssue.at(loc);
                newIssue.save();
            } catch (final Throwable e) {
                LOGGER.warn("Unexpected exception while adding issue", e);
            }

        }
    }

}
