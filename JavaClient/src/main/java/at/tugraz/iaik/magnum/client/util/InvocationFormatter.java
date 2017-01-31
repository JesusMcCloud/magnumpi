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
package at.tugraz.iaik.magnum.client.util;

import at.tugraz.iaik.magnum.client.gui.widgets.tree.FormattedInstanceNodeFactory;
import at.tugraz.iaik.magnum.model.InstanceModel;
import at.tugraz.iaik.magnum.model.MethodInvocationModel;

public abstract class InvocationFormatter {

  public static String formatArgs(MethodInvocationModel invocation) {
    StringBuilder bld = new StringBuilder();
    int i = 0;
    for (InstanceModel o : invocation.getArguments()) {
      bld.append(o.getData());
      if (++i != invocation.getArguments().size())
        bld.append(" , ");
    }
    return bld.toString();
  }

  public static String formatReturn(MethodInvocationModel invocation) {
    if(invocation != null && invocation.getReturnValue() != null && invocation.getReturnValue().getData() != null )
      return invocation.getReturnValue().getData().toString();
    else
      return "";
  }
}
