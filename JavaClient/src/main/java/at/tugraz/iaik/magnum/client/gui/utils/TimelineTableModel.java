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

import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import at.tugraz.iaik.magnum.client.util.datatypes.DBInvocation;

public class TimelineTableModel extends AbstractTableModel {
  private static final long  serialVersionUID = 4321636797820597915L;

  private List<DBInvocation> events;

  String[]                   COLUMN_NAMES     = { "?", "Class", "Method", "Arguments", "Return Value", "Timestamp" };

  public TimelineTableModel() {
    events = new LinkedList<>();
  }

  public void setContent(List<DBInvocation> events) {
    this.events = events;
  }

  @Override
  public String getColumnName(int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public int getColumnCount() {
    return 6;
  }

  @Override
  public int getRowCount() {
    return events.size();

  }

  @Override
  public boolean isCellEditable(int row, int col) {
    if (col == 0) {
      return true;
    }
    return false;
  }

  public void setValueAt(Object value, int row, int col) {
    switch (col) {
    case 0:
      events.get(row).setInteresting((Boolean) value);
      fireTableCellUpdated(row, col);
      return;
    }
  }

  public DBInvocation getInvocationAt(int index){
    synchronized (events) {
     return events.get(index); 
    }
  }
  
  public Class getColumnClass(int column) {
    return (getValueAt(0, column).getClass());
  }

  @Override
  public Object getValueAt(int row, int col) {
    synchronized (events) {
        switch (col) {
        case 0:
          return events.get(row).isInteresting();
        case 1:
          return events.get(row).getClassName();
        case 2:
          return events.get(row).getMethodName();
        case 3:
          return events.get(row).getParamString();
        case 4:
          return events.get(row).getRetString();
        case 5:
          return events.get(row).getTimestamp().toString();
        }
     
      throw new IllegalArgumentException("Bad Programmer!");
    }
  }
}
