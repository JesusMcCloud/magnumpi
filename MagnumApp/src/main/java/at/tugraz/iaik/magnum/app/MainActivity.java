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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import at.tugraz.iaik.magnum.suite.R;

public class MainActivity extends Activity {

  public MainActivity() {
    StrictMode.enableDefaults();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    StrictMode.enableDefaults();
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    // Remove notification bar
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    setContentView(R.layout.activity_main);
    Intent i = new Intent(getApplicationContext(), MagnumService.class);
    getApplicationContext().startService(i);
  }

  @Override
  protected void onStart() {
    super.onStart();
    new Thread() {
      public void run() {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
          public void run() {
            finish();

          }
        });
      }
    }.start();
  }

  public void onDestroy() {
    super.onDestroy();
  }

}
