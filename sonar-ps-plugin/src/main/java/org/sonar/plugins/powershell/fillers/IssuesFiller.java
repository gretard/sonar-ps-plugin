package org.sonar.plugins.powershell.fillers;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.ScriptAnalyzerRulesDefinition;
import org.sonar.plugins.powershell.issues.Objects;
import org.sonar.plugins.powershell.issues.Objects.Object.Property;

public class IssuesFiller {

	private static final Logger LOGGER = Loggers.get(IssuesFiller.class);

	public void fill(final SensorContext context, final File sourceDir, final Objects issues) {
		final FileSystem fileSystem = context.fileSystem();
		for (final Objects.Object o : issues.getObject()) {
			try {
				final List<Objects.Object.Property> props = o.getProperty();
				final String ruleName = getProperty("RuleName", props);
				final String initialFile = getProperty("File", props);
				final String fsFile = new PathResolver().relativePath(sourceDir, new File(initialFile));
				final String message = getProperty("Message", props);
				final String line = getProperty("Line", props);
				int issueLine = getLine(line);

				final RuleKey ruleKey = RuleKey.of(ScriptAnalyzerRulesDefinition.getRepositoryKeyForLanguage(),
						ruleName);
				final org.sonar.api.batch.fs.InputFile file = fileSystem
						.inputFile(fileSystem.predicates().and(fileSystem.predicates().hasRelativePath(fsFile)));

				if (file == null) {
					LOGGER.warn(String.format("File %s not found", fsFile));
					continue;
				}
				final NewIssue issue = context.newIssue().forRule(ruleKey);
				final NewIssueLocation loc = issue.newLocation().message(message).on(file);
				if (issueLine > 0) {
					loc.at(file.selectLine(issueLine));
				}
				issue.at(loc);
				issue.save();
			} catch (final Throwable e) {
				LOGGER.warn("Unexpected exception while adding issue", e);
			}

		}
	}

	private int getLine(final String line) {
		int issueLine = -1;
		if (StringUtils.isNotEmpty(line)) {
			try {
				issueLine = Integer.parseInt(line);
			} catch (Throwable e) {
				LOGGER.debug(String.format("Was not able to parse line: '%s'", line));
			}
		}
		return issueLine;
	}

	private static String getProperty(final String key, final List<Objects.Object.Property> props) {
		for (final Property p : props) {
			if (key.equalsIgnoreCase(p.getName())) {
				return p.getValue();
			}
		}
		return "";
	}
}
