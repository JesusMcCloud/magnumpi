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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Member;
import java.util.Map;

import at.tugraz.iaik.magnum.conf.PackageConfig;

public abstract class TransportObjectBuilder {

  public static TransportObject buildForLogMessage(final String format, final String... params) {
    return new LogMessageTransportObject(format, params);
  }

  public static TransportObject buildForPackageConfig(final Map<String, PackageConfig> packageConfig) {
    return new PackageConfigTransportObject(packageConfig);
  }

  public static TransportObject buildForMethodEntry(final Member method, final Object[] params, long identifier, long prevID, boolean callerKnown) {
    return new MethodEntryTransportObject(method, params, identifier, prevID, callerKnown);
  }

  public static TransportObject buildForMethodExit(final Member method, final Object result, Long identifier) {
    return new MethodExitTransportObject(method, result, identifier);
  }

  public static TransportObject buildForApkFile(final String apkPath, final String packageName) {
    byte[] data = null;

    try {
      File file = new File(apkPath);
      FileInputStream stream = new FileInputStream(file);
      data = new byte[(int) file.length()];
      stream.read(data);
      stream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new ApkFileTransportObject(data, packageName);
  }

  public static TransportObject buildForLoadClass(final Class<?> clazz) {
    return new LoadedClassTransportObject(clazz);
  }

  public static TransportObject buildForMethodHook(final Member method) {
    return new MethodHookTransportObject(method);
  }

  public static TransportObject buildForDonePatchingClass(final String loadedClass) {
    return new DonePatchingClassTransportObject(loadedClass);
  }
}