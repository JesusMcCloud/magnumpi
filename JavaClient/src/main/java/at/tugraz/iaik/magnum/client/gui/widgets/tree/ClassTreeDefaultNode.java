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

import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Modifier;

public abstract class ClassTreeDefaultNode extends AbstractTreeNode {
  private static final long serialVersionUID = 2473614497096146171L;

  protected static Color caclulateColor(int invocations) {
    if (invocations <= 64) {
      return new Color(255 - invocations * 2, 255 - invocations * 2, 255);
    } else if (invocations <= 7000) {
      invocations = (invocations - 600) / 100;
      return new Color(128 - invocations, 128 - invocations, 255);
    } else if (invocations <= 100000) {
      invocations = (invocations - 580) / 520;
      return new Color(64 + invocations, 64, 255 - invocations);
    } else
      return Color.RED;
  }

  protected Color background;

  public abstract Component getIcon();
  
  public ClassTreeDefaultNode() {
    super();
  }
  
  protected String getIconSuffixFromModifiers(int modifiers) {
    String iconName = "";
    
    if (Modifier.isPrivate(modifiers))
      iconName += "P";
    else if (Modifier.isProtected(modifiers))
      iconName += "PR";
    
    if (Modifier.isAbstract(modifiers))
      iconName += "_A";
    
    return iconName;
  }
}
