package es.deusto.ingenieria.ssdd.chat.client.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Observer;

import es.deusto.ingenieria.ssdd.chat.data.Message;
import es.deusto.ingenieria.ssdd.chat.data.User;
import es.deusto.ingenieria.ssdd.util.observer.local.LocalObservable;

public class ChatClientController {
	private String serverIP;
	private int serverPort;
	private User connectedUser;
	private LocalObservable observable;
	private List<String> connectedUsers;
	
	public ChatClientController() {
		this.observable = new LocalObservable();
		this.serverIP = null;
		this.serverPort = -1;
	}
	
	public String getConnectedUser() {
		if (this.connectedUser != null) {
			return this.connectedUser.getNick();
		} else {
			return null;
		}
	}

	public String getServerIP() {
		return this.serverIP;
	}
	
	public int gerServerPort() {
		return this.serverPort;
	}
	
	public boolean isConnected() {
		return this.connectedUser != null;
	}

	public void addLocalObserver(Observer observer) {
		this.observable.addObserver(observer);
	}
	
	public void deleteLocalObserver(Observer observer) {
		this.observable.deleteObserver(observer);
	}
	
	public int connect(String ip, int port, String nick) {
		this.connectedUser = new User();
		this.connectedUser.setNick(nick);
		this.serverIP = ip;
		this.serverPort = port;
		connectedUsers = new ArrayList<String>();
		
		String message = "0"+connectedUser.getNick();
		try (DatagramSocket udpSocket = new DatagramSocket()) {
			InetAddress serverHost = InetAddress.getByName(serverIP);			
			byte[] byteMsg = message.getBytes();
			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, serverPort);
			udpSocket.send(request);
			System.out.println(" - Sent a request to '" + serverHost.getHostAddress() + ":" + request.getPort() + 
					           "' -> " + new String(request.getData()));
			byte[] buffer = new byte[1024];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			udpSocket.receive(reply);
			System.out.println(" - Received a reply from '" + reply.getAddress().getHostAddress() + ":" + reply.getPort() + 
					           "' -> "+ new String(reply.getData()));
			String rep = new String(reply.getData());
			if (rep.contains("true")) {
				return 1;
			}
			else if (rep.contains("name")) {
				resetWhenNoConnected();
				return 0;
			}
			else {
				resetWhenNoConnected();
				return -1;
			}
		} catch (SocketException e) {
			System.err.println("# UDPClient Socket error: " + e.getMessage());
			resetWhenNoConnected();
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			System.err.println("# UDPClient IO error: " + e.getMessage());
			resetWhenNoConnected();
			return -1;
		}
	}
	
	private void resetWhenNoConnected() {
		this.connectedUser = null;
		this.serverIP = null;
		this.serverPort = -1;
	}
	
	public boolean disconnect() {
		String message = "1"+connectedUser.getNick();
		try (DatagramSocket udpSocket = new DatagramSocket()) {
			InetAddress serverHost = InetAddress.getByName(serverIP);			
			byte[] byteMsg = message.getBytes();
			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, serverPort);
			udpSocket.send(request);
			System.out.println(" - Sent a request to '" + serverHost.getHostAddress() + ":" + request.getPort() + 
					           "' -> " + new String(request.getData()));
		} catch (SocketException e) {
			System.err.println("# UDPClient Socket error: " + e.getMessage());
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.err.println("# UDPClient IO error: " + e.getMessage());
			return false;
		}
		
		this.connectedUser = null;
		connectedUsers.clear();
		connectedUsers = null;
		
		return true;
	}
	
	public List<String> getConnectedUsers() {
		String userString = null;
		
		try (DatagramSocket udpSocket = new DatagramSocket()) {
			String message = "4"+connectedUser.getNick();
			InetAddress serverHost = InetAddress.getByName(serverIP); //Transforma la IP en una URL			
			byte[] byteMsg = message.getBytes(); //Transforma el mensaje en un array de bytes
			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, serverPort);
			udpSocket.send(request);
			System.out.println(" - Sent a request to '" + serverHost.getHostAddress() + ":" + request.getPort() + 
					           "' -> " + new String(request.getData()));
			
			byte[] buffer = new byte[10240]; //Creo un buffer de 10kb - 10 usuarios
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			udpSocket.receive(reply);
			System.out.println(" - Received a reply from '" + reply.getAddress().getHostAddress() + ":" + reply.getPort() + 
					           "' -> "+ new String(reply.getData()));
			userString = new String(reply.getData());
		} catch (SocketException e) {
			System.err.println("# UDPClient Socket error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("# UDPClient IO error: " + e.getMessage());
		}
		//----
		
		connectedUsers.clear();
		if (userString != null) {
			String[] nicks = userString.split("/n");
			for (int i = 0; i < nicks.length; i++) {
				connectedUsers.add(nicks[i]);	
			}
		}
		
		return connectedUsers;
	}
	
	public void sendMessage(String message) {
		message = "2" + connectedUser.getNick() + "/" + message;
		//Pueden cambiarse los parametros		
		try (DatagramSocket udpSocket = new DatagramSocket()) {
			InetAddress serverHost = InetAddress.getByName(serverIP); //Transforma la IP en una URL			
			byte[] byteMsg = message.getBytes(); //Transforma el mensaje en un array de bytes
			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, serverPort);
			udpSocket.send(request);
			System.out.println(" - Sent a request to '" + serverHost.getHostAddress() + ":" + request.getPort() + 
					           "' -> " + new String(request.getData()));
		} catch (SocketException e) {
			System.err.println("# UDPClient Socket error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("# UDPClient IO error: " + e.getMessage());
		}
	}
	
	public void receiveMessage() {
		String receivedMessage = "~false";		
		try (DatagramSocket udpSocket = new DatagramSocket()) {
			String message = "3"+connectedUser.getNick();
			InetAddress serverHost = InetAddress.getByName(serverIP); //Transforma la IP en una URL			
			byte[] byteMsg = message.getBytes(); //Transforma el mensaje en un array de bytes
			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, serverPort);
			udpSocket.send(request);
			System.out.println(" - Sent a request to '" + serverHost.getHostAddress() + ":" + request.getPort() + 
					           "' -> " + new String(request.getData()));
			byte[] buffer = new byte[2048];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			udpSocket.receive(reply);			
			System.out.println(" - Received a reply from '" + reply.getAddress().getHostAddress() + ":" + reply.getPort() + 
					           "' -> "+ new String(reply.getData()));
			receivedMessage = new String(reply.getData());
		} catch (SocketException e) {
			System.err.println("# UDPClient Socket error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("# UDPClient IO error: " + e.getMessage());
		}
		
		//Notify the received message to the GUI
		if (!receivedMessage.startsWith("~false")) {
			Message m = new Message();
			String nick = "";
			String mes = "";
			for (int i = 0; i < receivedMessage.indexOf("/"); i++) {
				nick += receivedMessage.charAt(i);
			}
			for (int i = receivedMessage.indexOf("/") + 1; i < receivedMessage.length(); i++) {
				mes += receivedMessage.charAt(i);
			}
			if (!nick.equalsIgnoreCase(connectedUser.getNick())) {
				m.setText(mes);
				User u = new User();
				u.setNick(nick);
				m.setFrom(u);
				m.setTimestamp(Calendar.getInstance().getTimeInMillis());
				this.observable.notifyObservers(m);
			}
		}
	}	
}