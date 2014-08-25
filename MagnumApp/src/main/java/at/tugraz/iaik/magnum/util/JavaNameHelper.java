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
package at.tugraz.iaik.magnum.util;

import java.util.List;

public class JavaNameHelper {

  // USED in HOOK
  public static String getUniqueMethodName(String className, String simpleMethodName, Class[] params) {
    return internalGetUniqueName(className + "." + simpleMethodName, params);
  }

  public static String getUniqueMethodName(String className, String simpleMethodName, List<String> parameters) {
    return internalGetUniqueName(className + "." + simpleMethodName, parameters);
  }

  public static String getUniqueMethodName(String fqmn, List<String> parameters) {
    return internalGetUniqueName(fqmn, parameters);
  }

  private static String internalGetUniqueName(String fqMethodName, List<String> parameters) {
    StringBuilder builder = new StringBuilder(fqMethodName);

    for (String type : parameters)
      builder.append('/').append(type);

    builder.append(';');

    return builder.toString();
  }

  private static String internalGetUniqueName(String fqMethodName, Class[] parameters) {
    StringBuilder builder = new StringBuilder(fqMethodName);

    for (Class type : parameters)
      builder.append('/').append(type.getName());

    builder.append(';');

    return builder.toString();
  }

  public static String convertClassName(String className) {
    if (!className.endsWith(";"))
      // It is a primitive type like, I, B, [[I, ...
      return className;

    return className.substring(1, className.length() - 1).replace('/', '.');
  }

  public static String extractSimpleMethodName(String fqmn) {
    int lastIndex = fqmn.lastIndexOf('.');
    if (lastIndex < 0)
      return fqmn;

    return fqmn.substring(lastIndex + 1);
  }

  public static String extractClassName(String fqmn) {
    int lastIndex = fqmn.lastIndexOf('.');
    if (lastIndex < 0)
      return fqmn;
    return fqmn.substring(0, lastIndex);
  }

}
