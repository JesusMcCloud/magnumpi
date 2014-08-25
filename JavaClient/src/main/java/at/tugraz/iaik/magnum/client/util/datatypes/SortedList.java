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
package at.tugraz.iaik.magnum.client.util.datatypes;

import java.util.AbstractList;
import java.util.Collections;

import org.apache.commons.collections4.list.TreeList;

public class SortedList<E extends Comparable> extends AbstractList<E> {

  private TreeList<E> list;

  public SortedList() {
    this.list = new TreeList<>();
  }

  public boolean add(E item) {
    boolean ret = list.add(item);
    Collections.sort(list);
    return ret;
  }

  @Override
  public E get(int index) {
    return list.get(index);
  }

  public int indexOf(E item) {
    return list.indexOf(item);
  }

  public void clear() {
    list.clear();
  }

  @Override
  public int size() {
    return list.size();
  }

}
