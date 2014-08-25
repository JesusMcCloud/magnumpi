/*******************************************************************************
 * Copyright 2013 Alexander Jesner, Bernd Prünster
 * Copyright 2013, 2014 Bernd Prünster
 *
 *     This file is part of Magnum PI.
 *
 *     Magnum PI is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Magnum PI is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Magnum PI.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package at.tugraz.iaik.magnum.client.util;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Stack;

import javax.activation.UnsupportedDataTypeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import at.tugraz.iaik.magnum.client.util.datatypes.BlackWhiteListContainer;

public class BWListParser {

  private static final String DOM_ROOT = "bwlist";
  private static final String NAME     = "name";
  private static final String PREFIX   = "prefix";
  private static final String TYPE     = "type";

  private enum Type {
    PACKAGE,
    CLASS,
    METHOD,
    PARAM,
    CONSTRUCTOR;

    public static Type value(String str) {
      return valueOf(str.toUpperCase());
    }
  };

  private Document loadXMLFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setIgnoringElementContentWhitespace(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    // This is ugly, but the java DOM API is really extremely bad
    xml = xml.replaceAll("\\s*<", "<").replaceAll(">\\s*", ">");
    System.out.println(xml);
    InputSource is = new InputSource(new StringReader(xml));
    return builder.parse(is);
  }

  public BlackWhiteListContainer parse(String str) throws Exception {
    return parseDOM(loadXMLFromString(str));
  }

  private BlackWhiteListContainer parseDOM(Document dom) throws UnsupportedDataTypeException {
    BlackWhiteListContainer container = new BlackWhiteListContainer();
    Node root = dom.getChildNodes().item(0);
    if (!root.getNodeName().equalsIgnoreCase(DOM_ROOT))
      throw new UnsupportedDataTypeException("XML root element mismatch: " + root.getNodeName());

    container.packages = new HashSet<String>();
    container.packageWildcards = new HashSet<String>();
    container.classes = new HashSet<String>();
    container.classesWildcards = new HashSet<String>();
    container.methods = new HashSet<String>();
    container.methodWildcards = new HashSet<String>();
    NodeList nodes = root.getChildNodes();
    for (int i = 0; i < nodes.getLength(); ++i)
      parse(nodes.item(i), new Stack<Node>(), container);

    container.print();
    return container;
  }

  private StringBuilder joinParts(Stack<Node> parts) {
    StringBuilder bld = new StringBuilder();
    boolean first = true;
    for (Node n : parts) {
      if (!first)
        bld.append(".");
      first = false;
      NamedNodeMap attributes = n.getAttributes();
      String name = null;
      try {
        name = attributes.getNamedItem(NAME).getNodeValue();
      } catch (NullPointerException e) {
        name = "<init>";
      }
      bld.append(name);
    }
    return bld;
  }

  private boolean isPrefix(Stack<Node> parts) {
    for (Node n : parts) {
      if (n.hasAttributes() && (n.getAttributes().getNamedItem(PREFIX) != null)
          && (n.getAttributes().getNamedItem(PREFIX).getNodeValue().equalsIgnoreCase("true"))) {
        return true;
      }
    }
    return false;
  }

  private void parse(Node node, Stack<Node> parts, BlackWhiteListContainer container)
      throws UnsupportedDataTypeException {
    parts.push(node);
    Type nodeType = Type.value(node.getNodeName());

    if ((nodeType == Type.CONSTRUCTOR) || nodeType == Type.METHOD) {
      boolean prefix = isPrefix(parts);
      StringBuilder method = joinParts(parts);

      if (!prefix) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
          Node param = children.item(i);
          Type childType = Type.value(param.getNodeName());
          if ((childType != Type.PARAM) || !(param.hasAttributes())) {
            throw new UnsupportedDataTypeException("Cannot parse parameter: " + param.getNodeName());
          }
          Node paramType = param.getAttributes().getNamedItem(TYPE);
          if (paramType == null) {
            throw new UnsupportedDataTypeException(method.toString() + " Cannot parse parameter: "
                + param.getNodeName() + " (no type given)");
          }
          method.append("/").append(paramType.getNodeValue());
        }
        method.append(";");
        container.methods.add(method.toString());
      } else {
        container.methodWildcards.add(method.toString());
      }
    } else if (!node.hasChildNodes()) {
      StringBuilder joined = joinParts(parts);
      boolean prefix = isPrefix(parts);
      if (nodeType == Type.PACKAGE) {
        if (prefix)
          container.packageWildcards.add(joined.toString());
        else
          container.packages.add(joined.toString());
      } else if (nodeType == Type.CLASS) {
        if (prefix)
          container.classesWildcards.add(joined.toString());
        else
          container.classes.add(joined.toString());
      }
    } else {
      NodeList childNodes = node.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); ++i) {
        parse(childNodes.item(i), parts, container);
      }
    }
    parts.pop();
  }

}
