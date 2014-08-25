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

import java.util.concurrent.Semaphore;

import at.tugraz.iaik.magnum.data.transport.MethodEntryTransportObject;
import at.tugraz.iaik.magnum.data.transport.MethodExitTransportObject;
import at.tugraz.iaik.magnum.data.transport.TransportObject;
import at.tugraz.iaik.magnum.model.IModelBuilder;
import at.tugraz.iaik.magnum.model.MethodInvocationModel;
import at.tugraz.iaik.magnum.model.ModelObject;

public class MethodEntryProcessor extends Processor {
  private MethodEntryTransportObject entry;
  private Semaphore blocker;
  private final IModelBuilder builder;
  private MethodInvocationModel invocationModel;
  private boolean isFinished;
  private boolean isInitialized;

  public MethodEntryProcessor(TransportObject obj, IModelBuilder builder, IExecutorManager executorManager) {
    super(executorManager);
    
    this.builder = builder;
    entry = (MethodEntryTransportObject) obj;
    
    blocker = new Semaphore(0);
    isInitialized = true;
  }

  @Override
  public boolean consumeMessage(TransportObject obj) throws InterruptedException {
    if (!isInitialized || isFinished)
      return false;
    
    if (!(obj instanceof MethodExitTransportObject))
      return false;

    MethodExitTransportObject exit = (MethodExitTransportObject) obj;
    if (exit.getIdentifier() != entry.getIdentifier())
      return false;

    invocationModel = builder.processInvocationMessage(entry, exit);
    isFinished = true;
    
    blocker.release();
    return true;
  }

  @Override
  public ModelObject finishWithTermination() throws InterruptedException {
    executorManager.rememberCallEntry(entry.getIdentifier());
    MethodInvocationModel invocationModel = builder.processTerminatedInvocationMessage(entry);
    isFinished = true;
    
    blocker.release();
    return invocationModel;
  }

  @Override
  public boolean isFinished() {
    return isFinished;
  }

  @Override
  public ModelObject callInternal() throws Exception {
    blocker.acquire();
    return invocationModel;
  }

}
