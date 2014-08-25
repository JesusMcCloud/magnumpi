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
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class PackageConfigTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 4321636797820597915L;

  private class PackageStatus {
    Boolean status;
    String name;
    String pkg;

    public PackageStatus(boolean status, String name, String pkg) {
      this.status = status;
      this.name = name;
      this.pkg = pkg;
    }
  };

  private String needle;

  private ArrayList<PackageStatus> packages;
  private LinkedList<PackageStatus> filteredPackages;

  String[] COLUMN_NAMES = { "Status", "Name", "Package" };

  public PackageConfigTableModel() {
    packages = new ArrayList<>();
    filteredPackages = new LinkedList<>();
  }

  public void filterBy(String needle) {
    this.needle = needle;
    filteredPackages.clear();
    if (needle == null)
      return;
    this.needle = needle.toLowerCase();
    for (PackageStatus status : packages) {
      if (status.name.toLowerCase().contains(needle) || status.pkg.toLowerCase().contains(needle))
        filteredPackages.add(status);
    }
  }

  public void addPackageStatus(boolean hooked, String name, String pkg) {
    PackageStatus status = new PackageStatus(hooked, name, pkg);
    packages.add(status);
    int affectedRow;
    if (needle == null) {
      affectedRow = packages.size() - 1;
      fireTableRowsInserted(affectedRow, affectedRow);
    } else {
      if (name.toLowerCase().contains(needle) || pkg.toLowerCase().contains(needle))
        filteredPackages.add(status);
      affectedRow = filteredPackages.size() - 1;
      fireTableRowsInserted(affectedRow, affectedRow);
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
    if (needle == null) {
      return packages.size();
    } else
      return filteredPackages.size();
  }

  @Override
  public boolean isCellEditable(int row, int col) {
    if (col == 0) {
      return true;
    }
    return false;
  }

  public void setValueAt(Object value, int row, int col) {
    List<PackageStatus> list = needle == null ? packages : filteredPackages;
    switch (col) {
    case 0:
      list.get(row).status = (Boolean) value;
    }
    fireTableCellUpdated(row, col);
  }

  public Class getColumnClass(int column) {
    return (getValueAt(0, column).getClass());
  }

  @Override
  public Object getValueAt(int row, int col) {
    List<PackageStatus> list = needle == null ? packages : filteredPackages;

    switch (col) {
    case 0:
      return list.get(row).status;
    case 1:
      return list.get(row).name;
    case 2:
      return list.get(row).pkg;
    }
    return null;
  }

  public void clear() {
    this.needle = null;
    packages.clear();
    filteredPackages.clear();

  }

}
