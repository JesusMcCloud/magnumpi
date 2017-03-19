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

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.StrictMode;
import android.util.Log;
import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.data.transport.TransportObject;
import at.tugraz.iaik.magnum.hook.MessageQueue;

public class PcComm {

  final private MessageQueue queue;
  private static PcComm instance;
  private ServerSocket serverSocket;
  private ExecutorService supervisor;

  public static PcComm getInstance() throws Exception {
    if (instance == null)
      instance = new PcComm();

    return instance;
  }

  private PcComm() throws Exception {
    StrictMode.enableDefaults();
    queue = new MessageQueue();
    serverSocket = getFreeSocket();
    supervisor = Executors.newSingleThreadExecutor();
  }

  public int getPort() {
    return serverSocket.getLocalPort();
  }

  public void start() {
    supervisor.execute(new PcCommSupervisor(serverSocket, queue));
  }

  public void write(TransportObject msg) {
    queue.put(msg);
  }

  private ServerSocket getFreeSocket() throws Exception {
    ServerSocket socket = null;
    int port = 49152;

    while (socket == null && port <= 65535) {
      try {
        socket = new ServerSocket(port);
        Log.d(Constants.TAG, "Running server on " + socket.getLocalSocketAddress() + ":" + socket.getLocalPort());
        return socket;
      } catch (IOException e) {
        ++port;
      }
    }

    throw new Exception("Out of ports. No can do.");
  }

  public List<String> getIPs() {
    try {
      List<String> ips = new LinkedList<String>();

      ArrayList<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

      for (NetworkInterface intf : interfaces) {
        ArrayList<InetAddress> addresses = Collections.list(intf.getInetAddresses());

        for (InetAddress addr : addresses) {
          if (!addr.isLoopbackAddress())
            ips.add(addr.getHostAddress());
        }
      }

      return ips;
    } catch (Exception ex) {
      return new ArrayList<String>();
    }
  }
}
