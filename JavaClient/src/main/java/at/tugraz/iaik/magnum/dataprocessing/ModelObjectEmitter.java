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
package at.tugraz.iaik.magnum.dataprocessing;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import at.tugraz.iaik.magnum.model.LogMessageModel;
import at.tugraz.iaik.magnum.model.ModelObject;

public class ModelObjectEmitter implements Runnable {

  private final Future<ModelObject> future;
  private final IEmitListenerRegistry emitListenerRegistry;
  private final IExecutorManager runningExecutors;
  private final Processor executor;

  public ModelObjectEmitter(Processor executor, final Future<ModelObject> future, IEmitListenerRegistry registry, IExecutorManager runningExecutors) {
    this.executor = executor;
    this.future = future;
    this.emitListenerRegistry = registry;
    this.runningExecutors = runningExecutors;
  }

  @Override
  public void run() {
    ModelObject model = null;
    
    try {
      model = future.get(20, TimeUnit.SECONDS);
      
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      model = new LogMessageModel(e.getMessage());
      
    } catch (TimeoutException e) {
      System.out.println("Time-out for exec: " + executor);
      try {
        model = executor.finishWithTermination();
      } catch (InterruptedException ie) {
        model = ModelObject.empty;
      }        
      runningExecutors.remove(executor);
      
    } finally {
      emitListenerRegistry.emit(model);
    }
  }
}
