/*******************************************************************************
 * Copyright 2013 Alexander Jesner, Bernd Prﾃｼnster
 * Copyright 2013, 2014 Bernd Prﾃｼnster
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
package at.tugraz.iaik.magnum.client.db;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import at.tugraz.iaik.magnum.client.cg.CallGraph;
import at.tugraz.iaik.magnum.client.util.datatypes.DBInvocation;
import at.tugraz.iaik.magnum.model.MethodInvocationModel;

public interface IDBUtil extends Runnable {

  public abstract void exit();

  public abstract void write(MethodInvocationModel invocation);

  public abstract void export(String parentDir) throws IOException;
  
  public abstract void dbImport(String dbFile) throws IOException;

  public List<DBInvocation> query(String query) throws SQLException;
  
  public List<DBInvocation> findPasswords(String clazz, String method) throws SQLException;
  
  public List<DBInvocation> findPasswords(String arguments);

  public DBInvocation getInvocation(long id) throws SQLException;

  public void updateInvocationInteresting(long invocationID, boolean interesting) throws Exception;

  public void updateInvocationReturn(MethodInvocationModel invocationModel);

  public void createInvocationTrace(List<DBInvocation> invocationList, CallGraph cg, long id, boolean direction);
  
  public void hideTimeLineItems(String column, String arg, int mode) throws SQLException;

  public abstract void dbImportDifferentDB(String dbFile);
  
  public abstract boolean isConnectionDiffSet();
  
  public abstract List<DBInvocation> getDiffInvocationBy(DBInvocation origin);

void createInvocationTraceDiff(List<DBInvocation> invocationList, CallGraph cg,
		long id, boolean direction);

}
