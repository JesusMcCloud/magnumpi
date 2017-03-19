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
package at.tugraz.iaik.magnum.hook.ipc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.Callable;

import android.net.LocalSocket;
import android.util.Log;
import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.data.cmd.Command;
import at.tugraz.iaik.magnum.hook.cmd.Commander;
import at.tugraz.iaik.magnum.hook.data.Registry;

public class HookSideBridgeReader implements Callable<Boolean> {
  private final LocalSocket socket;

  public HookSideBridgeReader(final LocalSocket socket) {
    this.socket = socket;
  }

  @Override
  public Boolean call() throws IOException {
    start();
    return true;
  }

  private void start() throws IOException {
    try {
      ObjectInputStream objectStream = new ObjectInputStream(socket.getInputStream());

      if (!socket.isConnected())
        return;
      
      while (!Thread.interrupted() && socket.isConnected()) {
        Command buffer;

        while ((buffer = (Command) objectStream.readObject()) != null) {
          Log.d(Constants.TAG, "HOOK Rxd: " + buffer.getClass().getSimpleName());
          Commander.handleCommand(buffer);
        }
      }
      
      objectStream.close();
    } catch (Exception e) {
      Log.e("MAGNUM", "Exeption in HSBR!");
      e.printStackTrace();
      Registry.getHook().shutdown();
    }
  }
}
