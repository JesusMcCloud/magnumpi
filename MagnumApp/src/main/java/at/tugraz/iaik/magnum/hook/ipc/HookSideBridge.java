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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.data.cmd.Command;
import at.tugraz.iaik.magnum.hook.MessageQueue;
import at.tugraz.iaik.magnum.hook.cmd.Commander;

public class HookSideBridge implements Constants {

  private boolean running = false;
  private LocalSocket socket;
  private final MessageQueue messageQueue;
  private ExecutorService dispatcher;
  private HookSideBridgeSupervisor supervisor;

  public HookSideBridge(final MessageQueue queue) {
    messageQueue = queue;
    dispatcher = Executors.newSingleThreadExecutor();
  }

  public void retreiveInitCommand(String pkgName) throws IOException, ClassNotFoundException {
    socket = new LocalSocket();
    Log.d(TAG, "Trying to Connect to: " + NAME_SOCKET + "." + pkgName);
    socket.connect(new LocalSocketAddress(NAME_SOCKET + "." + pkgName));
    // InitHookCommand cmd = (InitHookCommand) new
    // ObjectInputStream(socket.getInputStream()).readObject();
    // Commander.handleCommand(cmd);
    supervisor = new HookSideBridgeSupervisor(messageQueue, socket, pkgName);
  }

  public void connect() {
    if (running)
      return;
    
    dispatcher.submit(supervisor);
    running = true;
  }

  public void disconnect() {
    Log.d(DEBUG, "HookSideBridge DISCONNECT!");
    if (!running) 
      return;
    
    running = false;
    supervisor.disconnect();
    dispatcher.shutdownNow();
  }
}
