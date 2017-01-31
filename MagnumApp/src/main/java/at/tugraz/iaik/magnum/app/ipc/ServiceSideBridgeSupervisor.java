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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.StrictMode;
import android.util.Log;
import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.conf.Preferences;
import at.tugraz.iaik.magnum.data.cmd.Command;
import at.tugraz.iaik.magnum.data.cmd.CommandBuilder;

public class ServiceSideBridgeSupervisor implements Callable<Boolean> {
  private LocalServerSocket                  socket;
  private LocalSocket                        sock;
  private ExecutorService                    dispatcher;
  private final LinkedBlockingQueue<Command> commands;
  private boolean                            connected;
  private boolean                            running;
  private final String                       packageName;
  private BridgeReader                       reader;
  private BridgeWriter                       writer;

  public ServiceSideBridgeSupervisor(String packageName) {
    StrictMode.enableDefaults();

    running = false;
    commands = new LinkedBlockingQueue<Command>();
    this.packageName = packageName;
  }

  public Boolean call() {
    dispatcher = Executors.newFixedThreadPool(2);
    start();
    return true;
  }

  public void execute(Command cmd) {
    if (!connected)
      return;

    ////Log.d("MAGNUM", "SSBSupervisor: trying to execute " + cmd);
    commands.add(cmd);
  }

  public void start() {
    running = true;

    while (!Thread.currentThread().isInterrupted() && running) {
      try {

        socket = new LocalServerSocket(Constants.NAME_SOCKET + "." + packageName);
        sock = socket.accept();

        if (!running)
          // Socket was killed in disconnect()
          return;

        reader = new BridgeReader(sock, packageName);
        writer = new BridgeWriter(sock, commands, packageName);
        connected = true;

        HashSet<String> hookedPkgs = new HashSet<String>();
        hookedPkgs.add(packageName);
        execute(Preferences.getInstance(null).getGlobalHooks());
        execute(CommandBuilder.buildForHookUnhookMethodCommand(Preferences.getInstance(null).getPackageConfig()
            .get(packageName)));
        execute(CommandBuilder.buildForHookUnhookClassCommand(Preferences.getInstance(null).getPackageConfig()
            .get(packageName)));

        ArrayList<Callable<Boolean>> rw = new ArrayList<Callable<Boolean>>(2);
        rw.add(reader);
        rw.add(writer);
        dispatcher.invokeAny(rw);

      } catch (Exception e) {
        e.printStackTrace();
        try {
          if (sock != null)
            sock.close();
        } catch (IOException e1) {
        }
        try {
          if (socket != null)
            socket.close();
        } catch (IOException e1) {
        }
      }

      connected = false;
    }
  }

  public void disconnect() {
    if (running) {
      ////Log.d(Constants.TAG, "SSB SHutdown");
      running = false;
      connected = false;

      try {
        if (sock != null)
          sock.close();
      } catch (IOException e) {
      }

      LocalSocket tempSocket = null;
      try {
        // localSeverSocket is broken!
        // We need to trigger a new connection to the socket
        // to release the blocking accept() in our connect above.
        tempSocket = new LocalSocket();
        tempSocket.connect(new LocalSocketAddress(Constants.NAME_SOCKET + "." + packageName));

        if (socket != null)
          socket.close();
      } catch (IOException e) {
      } finally {
        try {
          if (tempSocket != null)
            tempSocket.close();
        } catch (IOException e) {
        }
      }

      dispatcher.shutdownNow();
      ////Log.d(Constants.TAG, "SSB Shutdown done: " + dispatcher.isTerminated());
    }
  }
}
