package org.sonar.plugins.powershell.readers;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.sonar.api.internal.apachecommons.io.input.BOMInputStream;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.ast.Tokens;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TokensReader {
	private static final Logger LOGGER = Loggers.get(TokensReader.class);

	public Tokens read(File file) throws Throwable {
		Tokens tokens = new Tokens();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new BOMInputStream(new FileInputStream(file)));
		tokens.setComplexity(Integer.parseInt(doc.getDocumentElement().getAttribute("complexity")));
		NodeList list = doc.getElementsByTagName("Token");
		for (int i = 0; i < list.getLength(); i++) {
			try {
				Node node = list.item(i);
				Tokens.Token token = new Tokens.Token();
				token.setText(getChildByName(node, "Text").getTextContent());
				token.setValue(getChildByName(node, "Value").getTextContent());
				token.setTokenFlags(getChildByName(node, "TokenFlags").getTextContent());
				token.setKind(getChildByName(node, "Kind").getTextContent());
				token.setCType(getChildByName(node, "CType").getTextContent());

				token.setStartLineNumber(Integer.parseInt(getChildByName(node, "StartLineNumber").getTextContent()));
				token.setEndLineNumber(Integer.parseInt(getChildByName(node, "EndLineNumber").getTextContent()));
				token.setStartOffset(Integer.parseInt(getChildByName(node, "StartOffset").getTextContent()));
				token.setEndColumnNumber(Integer.parseInt(getChildByName(node, "EndOffset").getTextContent()));
				token.setStartColumnNumber(
						Integer.parseInt(getChildByName(node, "StartColumnNumber").getTextContent()));
				token.setEndColumnNumber(Integer.parseInt(getChildByName(node, "EndColumnNumber").getTextContent()));
				tokens.getToken().add(token);
			} catch (Exception e) {
				LOGGER.warn("Unexpected error reading results", e);
			}
		}
		return tokens;
	}

	public Node getChildByName(Node root, String name) throws Exception {
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (name.equalsIgnoreCase(child.getNodeName())) {
				return child;
			}

		}
		throw new Exception("Child node with name " + name + " was not found");
	}
}
