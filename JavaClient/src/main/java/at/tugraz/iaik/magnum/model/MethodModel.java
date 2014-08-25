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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import at.tugraz.iaik.magnum.client.util.AdditionalJavaNameHelper;
import at.tugraz.iaik.magnum.client.util.InvocationMapXmlAdapter;
import at.tugraz.iaik.magnum.client.util.ReturnTypeXmlAdapter;
import at.tugraz.iaik.magnum.util.JavaNameHelper;

public class MethodModel extends ModelObject {
  @XmlAttribute
  private final String                           methodName;

  @XmlAttribute
  private final int                              modifiers;

  @XmlElement(name = "type")
  @XmlElementWrapper(name = "parameterTypes")
  private final List<TypeModel>                  parameterTypes;

  @XmlJavaTypeAdapter(ReturnTypeXmlAdapter.class)
  @XmlElement
  private final TypeModel                        returnType;

  @XmlJavaTypeAdapter(InvocationMapXmlAdapter.class)
  @XmlElement
  private final Map<Long, MethodInvocationModel> invocations;

  private ArrayList<ChangeListener>              listeners;

  public MethodModel(final String methodName, final List<TypeModel> parameterTypes, final TypeModel returnType,
      int modifiers) {
    this.methodName = methodName;
    this.parameterTypes = parameterTypes;
    this.returnType = returnType;
    this.modifiers = modifiers;
    listeners = new ArrayList<>(5);

    invocations = new ConcurrentHashMap<Long, MethodInvocationModel>();
  }

  public TypeModel getReturnType() {
    return returnType;
  }

  public List<TypeModel> getParameterTypes() {
    return parameterTypes;
  }

  public void addInvocation(MethodInvocationModel invocation) {
    invocations.put((Long) invocation.getCallId(), invocation);
    ChangeEvent e = new ChangeEvent(this);
    for (ChangeListener l : listeners)
      l.stateChanged(e);
  }

  public Collection<MethodInvocationModel> getInvocations() {
    return invocations.values();
  }

  public String getMethodName() {
    return methodName;
  }

  public int getModifiers() {
    return modifiers;
  }

  public String getUniqueMethodName() {
    return AdditionalJavaNameHelper.getUniqueMethodName(getMethodName(), parameterTypes);
  }

  public boolean hasReturnValue() {
    try {
      return !returnType.getClassName().equals("void");
    } catch (Exception e) {
      return false;
      // TODO what is going on here?
    }
  }

  public MethodInvocationModel getInvocation(long identifier) {
    return invocations.get((Long) identifier);
  }

  public String getSimpleMethodName() {
    return JavaNameHelper.extractSimpleMethodName(getMethodName());
  }

  public String getClassName() {
    return JavaNameHelper.extractClassName(getMethodName());
  }

  public String getText() {
    return getSimpleMethodName();
  }

  public int getNumInvocations() {
    return invocations.size();
  }

  public void addChangeListener(ChangeListener l) {
    listeners.add(l);
  }
}
