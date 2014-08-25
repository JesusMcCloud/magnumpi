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
package at.tugraz.iaik.magnum.conf;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class MethodHookConfig implements Serializable {

  private static final long   serialVersionUID = -1476109735460687678L;

  private static final String TYPE             = "type";
  private static final String NUM_INVOCATIONS  = "numInvocations";
  private static final String NAME_METHOD      = "methodName";

  public static final byte    NONE             = 0;
  public static final byte    FULL             = 1;
  public static final byte    TRIGGER          = 2;

  private byte                type;
  private int                 numInvocations;
  private final String        methodName;

  public MethodHookConfig(String methodName, byte type) {
    this.type = type;
    this.methodName = methodName;
  }

  public MethodHookConfig(String methodName, int numInvocations) {
    this.numInvocations = numInvocations;
    this.type = TRIGGER;
    this.methodName = methodName;
  }

  public int getNumInvocations() {
    return numInvocations;
  }

  public void setNumInvocations(int numInvocations) {
    this.numInvocations = numInvocations;
  }

  public byte getType() {
    return type;
  }

  public void setType(byte type) {
    this.type = type;
  }

  public String getMethodName() {
    return methodName;
  }

  public String toJSonString() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(NAME_METHOD, methodName);
    json.put(TYPE, type);
    json.put(NUM_INVOCATIONS, numInvocations);
    return json.toString();
  }

  public static MethodHookConfig fromJsonString(String json) throws Exception {
    JSONObject jsO = new JSONObject(json);
    byte type = (byte) jsO.getInt(TYPE);
    int numInvocations = jsO.getInt(NUM_INVOCATIONS);
    String methodName = jsO.getString(NAME_METHOD);
    return numInvocations > 0 ? new MethodHookConfig(methodName, numInvocations) : new MethodHookConfig(methodName,
        type);
  }

}
