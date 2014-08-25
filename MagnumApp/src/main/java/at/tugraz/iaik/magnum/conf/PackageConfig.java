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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PackageConfig implements Serializable {

  private static final long serialVersionUID = -101745974456204684L;
  private final String pkg, appName;
  private boolean hooked;
  private final Set<String> unhookedClasses;
  private final HashMap<String, MethodHookConfig> methodHooks;

  public PackageConfig(String pkg, String appName, boolean hooked) {
    this.pkg = pkg;
    this.appName = appName;
    this.hooked = hooked;
    this.methodHooks = new HashMap<String, MethodHookConfig>();
    this.unhookedClasses = new HashSet<String>();
  }

  public PackageConfig(String pkg, String appName) {
    this(pkg, appName, false);
  }

  public void addMethodHookConfig(String method, MethodHookConfig conf) {
    methodHooks.put(method, conf);
  }

  public String getPkg() {
    return pkg;
  }

  public String getAppName() {
    return appName;
  }

  public boolean isHooked() {
    return hooked;
  }

  public void setHooked(boolean hooked) {
    this.hooked = hooked;
  }

  public HashMap<String, MethodHookConfig> getMethodHooks() {
    return methodHooks;
  }

  public Set<String> getUnhookedClasses() {
    return unhookedClasses;
  }

  public void addUnhookedClass(String className) {
    unhookedClasses.add(className);
  }

  public void addUnhookedClasses(Collection<String> classNames) {
    unhookedClasses.addAll(unhookedClasses);
  }

  public void removeUnhookedClass(String className) {
    unhookedClasses.remove(className);
  }

  public void removeUnhookedClasses(Collection<String> classNames) {
    unhookedClasses.removeAll(classNames);
  }
}
