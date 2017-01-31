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

import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

public class LogMessageTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 4321636797820597915L;

  private ArrayList<String[]> logMessages;

  private String[] COLUMN_NAMES = { "Type", "Tag", "Message" };


  public enum TYPE {
    V, D, I, W, E, F
  }

  public LogMessageTableModel() {
    logMessages = new ArrayList<>();
  }

  public void addLogMessage(TYPE type, String tag, String msg) {
    synchronized (logMessages) {
      String[] newMsg = { type.toString(), tag, msg };
      logMessages.add(newMsg);
      fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }
  }

  @Override
  public String getColumnName(int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public int getRowCount() {
    synchronized (logMessages) {
      return logMessages.size();
    }
  }

  @Override
  public Object getValueAt(int row, int col) {
    synchronized (logMessages) {
        return logMessages.get(row)[col];
    }
  }

}
