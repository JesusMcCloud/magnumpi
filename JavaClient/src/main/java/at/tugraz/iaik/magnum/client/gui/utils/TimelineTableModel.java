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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import at.tugraz.iaik.magnum.client.util.datatypes.DBInvocation;

public class TimelineTableModel extends AbstractTableModel {
  private static final long  serialVersionUID = 4321636797820597915L;

  private List<DBInvocation> events;

  final private String[] COLUMN_NAMES     = { "?", "Class", "Method", "Arguments", "Return Value", "Timestamp" };
  private List<String> blacklistedTimelineContent;
  private String selectOnlyTimelineContent;

  public TimelineTableModel() {
    events = Collections.synchronizedList(new LinkedList<DBInvocation>());
    blacklistedTimelineContent = new ArrayList<>();
    selectOnlyTimelineContent = "";
  }

  public void setContent(List<DBInvocation> events) {
    synchronized (this.events) {
      this.events = events;
        fireTableDataChanged();
    }
  }

  @Override
  public String getColumnName(int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public int getRowCount() {
    synchronized (events) {
      return this.events.size();
    }
  }

  @Override
  public boolean isCellEditable(int row, int col) {
    if (col == 0) {
      return true;
    }
    return false;
  }

  public void deleteData() {
    synchronized (this.events) {
      int rows = this.getRowCount();
      if (rows == 0) {
        return;
      }
      this.events.clear();
      fireTableRowsDeleted(0, rows - 1);
    }
  }

  public void addToBlacklist(String content)
  {
    if(!blacklistedTimelineContent.contains(content))
    {
      blacklistedTimelineContent.add(content);
      deleteData();
    }
  }

  public void setSelectOnly(String content)
  {
    selectOnlyTimelineContent = content;
    deleteData();
  }

  public void restore()
  {
    blacklistedTimelineContent.clear();
    selectOnlyTimelineContent = "";
    deleteData();
  }

  private boolean isInvocationInModel(long rowId)
  {
    synchronized (this.events)
    {
      for (DBInvocation element : this.events
           ) {
        if(element.getRowId() == rowId)
          return true;
      }
      return false;
    }
  }

  public void setValueAt(Object value, int row, int col) {
    //when element is already in model -> return
    if (value instanceof DBInvocation && isInvocationInModel(((DBInvocation) value).getRowId()))
      return;

    synchronized (this.events) {
      int rowcount = this.getRowCount();
      switch (col) {
        case 0:
          events.get(row).setInteresting((Boolean) value);

          fireTableChanged(new TableModelEvent(this, row, row, 0));
          break;

        default:
       //   System.out.println("case default" + value + "row: "+ row + " col: " + col + "rowcount: " + rowcount);

          DBInvocation dbElement = (DBInvocation) value;

          String className = dbElement.getClassName();
          String methodName = dbElement.getMethodName();
          String paramString = dbElement.getParamString();
          String retString = dbElement.getRetString();
          String timestamp = dbElement.getTimestamp().toString();

          if(selectOnlyTimelineContent.length() != 0)
          {
            if(selectOnlyTimelineContent.equals(className) ||
                    selectOnlyTimelineContent.equals(methodName) ||
                    selectOnlyTimelineContent.equals(paramString) ||
                    selectOnlyTimelineContent.equals(retString) ||
                    selectOnlyTimelineContent.equals(timestamp))
            {
              this.events.add((DBInvocation) value);
              fireTableRowsInserted(rowcount, rowcount);
            }
          }
          else
          {
            if(!(blacklistedTimelineContent.contains(className) ||
                    blacklistedTimelineContent.contains(methodName) ||
                    blacklistedTimelineContent.contains(paramString) ||
                    blacklistedTimelineContent.contains(retString) ||
                    blacklistedTimelineContent.contains(timestamp)))
            {
              this.events.add((DBInvocation) value);
              fireTableRowsInserted(rowcount, rowcount);
            }
          }
          break;
      }
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


    if(this.events.isEmpty())
    {
      return "";
    }
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
