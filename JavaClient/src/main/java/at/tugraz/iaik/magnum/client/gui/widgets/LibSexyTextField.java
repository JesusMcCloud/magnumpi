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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import at.tugraz.iaik.magnum.client.gui.GuiConstants;

public class LibSexyTextField extends JTextField {
  private static final Icon             ICON_CLEAR = new ImageIcon(
                                                       LibSexyTextField.class.getResource(GuiConstants.PATH_RES_ICONS
                                                           + "edit-delete.png"));
  private JButton                       btnClear;

  private LinkedHashSet<ActionListener> listeners;

  public LibSexyTextField() {
    super();
    listeners = new LinkedHashSet<>();
    btnClear = new JButton(ICON_CLEAR);
    btnClear.setBorderPainted(false);
    btnClear.setBorder(new EmptyBorder(2, 2, 2, 2));
    ComponentBorder cb = new ComponentBorder(btnClear);
    cb.install(this);
    btnClear.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        clear(e);
      }
    });
  }

  private void clear(ActionEvent e) {
    this.setText("");
    this.requestFocus();
    for (ActionListener l : listeners) {
      l.actionPerformed(e);
    }
  }

  public synchronized void addClearListener(ActionListener a) {
    listeners.add(a);
  }

  public ActionListener[] getClearListeners() {
    return listeners.toArray(new ActionListener[listeners.size()]);
  }
}
