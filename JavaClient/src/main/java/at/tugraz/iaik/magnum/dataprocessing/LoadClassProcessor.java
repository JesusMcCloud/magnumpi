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

import at.tugraz.iaik.magnum.data.transport.DonePatchingClassTransportObject;
import at.tugraz.iaik.magnum.data.transport.LoadedClassTransportObject;
import at.tugraz.iaik.magnum.data.transport.MethodHookTransportObject;
import at.tugraz.iaik.magnum.data.transport.TransportObject;
import at.tugraz.iaik.magnum.model.ClassModel;
import at.tugraz.iaik.magnum.model.IModelBuilder;
import at.tugraz.iaik.magnum.model.MethodModel;
import at.tugraz.iaik.magnum.model.ModelObject;

public class LoadClassProcessor extends Processor {
  private ClassModel classModel;
  private Semaphore blocker;
  private IModelBuilder builder;
  private boolean isFinished;
  private LoadedClassTransportObject lcto;
  private boolean isInitialized;

  LoadClassProcessor(TransportObject obj, IModelBuilder builder, IExecutorManager runningExecutors) {
    super(runningExecutors);
    
    lcto = (LoadedClassTransportObject) obj;
    classModel = builder.processLoadClassMessage(lcto);

    this.builder = builder;
    
    blocker = new Semaphore(0);
    
    isInitialized = true;
  }

  @Override
  public boolean consumeMessage(TransportObject obj) {
    if (!isInitialized || isFinished)
      return false;
    
    if (obj instanceof MethodHookTransportObject) {
      MethodHookTransportObject mhto = (MethodHookTransportObject) obj;

      if (!mhto.getClassName().equals(classModel.getClassName()))
        return false;
      
      MethodModel methodModel = builder.processHookMethodMessage(mhto);
      classModel.addMethod(methodModel);
      
      return true;
    } 

    if (obj instanceof DonePatchingClassTransportObject) {
      DonePatchingClassTransportObject dpcto = (DonePatchingClassTransportObject) obj;

      if (!dpcto.getClassName().equals(classModel.getClassName()))
        return false;

      isFinished = true;
      
      blocker.release();
      return true;
    }
    
    return false;
  }

  @Override
  public boolean isFinished() {
    return isFinished;
  }

  @Override
  public ModelObject callInternal() throws Exception {
    blocker.acquire();
    return classModel;
  }
}
