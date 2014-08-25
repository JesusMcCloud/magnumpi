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
package at.tugraz.iaik.magnum.client.net;

import java.io.IOException;
import java.net.ConnectException;

import javax.swing.event.ChangeListener;

import sun.net.ConnectionResetException;
import at.tugraz.iaik.magnum.data.cmd.Command;
import at.tugraz.iaik.magnum.data.transport.TransportObject;

public interface Communication {

  public abstract void connect(String host, int port) throws IOException;

  public abstract TransportObject read() throws ConnectException, ConnectionResetException, IOException, InterruptedException;

  public void write(Command cmd) throws ConnectException;
  
  public abstract boolean isConnected();
  
  public abstract void addChangeListener(ChangeListener l);
}