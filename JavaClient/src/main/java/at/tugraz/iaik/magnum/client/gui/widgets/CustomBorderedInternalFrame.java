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
package at.tugraz.iaik.magnum.client.gui.widgets;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyVetoException;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import at.tugraz.iaik.magnum.client.gui.utils.WindowStateManager;

public class CustomBorderedInternalFrame extends JInternalFrame implements StatefulComponent {

  private static final long serialVersionUID = 5821561035987568691L;

  public CustomBorderedInternalFrame() {
    this("");
  }

  public CustomBorderedInternalFrame(String title) {
    super(title);
    setName("magnumFrame." + title);
    setBorder(BorderFactory.createMatteBorder(2, 2, 6, 2, getBackground()));

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentMoved(ComponentEvent e) {
        storeState(e.getComponent());
      }

      @Override
      public void componentResized(ComponentEvent e) {
        storeState(e.getComponent());
      }
    });
    addInternalFrameListener(new InternalFrameAdapter() {
      @Override
      public void internalFrameDeiconified(InternalFrameEvent e) {
        storeState(e.getInternalFrame());
      }

      public void internalFrameIconified(InternalFrameEvent e) {
        storeState(e.getInternalFrame());
      }
    });
  }

  private void storeState(Component component) {
    WindowStateManager.store(component);
  }

  @Override
  public int getExtendedState() {
    int state = 0;
    state = state | (isMaximum() ? 4 : 0);
    state = state | (isIcon() ? 2 : 0);
    state = state | (isSelected() ? 1 : 0);
    return state;
  }

  @Override
  public void setExtendedState(int state) {
    try {
      setMaximum((state & 4) > 0);
      setIcon((state & 2) > 0);
      setSelected((state & 1) > 0);
    } catch (PropertyVetoException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean isNormalState() {
    return getExtendedState() <= 1;
  }
}