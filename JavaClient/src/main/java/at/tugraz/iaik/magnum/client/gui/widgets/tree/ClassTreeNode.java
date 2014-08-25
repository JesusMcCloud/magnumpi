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

import java.awt.Component;
import java.awt.Font;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tugraz.iaik.magnum.client.gui.EvesDropper;
import at.tugraz.iaik.magnum.client.gui.GuiConstants;
import at.tugraz.iaik.magnum.model.ClassModel;
import at.tugraz.iaik.magnum.model.MethodModel;

public class ClassTreeNode extends ClassTreeDefaultNode {
  private static final long                              serialVersionUID = 191698959569800569L;
  private ClassModel                                     classModel;

  private ConcurrentHashMap<MethodModel, MethodTreeNode> childNodes;
  private ConcurrentHashMap<MethodModel, Integer>        childInvocations;

  private JLabel                                         label;
  private boolean                                        highlight;
  private int                                            invocations;
  private boolean                                        empty            = false;

  public ClassTreeNode(ClassModel classModel) {
    this(classModel, false);
  }

  public ClassTreeNode(final ClassModel classModel, boolean empty) {
    this.classModel = classModel;
    childNodes = new ConcurrentHashMap<>();
    childInvocations = new ConcurrentHashMap<>();
    setText(classModel.getDisplayName());

    if (empty) {
      this.empty = true;
      return;
    }

    for (final MethodModel method : classModel.getMethods()) {
      final MethodTreeNode methodNode = new MethodTreeNode(method);
      add(methodNode);
      childNodes.put(method, methodNode);
      method.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          childInvocations.put(method, method.getNumInvocations());
          highlight();
          methodNode.highlight();
        }
      });
    }
  }

  public MethodTreeNode getMethodNode(MethodModel method) {
    return childNodes.get(method);
  }

  @Override
  public Component getIcon() {
    String iconName = "class" + getIconSuffixFromModifiers(classModel.getModifiers());
    Icon icon = new ImageIcon(EvesDropper.class.getResource(GuiConstants.PATH_RES_ICONS + iconName + ".png"));
    if (label == null) {
      label = new JLabel(getText(), icon, SwingConstants.LEFT);

    }
    if (!empty) {
      invocations = 0;
      for (int i : childInvocations.values())
        invocations += i;
    }
    if (invocations > 0)
      label.setOpaque(true);
    background = caclulateColor(invocations);
    label.setText(invocations > 0 ? getText() + " (" + invocations + ")" : getText());
    label.setFont(new Font(Font.SANS_SERIF, (highlight ? Font.BOLD : Font.PLAIN), label.getFont().getSize()));
    label.setBackground(background);
    return label;
  }

  public ClassModel getClassModel() {
    return classModel;
  }

  public void highlight() {
    highlight = true;
  }

  public boolean isHighlighted() {
    return highlight;
  }

  public Collection<MethodTreeNode> getChildren() {
    return childNodes.values();
  }

  public ClassTreeNode copy(boolean full) {
    ClassTreeNode ctn = new ClassTreeNode(classModel, !full);
    if (full) {
      ctn.childInvocations = childInvocations;
      for (final MethodModel method : classModel.getMethods()) {
        if (childNodes.get(method).isHighlighted())
          ctn.childNodes.get(method).highlight();
      }
    }
    ctn.invocations = invocations;
    ctn.highlight = highlight;
    return ctn;
  }

  public ClassTreeNode copy() {
    return copy(false);
  }

  public void updateInvocations() {
    for (MethodModel method : childNodes.keySet()) {

      if (method.getNumInvocations() > 0) {
        childInvocations.put(method, method.getNumInvocations());
        childNodes.get(method).highlight();
        if (!method.getSimpleMethodName().endsWith("<init>"))
          highlight();
      }
    }
  }
}
