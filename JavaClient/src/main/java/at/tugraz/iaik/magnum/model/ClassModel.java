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
package at.tugraz.iaik.magnum.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.ChangeListener;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class ClassModel extends ModelObject implements Comparable<ClassModel> {
  @XmlElement(name = "method")
  private final List<MethodModel>   methods;

  @XmlAttribute
  private final String              className;

  @XmlAttribute
  private final int                 modifiers;

  private ArrayList<ChangeListener> listeners;

  private String                    displayName;

  public ClassModel(final String className, final int modifiers) {
    this(className, modifiers, className);
  }

  public ClassModel(final String className, final int modifiers, String displayName) {
    this.className = className;
    this.displayName = displayName;
    this.modifiers = modifiers;
    listeners = new ArrayList<>(4);
    methods = new CopyOnWriteArrayList<MethodModel>();

  }

  public String getClassName() {
    return className;
  }

  public void addMethod(final MethodModel method) {
    methods.add(method);
  }

  public List<MethodModel> getMethods() {
    return methods;
  }

  public int getModifiers() {
    return modifiers;
  }

  @Override
  public int compareTo(ClassModel o) {
    if (o != null)
      return className.compareTo(o.getClassName());
    else
      return -1;
  }

  public void addChangeListener(ChangeListener l) {
    listeners.add(l);
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

}
