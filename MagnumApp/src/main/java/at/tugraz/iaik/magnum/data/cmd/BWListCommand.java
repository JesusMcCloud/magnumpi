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

public class BWListCommand extends Command {
  private static final long serialVersionUID = -5178286133046571360L;

  private final boolean     pureWhiteList;
  private final Set<String> packages;
  private final Set<String> packagesWildcards;
  private final Set<String> classes;
  private final Set<String> classesWhildcards;
  private final Set<String> methods;
  private final Set<String> methodWildcards;

  public BWListCommand(final boolean pureWhiteList, final Set<String> packages, final Set<String> packagesWildcards,
      final Set<String> classes, final Set<String> classesWildcards, final Set<String> methods,
      final Set<String> methodWildcards) {
    this.pureWhiteList = pureWhiteList;
    this.packages = packages;
    this.packagesWildcards = packagesWildcards;
    this.classes = classes;
    this.classesWhildcards = classesWildcards;
    this.methodWildcards = methodWildcards;
    this.methods = methods;
  }

  public boolean isPureWhiteList() {
    return pureWhiteList;
  }

  public Set<String> getPackages() {
    return packages;
  }

  public Set<String> getClasses() {
    return classes;
  }

  public Set<String> getMethods() {
    return methods;
  }

  public Set<String> getPackagesWildCards() {
    return packagesWildcards;
  }

  public Set<String> getClassesWildcards() {
    return classesWhildcards;
  }

  public Set<String> getMethodWildcards() {
    return methodWildcards;
  }
}
