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
package at.tugraz.iaik.magnum.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import at.tugraz.iaik.magnum.client.db.DBUtil;
import at.tugraz.iaik.magnum.client.db.IDBUtil;
import at.tugraz.iaik.magnum.data.transport.LoadedClassTransportObject;
import at.tugraz.iaik.magnum.data.transport.MethodEntryTransportObject;
import at.tugraz.iaik.magnum.data.transport.MethodExitTransportObject;
import at.tugraz.iaik.magnum.data.transport.MethodHookTransportObject;
import at.tugraz.iaik.magnum.dataprocessing.IMoustacheClassLoader;

import com.google.inject.Inject;

public class ModelBuilder implements IModelBuilder {
  private final Map<String, ClassModel> knownClasses;
  private final Map<String, MethodModel> knownMethods;
  private final Map<String, MethodModel> intermediateMethodModels;
  private final IMoustacheClassLoader moustacheClassLoader;
  private final IDBUtil dbUtil;
  
  
  @Inject
  public ModelBuilder(IMoustacheClassLoader moustacheClassLoader, DBUtil dbUtil) {
    this.moustacheClassLoader = moustacheClassLoader;
    this.dbUtil=dbUtil;
    knownClasses = new ConcurrentHashMap<>();
    knownMethods = new ConcurrentHashMap<>();
    intermediateMethodModels = new ConcurrentHashMap<>();
  }

  @Override
  public ClassModel processLoadClassMessage(LoadedClassTransportObject lc) {
    String className = lc.getClassName();

    if (knownClasses.containsKey(className))
      return knownClasses.get(className);

    int modifiers = lc.getModifiers();
    
    ClassModel classModel = new ClassModel(className, modifiers);
    knownClasses.put(className, classModel);

    return classModel;
  }

  @Override
  public MethodModel processHookMethodMessage(MethodHookTransportObject mh) {
    String methodName = mh.getMethodName();

    String uniqueMethodName = mh.getUniqueMethodName();
    if (knownMethods.containsKey(uniqueMethodName))
      return knownMethods.get(uniqueMethodName);

    String[] parameterTypeNames = mh.getParameterTypes();
    List<TypeModel> parameterTypes = new ArrayList<>();
    for (String type : parameterTypeNames)
      parameterTypes.add(new TypeModel(type));

    String returnTypeName = mh.getReturnType();
    TypeModel returnType = new TypeModel(returnTypeName);

    int modifiers = mh.getModifiers();

    MethodModel methodModel = new MethodModel(methodName, parameterTypes, returnType, modifiers);
    knownMethods.put(uniqueMethodName, methodModel);

    if (intermediateMethodModels.containsKey(uniqueMethodName)) {
      MethodModel intermediateMethodModel = intermediateMethodModels.get(uniqueMethodName);
      Collection<MethodInvocationModel> invocations = intermediateMethodModel.getInvocations();

      for (MethodInvocationModel invocation : invocations) {
        invocation.setMethodModel(methodModel);
        methodModel.addInvocation(invocation);
      }

      intermediateMethodModels.remove(uniqueMethodName);
    }

    return methodModel;
  }

  @Override
  public MethodInvocationModel processInvocationMessage(MethodEntryTransportObject entry,
      MethodExitTransportObject exit) throws InterruptedException {
    
    String methodName = entry.getMethodName();
    String uniqueMethodName = entry.getUniqueMethodName();

    MethodModel methodModel = knownMethods.get(uniqueMethodName);

    if (methodModel == null) {
      methodModel = new IntermediateMethodModel(methodName);
      intermediateMethodModels.put(uniqueMethodName, methodModel);
    }

    InstanceModel returnValue = new NullInstanceModel();
    boolean didReturn = false;
    
    long caller=0;
    boolean callerKnown=false;
    if(entry!=null)
      callerKnown=entry.isCallerKnwon();
      caller=entry.getCaller();

    // We cannot create InstanceModels when the APK 
    // was not loaded yet.
    moustacheClassLoader.waitForInitialization();
    
    if (exit != null) {
      byte[] returnData = exit.getTransportBuffer();
      returnValue = new InstanceModel(methodModel.getReturnType(), returnData);
      didReturn = true;
    }

    List<InstanceModel> arguments = new ArrayList<>();
    List<TypeModel> parameterTypes = methodModel.getParameterTypes();

    for (int i = 0; i < parameterTypes.size(); ++i)
      arguments.add(new InstanceModel(parameterTypes.get(i), entry.getTransportBuffer(i)));

    long timestamp = entry.getTimestamp();
    long callId = entry.getIdentifier();
    
    MethodInvocationModel invocation = new MethodInvocationModel(methodModel, arguments, returnValue, timestamp, callId, didReturn, caller, callerKnown);
    methodModel.addInvocation(invocation);

    return invocation;
  }

  @Override
  public MethodInvocationModel processTerminatedInvocationMessage(MethodEntryTransportObject entry) throws InterruptedException {
    return processInvocationMessage(entry, null);
  }

  @Override
  public void processDeferredInvocationExitMessage(MethodExitTransportObject exit) {
    String uniqueMethodName = exit.getUniqueMethodName();
    MethodModel methodModel = knownMethods.get(uniqueMethodName);
    
    if (methodModel == null)
      // Drop the entire exit message if
      // no entry was ever present.
      return;
    
    MethodInvocationModel invocationModel = methodModel.getInvocation(exit.getIdentifier());
    TypeModel typeModel = new TypeModel(exit.getReturnType());
    
    dbUtil.updateInvocationReturn(invocationModel);
    
    invocationModel.setDeferredReturnInfo(typeModel, exit.getTransportBuffer());
  }
}
