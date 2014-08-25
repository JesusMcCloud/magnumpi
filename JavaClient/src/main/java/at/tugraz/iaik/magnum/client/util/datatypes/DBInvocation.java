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

import java.util.Date;

public class DBInvocation {
  // "CREATE TABLE IF NOT EXISTS invocations(ID IDENTITY PRIMARY KEY, INTERESTING BOOLEAN, CLASS TEXT, METHOD TEXT, PARAM_STRING TEXT, RETURN_STRING TEXT, TIMESTAMP DATE)");
  private final long    id, callerID;
  private boolean       interesting;
  private final boolean callerKnown;
  private final String  className, methodName, paramString, retString;
  private final Date    timestamp;
  private final String  uniqueMethodName;

  public DBInvocation(long id, boolean interesting, String className, String methodName, String paramString,
      String retString, Date timestamp, String uniqueMethodName) {
    this.id = id;
    this.interesting = interesting;
    this.className = className;
    this.methodName = methodName;
    this.paramString = paramString;
    this.retString = retString;
    this.timestamp = timestamp;
    this.uniqueMethodName = uniqueMethodName;
    this.callerID = 0;
    this.callerKnown = false;
  }

  public DBInvocation(long id, boolean interesting, String className, String methodName, String paramString,
      String retString, Date timestamp, String uniqueMethodName, long callerID, boolean callerKnown) {
    this.id = id;
    this.interesting = interesting;
    this.className = className;
    this.methodName = methodName;
    this.paramString = paramString;
    this.retString = retString;
    this.timestamp = timestamp;
    this.uniqueMethodName = uniqueMethodName;
    this.callerID = callerID;
    this.callerKnown = callerKnown;

  }

  public long getId() {
    return id;
  }

  public void setInteresting(boolean interesting) {
    this.interesting = interesting;
  }

  public boolean isInteresting() {
    return interesting;
  }

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getParamString() {
    return paramString;
  }

  public String getRetString() {
    return retString;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public long getCallerID() {
    return callerID;
  }

  public boolean isCallerKnown() {
    return callerKnown;
  }

  public String prettyPrint() {
    StringBuilder bld = new StringBuilder("Class:   ").append(className).append("\n");
    bld.append("Method:  ").append(methodName).append("\n");
    bld.append("Args:    ").append(paramString).append("\n\n");
    bld.append("Returns: ").append(retString).append("\n\n");
    bld.append("Time:    ").append(timestamp);
    return bld.toString();
  }

  public String getUniqueMethodName() {
    return uniqueMethodName;
  }

}
