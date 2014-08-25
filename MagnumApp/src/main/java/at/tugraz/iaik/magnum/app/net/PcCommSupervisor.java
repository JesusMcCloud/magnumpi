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
package at.tugraz.iaik.magnum.app.net;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;
import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.hook.MessageQueue;

public class PcCommSupervisor implements Runnable {

  private ExecutorService dispatcher;
  private ServerSocket serverSocket;
  private MessageQueue queue;

  public PcCommSupervisor(ServerSocket serverSocket, MessageQueue queue) {
    this.serverSocket = serverSocket;
    this.queue = queue;

    dispatcher = Executors.newFixedThreadPool(2);
  }

  @Override
  public void run() {
    while (!Thread.interrupted()) {
      try {
        Socket sock = serverSocket.accept();

        Log.d(Constants.TAG, "Spawning PcComm writer.");
        PcCommWriter writer = new PcCommWriter(sock, queue);

        Log.d(Constants.TAG, "Spawning PcComm reader.");
        PcCommReader reader = new PcCommReader(sock);
        ArrayList<Callable<Boolean>> rw = new ArrayList<Callable<Boolean>>(2);
        rw.add(reader);
        rw.add(writer);
        dispatcher.invokeAny(rw);
        sock.close();
       // dispatcher.shutdownNow();
        
        Log.d(Constants.TAG, "Reader or Writer done");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
