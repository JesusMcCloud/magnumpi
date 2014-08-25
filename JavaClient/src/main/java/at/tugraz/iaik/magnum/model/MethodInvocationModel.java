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

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import at.tugraz.iaik.magnum.client.util.InstanceXmlAdapter;

public class MethodInvocationModel extends ModelObject {
  @XmlElement(name = "instance")
  @XmlElementWrapper(name = "argumentValues")
  private final List<InstanceModel> arguments;

  @XmlElement
  @XmlJavaTypeAdapter(InstanceXmlAdapter.class)
  private InstanceModel             returnValue;

  @XmlAttribute
  private final long                callId;

  @XmlAttribute
  private final long                invocationTime;

  @XmlAttribute
  private final long                callerId;

  @XmlAttribute
  private boolean                   didReturn;

  @XmlAttribute
  private final boolean             callerKnown;

  private MethodModel               methodModel;

  public MethodInvocationModel(final MethodModel methodModel, final List<InstanceModel> arguments,
      final InstanceModel returnValue, final long timestamp, final long callId, final boolean didReturn,
      final long caller, final boolean callerKnown) {
    this.methodModel = methodModel;
    this.arguments = arguments;
    this.returnValue = returnValue;
    this.invocationTime = timestamp;
    this.callId = callId;
    this.didReturn = didReturn;
    this.callerId = caller;
    this.callerKnown = callerKnown;
  }

  public List<InstanceModel> getArguments() {
    return arguments;
  }

  public InstanceModel getReturnValue() {
    return returnValue;
  }

  public long getInvocationTime() {
    return invocationTime;
  }

  public MethodModel getMethodModel() {
    return methodModel;
  }

  void setMethodModel(final MethodModel methodModel) {
    this.methodModel = methodModel;
  }

  public long getCallId() {
    return callId;
  }

  public long getCallerId() {
    return callerId;
  }

  public boolean didReturn() {
    return didReturn;
  }

  public boolean isCallerKnown() {
    return callerKnown;
  }

  protected void setDeferredReturnInfo(TypeModel type, byte[] data) {
    didReturn = true;
    returnValue = new InstanceModel(type, data);
  }
}
