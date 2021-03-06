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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import at.tugraz.iaik.magnum.client.gui.EvesDropper;
import at.tugraz.iaik.magnum.client.gui.GuiConstants;
import at.tugraz.iaik.magnum.model.TypeModel;

public class ReturnTreeNode extends ClassTreeDefaultNode {
  private static final long serialVersionUID = 6306572036591293374L;
  private final TypeModel returnType;

  public ReturnTreeNode(final TypeModel returnType) {
    this.returnType = returnType;
  }

  @Override
  public Component getIcon() {
    Icon icon = new ImageIcon(EvesDropper.class.getResource(GuiConstants.PATH_RES_ICONS + "argOut.png"));

    return new JLabel(returnType.getClassName(), icon, SwingConstants.LEFT);
  }
}