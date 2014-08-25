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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.inject.Singleton;
 
@Singleton
public class ExecutorManager implements IExecutorManager {
  private List<Processor> runningExecutors;
  private Set<Long> callEntries;
  
  public ExecutorManager() {
    runningExecutors = new CopyOnWriteArrayList<Processor>();
    callEntries = new ConcurrentSkipListSet<Long>();
  }
  
  @Override
  public void add(Processor executor) {
    runningExecutors.add(executor);
  }

  @Override
  public void remove(Processor executor) {
    runningExecutors.remove(executor);
  }

  @Override
  public Iterator<Processor> iterator() {
    return runningExecutors.iterator();
  }

  @Override
  public void rememberCallEntry(long identifier) {
    callEntries.add(identifier);
  }
  
  @Override
  public boolean callEntryExists(long identifier) {
    return callEntries.contains((Long) identifier);
  }
}
