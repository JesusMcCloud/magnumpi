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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class LolCat {

  /**
   * Tries to establish an ABD-Over-Network connection with the device
   * @param ip the IP-address to connect to
   * @throws IOException
   */
  public static void connectIfNecessary(String ip) throws IOException {
    Process devList = Runtime.getRuntime().exec("adb devices");
    BufferedReader brDev = new BufferedReader(new InputStreamReader(devList.getInputStream()));
    String aLine = null;
    while ((aLine = brDev.readLine()) != null) {
      if (aLine.contains("\tdevice")) {
        brDev.close();
        return;
      }
    }
    Runtime.getRuntime().exec("adb disconnect " + ip);
    Runtime.getRuntime().exec("adb connect " + ip);
  }

  /**
   * Returns a Reader to read logcat output (also clears the logcat output beforehand)
   * @return the reader
   * @throws IOException
   */
  public static BufferedReader getReader() throws IOException {
    Runtime.getRuntime().exec("adb logcat -c");
    Process lolcat = Runtime.getRuntime().exec("adb logcat");
    return new BufferedReader(new InputStreamReader(lolcat.getInputStream()));
  }
}
