/*******************************************************************************
 * Copyright 2013 Alexander Jesner, Bernd PrÃ¼nster
 * Copyright 2013, 2014 Bernd PrÃ¼nster
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

import java.util.ArrayList;
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
  
  public static List<String> getParameters(String parameterString) {
	  
	  String[] params;
	  
	  List<String> list = new ArrayList<String>();
	  
	  if(parameterString.length() <= 0)
		  return list;
	  
	  if(parameterString.contains(",")) {
		  params = parameterString.split(",");
	  }
	  else {
		  params = new String[] { parameterString };
	  }
		  
	  for( String param : params) {
		  param = param.trim();
		  
		  if(param.charAt(0) == '"') {
			  // is String
			  param = param.substring(1, param.indexOf("\"", 1) );
		  } else {
			  // is type
			  if(param.contains(":")) {
				  param = param.substring(param.indexOf(":"));
				  param = param.substring(param.indexOf("[") + 1);
				  param = param.substring(0, param.lastIndexOf("]")).trim();
			  } else if(param.startsWith("(null)") ) {
				  param = "";
			  } else if(param.contains("{")) {
				  param = param.substring(param.indexOf("{") + 1);
				  param = param.substring(0, param.lastIndexOf("}")).trim();
			  } else {

			  }

		  }
		  
		  if(!list.contains(param) && param.length() > 0)
			  list.add(param);
	  }
	  
	  return list;
  }

}
