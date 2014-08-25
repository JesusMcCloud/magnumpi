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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.os.StrictMode;
import android.util.Log;
import at.tugraz.iaik.magnum.data.cmd.Command;

public class ServiceSideBridge {

  private class Connection {
    ServiceSideBridgeSupervisor supervisor;

    Connection(ServiceSideBridgeSupervisor supervisor, Future<Boolean> future) {
      this.supervisor = supervisor;
    }
  }

  private boolean connected = false;
  private ExecutorService dispatcher;
  private Map<String, Connection> connections;

  private static ServiceSideBridge instance = null;

  public static ServiceSideBridge getInstance() {
    if (instance == null)
      instance = new ServiceSideBridge();
    return instance;
  }

  private ServiceSideBridge() {
    StrictMode.enableDefaults();
    dispatcher = Executors.newCachedThreadPool();
    connections = new ConcurrentHashMap<String, Connection>();
  }

  public synchronized void execute(Command cmd) {
    if (!connected)
      return;

    Log.d("MAGNUM", "ServiceSideBridge: trying to exec: " + cmd);
    for (Connection connection : connections.values())
      connection.supervisor.execute(cmd);
  }

  public synchronized void connect(String packageName) {
    connected = true;
    if (connections.containsKey(packageName))
      return;

    ServiceSideBridgeSupervisor con = new ServiceSideBridgeSupervisor(packageName);
    Future<Boolean> future = dispatcher.submit(con);

    connections.put(packageName, new Connection(con, future));
  }

  public synchronized void disconnect(String packageName) {
    if (!connections.containsKey(packageName))
      return;

    Connection con = connections.remove(packageName);

    if (con != null) {
      con.supervisor.disconnect();
      // con.future.cancel(true);
    }
  }

  public synchronized void disconnect() {
    connected = false;

    for (String pkgName : connections.keySet()) {
      disconnect(pkgName);
    }

    connected = false;
  }
}
