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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class ConfFile {
  private static Properties  props;
  public static final String FILENAME = System.getProperty("user.home") + File.separatorChar + ".magnum.properties";
  public static final String KEY_IP   = "IP";
  public static final String KEY_PATH = "Path";

  public static String get(String key) {
    if (props == null)
      read();
    return props.getProperty(key);
  }

  public static void set(String key, String value) {
    if (props == null)
      read();
    props.setProperty(key, value);
  }

  public static void store() {
    try {
      if (props != null)
        props.store(new FileOutputStream(ConfFile.FILENAME), "Magnum Packet Inspector");
    } catch (Exception e) {

    }
  }

  private static void read() {
    if (props == null) {
      File conFile = new File(ConfFile.FILENAME);
      props = new Properties();
      props.put(ConfFile.KEY_IP, "192.168.178.49:49152");
      String userHome = System.getProperty("user.home");
      userHome = userHome == null ? "" : userHome;
      props.put(ConfFile.KEY_PATH, userHome);
      try {
        conFile.createNewFile();
        props.load(new FileInputStream(conFile));
        props.toString();
      } catch (IOException e) {
      }
    }
  }
}
