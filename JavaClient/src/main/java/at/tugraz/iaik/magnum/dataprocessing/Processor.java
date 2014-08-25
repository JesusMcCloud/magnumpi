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

import java.util.concurrent.Callable;

import at.tugraz.iaik.magnum.data.transport.TransportObject;
import at.tugraz.iaik.magnum.model.ModelObject;

public abstract class Processor implements Callable<ModelObject> {
  protected final IExecutorManager executorManager;

  public Processor(IExecutorManager executorManager) {
    this.executorManager = executorManager;
    executorManager.add(this);
  }

  public abstract ModelObject callInternal() throws Exception;
  
  @Override
  public final ModelObject call() throws Exception {
    ModelObject result = callInternal();
    executorManager.remove(this);
    
    return result;
  }

  public boolean consumeMessage(TransportObject obj) throws InterruptedException {
    return false;
  }
  
  public boolean isFinished() {
    return true;
  }

  public ModelObject finishWithTermination() throws InterruptedException {
    return ModelObject.empty;
  }
}
