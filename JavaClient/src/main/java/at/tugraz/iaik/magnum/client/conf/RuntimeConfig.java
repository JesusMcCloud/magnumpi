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
package at.tugraz.iaik.magnum.client.conf;

import java.util.Map;

import at.tugraz.iaik.magnum.conf.PackageConfig;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RuntimeConfig implements IRuntimeConfig {

  private Map<String, PackageConfig> packageConfig;
  private String                     currentPkg;

  @Inject
  public RuntimeConfig() {
  }

  @Override
  public void setPackageConfig(Map<String, PackageConfig> conf) {
    packageConfig = conf;
  }

  @Override
  public PackageConfig getPackageConfig(String pkg) {
    return packageConfig.get(pkg);
  }

  @Override
  public String getCurrentPackage() {
    return currentPkg;
  }

  @Override
  public void setCurrentPackage(String pkg) {
    currentPkg = pkg;
  }

}
