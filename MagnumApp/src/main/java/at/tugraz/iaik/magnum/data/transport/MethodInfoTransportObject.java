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
package at.tugraz.iaik.magnum.data.transport;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

public abstract class MethodInfoTransportObject extends ClassInfoTransportObject {
  private static final long serialVersionUID = -4218898185703438262L;

  private final String methodName;
  private final String[] parameterTypes;
  private final String returnType;

  protected MethodInfoTransportObject(Member method) {
    super(method.getDeclaringClass());

    if(method instanceof Method)
      methodName = method.getName();
    else
      methodName = "<init>";

    Class<?>[] methodParameterTypes;
    if (method instanceof Method)
      methodParameterTypes = ((Method) method).getParameterTypes();
    else
      methodParameterTypes = ((Constructor) method).getParameterTypes();

    parameterTypes = new String[methodParameterTypes.length];

    for (int i = 0; i < methodParameterTypes.length; ++i)
      parameterTypes[i] = methodParameterTypes[i].getName();
    if (method instanceof Method)
      returnType = ((Method) method).getReturnType().getName();
    else
      returnType = null;

  }

  public String[] getParameterTypes() {
    return parameterTypes;
  }

  public String getUniqueMethodName() {
    StringBuilder builder = new StringBuilder(getMethodName());

    for (String type : getParameterTypes())
      builder.append('/').append(type);

    builder.append(';');

    return builder.toString();
  }

  public String getMethodName() {
    return className + "." + methodName;
  }

  public String getReturnType() {
    return returnType;
  }
}