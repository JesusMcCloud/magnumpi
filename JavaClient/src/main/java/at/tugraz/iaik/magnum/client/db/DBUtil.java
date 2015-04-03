/*******************************************************************************
 * Copyright 2013 Alexander Jesner, Bernd Prﾃθ津つｼnster
 * Copyright 2013, 2014 Bernd Prﾃθ津つｼnster
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import at.tugraz.iaik.magnum.client.cg.CallGraph;
import at.tugraz.iaik.magnum.client.util.InvocationFormatter;
import at.tugraz.iaik.magnum.client.util.datatypes.DBInvocation;
import at.tugraz.iaik.magnum.model.MethodInvocationModel;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DBUtil implements IDBUtil {
  private Connection                             conn;
  private Connection                             connDiff;
  private LinkedBlockingQueue<PreparedStatement> queue;
  private boolean                                running;

  private String                                 tmpFile;

  @Inject
  public DBUtil() {
    try {
    	
      connDiff = null;
      
      File tmp = File.createTempFile("magnum", ".tmp");
      tmpFile = tmp.getCanonicalPath();
      tmp.delete();
      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:" + tmp.getCanonicalPath());
      synchronized (conn) {

        queue = new LinkedBlockingQueue<>();
        // CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))
        conn.createStatement()
            .execute(
                "CREATE TABLE IF NOT EXISTS invocations(ID IDENTITY PRIMARY KEY, INTERESTING BOOLEAN, CLASS TEXT, METHOD TEXT, ARGUMENTS TEXT, RETURN_VALUE TEXT, TIMESTAMP TIMESTAMP, UMN TEXT, CALLER LONG, CALLER_KNOWN BOOLEAN, HIDE BOOLEAN DEFAULT 0)");
        running = true;
      }
    } catch (ClassNotFoundException | SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public void dbImport(String dbFile)
  {
	  
	  try {
		Thread.currentThread().interrupt();
		conn.close();  
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
		
		PreparedStatement stmt = conn.prepareStatement("select sql from sqlite_master where tbl_name = 'invocations' and sql like '%hide%'"); // and sql like 'hide' ");
		boolean doAlter = true;
		
		synchronized (this) {
		      ResultSet rs = stmt.executeQuery();
		      
		      while (rs.next()) {
		    	  doAlter = false;
		      }
		      
		      if(doAlter)
		      {
		    	  PreparedStatement alterStmt = conn.prepareStatement("alter table 'invocations' add column HIDE BOOLEAN DEFAULT 0");
		    	  alterStmt.execute();
		      }
		      
		      /*
		      PreparedStatement stmt1 = conn.prepareStatement("select sql from sqlite_master where tbl_name = 'invocations' and sql like '%hide%'"); // and sql like 'hide' ");
		      ResultSet rs1 = stmt1.executeQuery();
		      while (rs1.next()) {
		    	  System.out.println(rs1.getString(1));
		      } */
			}

		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
  
  public void dbImportDifferentDB(String dbFile)
  {
	  try {
	    Class.forName("org.sqlite.JDBC");
		connDiff = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
	} catch (ClassNotFoundException | SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
  }

  public void updateInvocationInteresting(long invocationID, boolean interesting) throws Exception {
    PreparedStatement stmt = conn.prepareStatement("UPDATE invocations SET INTERESTING=? WHERE ID=?");
    stmt.setBoolean(1, interesting);
    stmt.setLong(2, invocationID);
    queue.add(stmt);
  }

  @Override
  public List<DBInvocation> query(String query) throws SQLException {
    PreparedStatement stmt = conn.prepareStatement(query);
    List<DBInvocation> invocatons = new LinkedList<>();
    synchronized (this) {
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        invocatons.add(new DBInvocation(rs.getLong(1), rs.getBoolean(2), rs.getString(3), rs.getString(4), rs
            .getString(5), rs.getString(6), rs.getTimestamp((7)), rs.getString(8)));
      }
      return invocatons;
    }
  }

  @Override
  public DBInvocation getInvocation(long id) throws SQLException {
    PreparedStatement stmt = conn.prepareStatement("SELECT * from invocations WHERE ID=?");
    stmt.setLong(1, id);
    synchronized (this) {
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        // TODO: check columns
        return (new DBInvocation(rs.getLong(1), rs.getBoolean(2), rs.getString(3), rs.getString(4), rs.getString(5),
            rs.getString(6), rs.getTimestamp((7)), rs.getString(8)));
      }
      throw new SQLException("FUBAR");
    }
  }
  
  public void hideTimeLineItems(String column, String arg, int mode) throws SQLException {
	  
    PreparedStatement stmt = null;
    
    switch (mode) {
    case 0:
    	stmt = conn.prepareStatement("UPDATE invocations SET hide =1 WHERE NOT " + column + "=?");
    	stmt.setString(1, arg);
    	break;
    case 1:
    	stmt = conn.prepareStatement("UPDATE invocations SET hide =1 WHERE " + column + "=?");
    	stmt.setString(1, arg);
    	break;
    case 2:
    	stmt = conn.prepareStatement("UPDATE invocations SET hide =0");
    	break;
    default:
    	throw new SQLException("Mode not available");
    }
    
    synchronized (this) {
    	stmt.execute();
    }
  }

  @Override
  public void write(MethodInvocationModel invocation) {

    try {
      PreparedStatement stmt = conn.prepareStatement("INSERT INTO invocations VALUES(?,?,?,?,?,?,?,?,?,?,?)");
      stmt.setLong(1, invocation.getCallId());
      stmt.setBoolean(2, false);
      stmt.setString(3, invocation.getMethodModel().getClassName());
      stmt.setString(4, invocation.getMethodModel().getSimpleMethodName());
      stmt.setString(5, InvocationFormatter.formatArgs(invocation));
      stmt.setString(6, InvocationFormatter.formatReturn(invocation));
      stmt.setDate(7, new Date(invocation.getInvocationTime()));
      stmt.setString(8, invocation.getMethodModel().getUniqueMethodName());
      stmt.setLong(9, invocation.getCallerId());
      stmt.setBoolean(10, invocation.isCallerKnown());
      stmt.setBoolean(11, false);
      queue.add(stmt);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void exit() {
    try {
      Thread.currentThread().interrupt();

      conn.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void export(String parentDir) throws IOException {

    FileChannel fin = null;
    FileChannel fout = null;

    try {
      fin = new FileInputStream(tmpFile).getChannel();
      fout = new FileOutputStream(parentDir + File.separator + "magnum.sqlite").getChannel();
      fout.transferFrom(fin, 0, fin.size());
    } finally {
      fin.close();
      fout.close();
    }

  }

  @Override
  public void run() {
    try {
      while (running) {
        try {
          PreparedStatement stmt = queue.take();
          synchronized (this) {
            stmt.execute();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void updateInvocationReturn(MethodInvocationModel invocation) {
    try {
      PreparedStatement stmt = conn.prepareStatement("UPDATE invocations SET RETURN_VALUE=? WHERE ID=?");
      stmt.setString(1, InvocationFormatter.formatReturn(invocation));
      stmt.setLong(2, invocation.getCallId());
      queue.add(stmt);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  
  @Override
  public List<DBInvocation> findPasswords(String clazz, String method) {
	  
	  List<DBInvocation> dbList = new ArrayList<DBInvocation>();
	  
	  synchronized (this) {
		  try {

	          PreparedStatement stmt;
	          stmt = conn.prepareStatement("SELECT * FROM invocations WHERE CLASS like ? AND METHOD like ?");
	          stmt.setString(1, "%" + clazz + "%");
	          stmt.setString(2, "%" + method + "%");
	          
	          ResultSet rs = stmt.executeQuery();
	          
	          while (rs.next()) {
	        	  dbList.add(new DBInvocation(rs.getLong(1), rs.getBoolean(2), rs.getString(3),
	                  rs.getString(4), rs.getString(5), rs.getString(6), rs.getTimestamp(7), rs.getString(8), rs.getLong(9),
	                  rs.getBoolean(10)));
	          }
	          
		  }  catch (SQLException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
		  }
	  }
	  
	  return dbList;  
  }
  
  @Override
  public List<DBInvocation> findPasswords(String argument) {
	  
	  List<DBInvocation> dbList = new ArrayList<DBInvocation>();
	  
	  synchronized (this) {
		  try {

	          PreparedStatement stmt;
	          stmt = conn.prepareStatement("SELECT * FROM invocations WHERE ARGUMENTS LIKE ? OR RETURN_VALUE LIKE ? ORDER BY timestamp DESC");
	          stmt.setString(1, "%" + argument + "%");
	          stmt.setString(2, "%" + argument + "%");
	          
	          ResultSet rs = stmt.executeQuery();
	          
	          while (rs.next()) {
	        	  dbList.add(new DBInvocation(rs.getLong(1), rs.getBoolean(2), rs.getString(3),
	                  rs.getString(4), rs.getString(5), rs.getString(6), rs.getTimestamp(7), rs.getString(8), rs.getLong(9),
	                  rs.getBoolean(10)));
	          }
	          
		  }  catch (SQLException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
		  }
	  }
	  
	  return dbList;  
  }

  @Override
  public void createInvocationTrace(List<DBInvocation> invocationList, CallGraph cg, long id, boolean direction) {
	  
    long callingID = id;
    
    synchronized (this) {
      int depth = 0;
      boolean callerKnown = false;

      outer: do {

        try {
          PreparedStatement stmt;
          if (direction) {
            stmt = conn.prepareStatement("SELECT * FROM invocations WHERE ID=?");
            stmt.setLong(1, id);
          } else {
            stmt = conn.prepareStatement("SELECT * FROM invocations WHERE CALLER_KNOWN=? AND CALLER=?");
            stmt.setBoolean(1, true);
            stmt.setLong(2, id);
          }
          ResultSet rs = stmt.executeQuery();

          while (rs.next()) {
            DBInvocation invocation = new DBInvocation(rs.getLong(1), rs.getBoolean(2), rs.getString(3),
                rs.getString(4), rs.getString(5), rs.getString(6), rs.getTimestamp(7), rs.getString(8), rs.getLong(9),
                rs.getBoolean(10));
            callerKnown = invocation.isCallerKnown();
            
            if(invocationList != null) 
            	invocationList.add(invocation);
            
            cg.setInvocation(invocation.getId(), invocation);
            
            if (!direction) {
            	
            	cg.addCallRelation(id, invocation.getId(), invocation, id == callingID);

            } else {
              if (callerKnown) {
            	  
            	cg.addCallRelation(invocation.getCallerID(), invocation.getId(), invocation, id == callingID);
                id = invocation.getCallerID();
              }
            }
          }
          if (!direction)
            break outer;
        } catch (SQLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
          callerKnown = false;
        }
      } while (callerKnown && ++depth < 30);
      cg.getNodes().get(id).setIndividual(true);
    }

  }
  
  @Override
  public void createInvocationTraceDiff(List<DBInvocation> invocationList, CallGraph cg, long id, boolean direction) {
	  
    long callingID = id;
    
    synchronized (this) {
      int depth = 0;
      boolean callerKnown = false;

      outer: do {

        try {
          PreparedStatement stmt;
          if (direction) {
            stmt = connDiff.prepareStatement("SELECT * FROM invocations WHERE ID=?");
            stmt.setLong(1, id);
          } else {
            stmt = connDiff.prepareStatement("SELECT * FROM invocations WHERE CALLER_KNOWN=? AND CALLER=?");
            stmt.setBoolean(1, true);
            stmt.setLong(2, id);
          }
          ResultSet rs = stmt.executeQuery();

          while (rs.next()) {
            DBInvocation invocation = new DBInvocation(rs.getLong(1), rs.getBoolean(2), rs.getString(3),
                rs.getString(4), rs.getString(5), rs.getString(6), rs.getTimestamp(7), rs.getString(8), rs.getLong(9),
                rs.getBoolean(10));
            callerKnown = invocation.isCallerKnown();
            
            if(invocationList != null) 
            	invocationList.add(invocation);
            
            cg.setInvocation(invocation.getId(), invocation);
            
            if (!direction) {
            	
            	cg.addCallRelation(id, invocation.getId(), invocation, id == callingID);

            } else {
              if (callerKnown) {
            	  
            	cg.addCallRelation(invocation.getCallerID(), invocation.getId(), invocation, id == callingID);
                id = invocation.getCallerID();
              }
            }
          }
          if (!direction)
            break outer;
        } catch (SQLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
          callerKnown = false;
        }
      } while (callerKnown && ++depth < 30);
      cg.getNodes().get(id).setIndividual(true);
    }

  }
  
  public boolean isConnectionDiffSet() {
	  
	  if(connDiff != null)
		  return true;
	  else
		  return false;
  }
  
  public List<DBInvocation> getDiffInvocationBy(DBInvocation origin) {
	  
	  List<DBInvocation> dbList = new ArrayList<DBInvocation>();
	  
	  synchronized (this) {
		  try {

	          PreparedStatement stmt;
	          stmt = conn.prepareStatement("SELECT * FROM invocations WHERE CLASS = ? AND METHOD = ? AND UMN =?");
	          stmt.setString(1, origin.getClassName());
	          stmt.setString(2, origin.getMethodName());
	          stmt.setString(3, origin.getUniqueMethodName());
	          
	          ResultSet rs = stmt.executeQuery();
	          
	          while (rs.next()) {
	        	  dbList.add(new DBInvocation(rs.getLong(1), rs.getBoolean(2), rs.getString(3),
	                  rs.getString(4), rs.getString(5), rs.getString(6), rs.getTimestamp(7), rs.getString(8), rs.getLong(9),
	                  rs.getBoolean(10)));
	          }
	          
		  }  catch (SQLException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
		  }
		  
		  return dbList;
	  }
  }
}
