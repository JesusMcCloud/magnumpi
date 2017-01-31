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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import at.tugraz.iaik.magnum.client.util.datatypes.RingBuffer;

public class HistoryTextField extends LibSexyTextField {

  private RingBuffer<String> cmdBuffer;

  private KeyAdapter         upDown;

  private String             lastText;

  public HistoryTextField() {
    super();
    cmdBuffer = new RingBuffer<>(50);
    upDown = new KeyAdapter() {
      public void keyReleased(KeyEvent evt) {
        int keyCode = evt.getExtendedKeyCode();
        if (keyCode == KeyEvent.VK_UP)
          setFromHistory(true);
        else if (keyCode == KeyEvent.VK_DOWN)
          setFromHistory(false);
        else if (keyCode == KeyEvent.VK_ENTER)
          invoke();
      }
    };
    addKeyListener(upDown);
  }

  private void setFromHistory(boolean prev) {
    if (prev)
      setText(cmdBuffer.prev());
    else
      setText(cmdBuffer.next());
  }

  public String getLastText() {
    return "SELECT * , rowid FROM invocations " + lastText;
  }

  public void invoke() {
    cmdBuffer.add(getText());
    lastText = cmdBuffer.current();
  }

}
