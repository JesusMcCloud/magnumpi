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
package at.tugraz.iaik.magnum.app;

import java.util.List;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;
import at.tugraz.iaik.magnum.app.cmd.Commander;
import at.tugraz.iaik.magnum.app.ipc.ServiceSideBridge;
import at.tugraz.iaik.magnum.app.net.PcComm;
import at.tugraz.iaik.magnum.conf.Constants;
import at.tugraz.iaik.magnum.conf.PackageConfig;
import at.tugraz.iaik.magnum.conf.Preferences;
import at.tugraz.iaik.magnum.suite.R;

public class MagnumService extends Service {

  private ServiceSideBridge bridge;
  private boolean running;

  private Preferences prefs;

  public MagnumService() {
    StrictMode.enableDefaults();

    running = false;
    bridge = ServiceSideBridge.getInstance();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d("MAGNUM", "starting service");
    prefs = Preferences.getInstance(getApplicationContext());
    Commander.init(prefs);
    connect();
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {

  }

  @Override
  public IBinder onBind(Intent intent) {
    return (null);
  }

  private void displayNotification() {
    new Thread() {
      public void run() {
        List<String> interfaces;

        try {
          interfaces = (PcComm.getInstance().getIPs());

          Log.d("MAGNUM", "" + PcComm.getInstance().getPort());

          while (running) {
            if (interfaces.size() == 0) {
              final String noInterfacesWarning = "Cannot start Magnum without any Network Interfaces up!";

              Toast.makeText(getApplicationContext(), noInterfacesWarning, Toast.LENGTH_LONG).show();
            }

            for (String ip : interfaces) {
              if(ip.contains(":"))
                continue;
              Notification note = new Notification.Builder(getApplicationContext())
                  .setContentTitle(getString(R.string.app_name)).setContentText(ip)
                  .setContentInfo("" + PcComm.getInstance().getPort())
                  .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                  .setSmallIcon(R.drawable.ic_magnum).getNotification();
              note.flags |= Notification.FLAG_NO_CLEAR;

              startForeground(1337, note);
              Thread.sleep(5000);
            }
          }
        } catch (Exception e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
      }
    }.start();

  }

  private void connect() {
    Toast.makeText(getApplicationContext(), MagnumQuotes.getQuote(), Toast.LENGTH_LONG).show();
    if (running)
      return;
    running = true;

    try {
      PcComm.getInstance().start();
      for (PackageConfig pkg : prefs.getHookedPackages()) {
        Log.d(Constants.TAG, "Connecting to " + pkg);
        bridge.connect(pkg.getPkg());
      }
      displayNotification();
    } catch (Exception e) {
      Toast.makeText(getApplicationContext(), "Failed to Start Magnum Service", Toast.LENGTH_LONG).show();
    }
  }

}