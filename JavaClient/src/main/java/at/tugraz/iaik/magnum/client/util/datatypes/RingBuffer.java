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

import java.util.LinkedList;

public class RingBuffer<E> {

  private int           size;
  private LinkedList<E> mem;
  private int           pos;

  public RingBuffer(int size) {
    this.size = size;
    this.mem = new LinkedList<>();
    pos = 0;
  }

  public void add(E elem) {
    mem.addFirst(elem);
    if (mem.size() > size)
      mem.removeLast();
    else
      ++size;
  }

  public E prev() {
    System.out.println("PREV");
    if (--pos < 0)
      pos = 0;
    return mem.get(pos);
  }

  public E next() {
    System.out.println("NEXT");
    if (++pos > (mem.size() - 1))
      pos = mem.size() - 1;
    return mem.get(pos);
  }

  public E current() {
    return mem.get(pos);
  }
}
