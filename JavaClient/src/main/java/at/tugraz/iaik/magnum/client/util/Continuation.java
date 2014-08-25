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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class Continuation<T> {
  private static ExecutorService dispatcher = Executors.newCachedThreadPool();
  
  public final void continueWithResult(final T result) {
    dispatcher.execute(new Runnable() {
      @Override
      public void run() {
        onContinue(result);
      }
    });
  }
  
  public final void asyncContinueWithResult(final Callable<T> result) {
    dispatcher.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Future<T> future = dispatcher.submit(result);
          onContinue(future.get());
        } catch (InterruptedException | ExecutionException e) {
          continueWithError();
        }
      }
    });
  }
  
  public final void continueWithError() {
    dispatcher.execute(new Runnable() {
      @Override
      public void run() {
        onError();
      }
    });
  }

  public abstract void onContinue(T result);
  
  public abstract void onError();
}