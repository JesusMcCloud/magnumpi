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
package at.tugraz.iaik.magnum.client.gui.widgets.tree;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public class FormattedInstanceNodeFactory {

  public static void format(DefaultMutableTreeNode node, Object data) {

    formatInternal(node, data, new HashSet<Object>());

  }

  public static String toString(Object data) {
    try {

      return toStringInternal(data, new HashSet<Object>(), 0);

    } catch (StackOverflowError er) {
      System.out.print("e");
      return "Circular Dependency!";
    }
  }

  private static void formatInternal(DefaultMutableTreeNode node, Object data, Set<Object> alreadyVisited) {

    if (alreadyVisited.contains(data))
      return;
    if (data == null) {
      formatNullValue(node);
      return;
    }
    alreadyVisited.add(data);

    Class<? extends Object> clazz = data.getClass();

    if (isPrimitive(clazz)) {
      formatPrimitive(node, data);
      return;
    }

    if (clazz.isArray()) {
      int length = Array.getLength(data);
      node.setUserObject(clazz.getComponentType().getName() + "[" + length + "]");

      for (int i = 0; i < length; ++i) {
        Object element = Array.get(data, i);
        DefaultMutableTreeNode arrayElementNode = new DefaultMutableTreeNode();
        formatInternal(arrayElementNode, element, alreadyVisited);
        node.add(arrayElementNode);
      }

      return;
    }

    node.setUserObject("Instance of " + clazz.getName());
    Field[] fields = clazz.getFields();

    for (Field field : fields) {
      try {
        // if((clazz.equals(Thread.class)) &&
        // (field.getClass().equals(Thread.class)))
        // continue;
        Object fieldData = field.get(data);
        DefaultMutableTreeNode fieldNode = new DefaultMutableTreeNode(field.getType() + " " + field.getName());

        DefaultMutableTreeNode fieldDataNode = new DefaultMutableTreeNode();
        // format(fieldDataNode, fieldData);
        formatInternal(fieldDataNode, fieldData, alreadyVisited);
        fieldNode.add(fieldDataNode);
        node.add(fieldNode);

        // alreadyVisited.add(field);
      } catch (IllegalArgumentException | IllegalAccessException e) {
        node.add(new DefaultMutableTreeNode("Illegal access exception"));
      }
    }
  }

  private static String toStringInternal(Object data, HashSet<Object> alreadyVisited, int depth) {

    if (data == null)
      return createNullString();
    if (alreadyVisited.contains(data))
      return "";
    alreadyVisited.add(data);
    Class<? extends Object> clazz = data.getClass();
    StringBuilder returnString = new StringBuilder();
    for (int i = 0; i < depth; ++i) {
      returnString.append(" ");
    }
    String indent = returnString.toString();
    depth += 2;
    if (isPrimitive(clazz)) {
      return returnString.append(createPrimtiveString(data, false)).toString();
    }

    if (clazz.isArray()) {
      int length = Array.getLength(data);
      returnString.append(clazz.getComponentType().getName()).append("[" + length + "]: [\n").append(indent);
      if (clazz.getComponentType().getSimpleName().equalsIgnoreCase("Byte")){
        for (int i = 0; i < length; ++i) {
          Object element = Array.get(data, i);
          returnString.append(createPrimtiveString(element, false));
          alreadyVisited.add(element);
        }
        return returnString.append("\n").append(indent).append("]").toString();
      } else  if ( clazz.getComponentType().getSimpleName().equals("Character")
          || clazz.getComponentType().getSimpleName().equals("char")) {
        for (int i = 0; i < length; ++i) {
          Object element = Array.get(data, i);
          returnString.append(element);
          alreadyVisited.add(element);
        }
        return returnString.append("\n").append(indent).append("]").toString();
      } else {
        for (int i = 0; i < length; ++i) {
          Object element = Array.get(data, i);
          returnString.append(indent).append(toStringInternal(element, alreadyVisited, depth)).append("\n")
              .append(indent);
        }
      }
      return returnString.append("]").toString();
    }

    returnString.append(clazz.getCanonicalName()).append(" {");
    Field[] fields = clazz.getFields();
    int i = 0;
    indent += indent;
    for (Field field : fields) {
      // if((clazz.equals(Thread.class)) &&
      // (field.getClass().equals(Thread.class)))
      // continue;
      try {
        if (i++ != 0)
          returnString.append("; ");
        Object fieldData = field.get(data);
        returnString.append("\n").append(indent);
        returnString.append(field.getType()).append(" ").append(field.getName()).append(" : ");

        returnString.append(toStringInternal(fieldData, alreadyVisited, depth)).append(" ");

        // alreadyVisited.add(field);
      } catch (IllegalArgumentException | IllegalAccessException e) {
        returnString.append("<<ERROR>>");
      }
    }
    returnString.append("}");
    return returnString.toString();
  }

  private static void formatNullValue(MutableTreeNode node) {
    node.setUserObject(createNullString());
  }

  private static boolean isPrimitive(Class<? extends Object> clazz) {

    return clazz.equals(Integer.class) || clazz.equals(Long.class) || clazz.equals(Short.class)
        || clazz.equals(Double.class) || clazz.equals(Float.class) || clazz.equals(String.class)
        || clazz.equals(Boolean.class) || clazz.equals(Byte.class) || clazz.equals(Character.class)
        || clazz.equals(Void.class);
  }

  private static void formatPrimitive(MutableTreeNode node, Object data) {
    node.setUserObject(createPrimtiveString(data, true));
  }

  private static String createNullString() {
    return ("(null)");
  }

  private static String createPrimtiveString(Object data, boolean full) {
    if ((data != null) && (data instanceof Byte)) {
      byte b = (Byte) data;
      if (full)
        return (data.toString() + " (" + Character.toString((char) b) + ")");
      return Character.toString((char) b);
    }

    if (data instanceof String)
      return "\""
          + data.toString().replace("\"", "\\\"").replace("{", "\\{").replace("}", "\\}").replace("\n", "\\n")
              .replace("\r", "\\r") + "\"";
    else if (data instanceof Character) {
      char c = (Character) data;
      if (c == '\n')
        return "'\\n'";
      else if (c == '\r')
        return "'\\r'";
      else if (c == '\'')
        return "'\\''";
      return "'" + c + "'";
    } else
      return data.toString().replace("{", "\\{").replace("}", "\\}");

  }
}
