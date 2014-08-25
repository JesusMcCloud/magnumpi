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
package at.tugraz.iaik.magnum.app.ipc;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.net.LocalSocket;
import android.util.Log;
import at.tugraz.iaik.magnum.data.cmd.Command;

public class BridgeWriter implements Callable<Boolean> {
  private final LocalSocket socket;
  private final LinkedBlockingQueue<Command> commands;
  private String packageName;

  public BridgeWriter(final LocalSocket socket, LinkedBlockingQueue<Command> commands, String packageName) {
    this.socket = socket;
    this.commands = commands;
    this.packageName = packageName;
  }

  @Override
  public Boolean call() throws IOException {
    start();
    return true;
  }

  public void forward(Command cmd) {
    try {
      commands.put(cmd);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  private void start() throws IOException {
    try {
      ObjectOutputStream objectStream = new ObjectOutputStream(socket.getOutputStream());

      while (!Thread.interrupted()) {
        try {
          Command cmd = commands.poll(200, TimeUnit.MILLISECONDS);

          if (cmd == null)
            continue;

          Log.d("MAGNUM", "BridgeWriter: received cmd, forwarding it. " + cmd);
          objectStream.writeObject(cmd);
          objectStream.flush();
        } catch (InterruptedException e) {
          Log.d("MAGNUM", "BridgeWriter: was interrupted in commands.poll()");
          Thread.currentThread().interrupt();
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      ServiceSideBridge.getInstance().disconnect(packageName);
      
    }
  }
}
