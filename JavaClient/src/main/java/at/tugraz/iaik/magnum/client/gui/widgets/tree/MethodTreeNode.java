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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import at.tugraz.iaik.magnum.client.gui.EvesDropper;
import at.tugraz.iaik.magnum.client.gui.GuiConstants;
import at.tugraz.iaik.magnum.model.MethodModel;
import at.tugraz.iaik.magnum.model.TypeModel;

public class MethodTreeNode extends ClassTreeDefaultNode {
  private static final long serialVersionUID = -1882866853222598559L;
  private final MethodModel methodModel;
  private boolean           highlight;
  private JLabel            label;

  public MethodTreeNode(MethodModel method) {
    this.methodModel = method;
    highlight = false;

    add(new ReturnTreeNode(method.getReturnType()));
    setText(method.getText());
    for (TypeModel paramType : method.getParameterTypes())
      add(new ParameterTreeNode(paramType));
  }

  @Override
  public Component getIcon() {
    String iconName = "method" + getIconSuffixFromModifiers(getMethodModel().getModifiers());
    Icon icon = new ImageIcon(EvesDropper.class.getResource(GuiConstants.PATH_RES_ICONS + iconName + ".png"));

    if (label == null) {
      label = new JLabel(getText(), icon, SwingConstants.LEFT);
    }
    label.setText(methodModel.getNumInvocations() > 0 ? getText() + " (" + methodModel.getNumInvocations() + ")"
        : getText());
    label.setFont(new Font(Font.SANS_SERIF, (highlight ? Font.BOLD : Font.PLAIN), label.getFont().getSize()));
    return label;
  }

  public MethodModel getMethodModel() {
    return methodModel;
  }

  public void highlight() {
    highlight = true;
  }

  public MethodTreeNode copy() {
    MethodTreeNode mtn = new MethodTreeNode(methodModel);
    mtn.highlight = highlight;
    return mtn;
  }

  public boolean isHighlighted() {
    return highlight;
  }
}