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

import java.util.Set;

import at.tugraz.iaik.magnum.conf.PackageConfig;

public abstract class CommandBuilder {

  public static HookUnhookPackageCommand buildForHookUnhookCmd(String pkgName, boolean hook) {
    return new HookUnhookPackageCommand(pkgName, hook);
  }

  public static BWListCommand buildForBWListCmd(final boolean pureWhiteList, final Set<String> packages,
      final Set<String> packagesWildcards, final Set<String> classes, final Set<String> classesWildcards,
      final Set<String> methods, final Set<String> methodsWildcards) {
    return new BWListCommand(pureWhiteList, packages, packagesWildcards, classes, classesWildcards, methods,
        methodsWildcards);
  }

  public static RequestPackageConfigCommand buildforRequestPackageConfigCommand() {
    return new RequestPackageConfigCommand();
  }

  public static HookUnhookMethodCommand buildForHookUnhookMethodCommand(PackageConfig pkgConf) {
    return new HookUnhookMethodCommand(pkgConf.getPkg(), pkgConf.getMethodHooks().values());
  }

  public static HookUnhookClassCommand buildForHookUnhookClassCommand(PackageConfig pkgConf) {
    return new HookUnhookClassCommand(pkgConf.getPkg(), pkgConf.getUnhookedClasses(), true);
  }

  public static HookUnhookClassCommand buildForHookUnhookClassCommand(String pkg, Set<String> classesToUnhook,boolean hook) {
    return new HookUnhookClassCommand(pkg, classesToUnhook, hook);
  }
}
