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
package at.tugraz.iaik.magnum.data.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.util.Log;

public class HookUnhookClassCommand extends Command {

  private static final long          serialVersionUID = 8109244219638686250L;
  private final String               pkg;
  private final Map<String, Boolean> classHooks;

  public HookUnhookClassCommand(String pkg, Map<String, Boolean> classHooks) {
    this.pkg = pkg;
    this.classHooks = classHooks;
  }

  public HookUnhookClassCommand(String pkg, Set<String> unhookedClasses, boolean hook) {
    this.pkg = pkg;
    try {
      Log.d("MAGNUM", "HUCC: " + pkg);
    } catch (NoClassDefFoundError e) {

    }
    this.classHooks = new HashMap<String, Boolean>();
    for (String unhookedClass : unhookedClasses) {
      try {
        Log.d("MAGNUM", "HUCC: " + unhookedClass);
      } catch (NoClassDefFoundError e) {
      }
      classHooks.put(unhookedClass, hook);
    }
  }

  public String getPkg() {
    return pkg;
  }

  public Map<String, Boolean> getUnhookedClasses() {
    return classHooks;
  }

  public String toString() {
    StringBuilder bld = new StringBuilder(this.getClass().getName());
    bld.append(":\nClassHooks:\n");
    for (String clazz : classHooks.keySet()) {
      bld.append(clazz).append(": ").append(classHooks.get(clazz));
    }
    return bld.toString();
  }

}
