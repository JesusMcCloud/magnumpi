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
package at.tugraz.iaik.magnum.client.gui.utils;

import java.awt.Component;
import java.awt.Rectangle;

import at.tugraz.iaik.magnum.client.conf.ConfFile;
import at.tugraz.iaik.magnum.client.gui.widgets.StatefulComponent;

public abstract class WindowStateManager {
  private static final String separatorChar = ";";

  public static void store(Component... components) {
    for (Component component : components) {
      ConfFile.set(getComponentID(component), state2String(component));
    }
  }

  public static void load(Component... components) {
    for (Component component : components) {
      String state = ConfFile.get(getComponentID(component));
      if (state != null)
        string2state(state, component);

    }
  }

  private static String state2String(Component component) {
    Rectangle rect;
    try {
      rect = state2Rect(ConfFile.get(getComponentID(component)).split(separatorChar));
    } catch (Exception e) {
      rect = component.getBounds();
    }
    StringBuilder bld = new StringBuilder();

    if ((component instanceof StatefulComponent) && ((StatefulComponent) component).isNormalState()) {
      rect = component.getBounds();
    }

    bld.append(rect.x).append(separatorChar);
    bld.append(rect.y).append(separatorChar);
    bld.append(rect.width).append(separatorChar);
    bld.append(rect.height).append(separatorChar);
    bld.append(((StatefulComponent) component).getExtendedState());

    return bld.toString();
  }

  private static void string2state(String str, Component comp) {
    String[] state = str.split(separatorChar);
    comp.setBounds(state2Rect(state));
    if (comp instanceof StatefulComponent) {
      StatefulComponent frame = (StatefulComponent) comp;
      frame.setExtendedState(Integer.parseInt(state[4]));
    }
  }

  private static Rectangle state2Rect(String[] state) {
    return new Rectangle(Integer.parseInt(state[0]), Integer.parseInt(state[1]), Integer.parseInt(state[2]),
        Integer.parseInt(state[3]));
  }

  private static String getComponentID(Component comp) {
    if (comp.getName() != null)
      return comp.getName();
    return Integer.toString(comp.hashCode(), 26);
  }
}
