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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashSet;

import javax.swing.event.ChangeListener;

import sun.net.ConnectionResetException;
import at.tugraz.iaik.magnum.data.cmd.Command;
import at.tugraz.iaik.magnum.data.transport.TransportObject;

import com.google.inject.Singleton;

@Singleton
public class NetworkCommunication implements Communication {
  public static Communication     instance  = null;
  private Socket                  sock;
  private ObjectInputStream       objIn;
  private ObjectOutputStream      objOut;
  private TrafficIndicatorStream  indicator;
  private boolean                 connected = false;
  private HashSet<ChangeListener> listenerCache;

  public NetworkCommunication() {
    listenerCache = new HashSet<>();
  }

  @Override
  public void connect(String host, int port) throws IOException {
    System.out.println(host);
    System.out.println(port);

    SocketAddress addr = new InetSocketAddress(host, port);
    sock = new Socket();
    sock.connect(addr, 1000);
    connected = true;
    System.out.println("Connected");
  }

  private void createInStream() throws IOException {
    if (objIn != null)
      return;
    indicator = new TrafficIndicatorStream(sock.getInputStream());
    for (ChangeListener l : listenerCache)
      indicator.addChangeListener(l);
    listenerCache.clear();
    BufferedInputStream bufferedStream = new BufferedInputStream(indicator);
    objIn = new ObjectInputStream(bufferedStream);
  }

  private void createOutStream() throws IOException {
    if (objOut != null)
      return;
    objOut = new ObjectOutputStream(sock.getOutputStream());
  }

  public void write(Command cmd) throws ConnectException {
    if (!connected)
      throw new ConnectException("Not Connected!");
    try {
      createOutStream();
      objOut.writeObject(cmd);
      objOut.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public TransportObject read() throws IOException {
    if (!connected)
      throw new ConnectException("Not Connected!");
    try {
      createInStream();

      return (TransportObject) objIn.readObject();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      connected = false;
      e.printStackTrace();
      throw e;
    }

    throw new ConnectionResetException();
  }

  public void addChangeListener(ChangeListener l) {
    if (isConnected())
      indicator.addChangeListener(l);
    else
      listenerCache.add(l);
  }

  @Override
  public boolean isConnected() {
    return connected;
  }
}
