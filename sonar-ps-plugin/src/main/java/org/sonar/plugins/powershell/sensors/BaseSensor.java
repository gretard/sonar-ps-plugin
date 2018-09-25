package org.sonar.plugins.powershell.sensors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BaseSensor {
	protected static String read(Process process) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		StringBuilder builder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append(System.getProperty("line.separator"));
		}
		String result = builder.toString();
		return result;
	}
}
