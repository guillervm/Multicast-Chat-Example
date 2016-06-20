package es.deusto.ingenieria.ssdd.chat.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import es.deusto.ingenieria.ssdd.chat.serverdata.ServerUser;

public class MainProgram {
	private static int serverPort;
	private static final int PORT = 6789;
	private static ArrayList<ServerUser> connectedUsers;
	private static ArrayList<String> messages;
	
	public static void main(String[] args) {
		messages = new ArrayList<String>();
		messages.add("Server/Welcome to the chat! :-)");
		connectedUsers = new ArrayList<ServerUser>();
		
		serverPort = args.length == 0 ? MainProgram.PORT : Integer.parseInt(args[0]);
		
		try (DatagramSocket udpSocket = new DatagramSocket(serverPort)) {
			DatagramPacket request = null;
			DatagramPacket reply = null;
			byte[] buffer = new byte[1024];
			
			System.out.println(" - Waiting for connections '" + 
			                       udpSocket.getLocalAddress().getHostAddress() + ":" + 
					               serverPort + "' ...");
			
			while (true) {
				request = new DatagramPacket(buffer, buffer.length);
				udpSocket.receive(request);
				System.out.println(" - Received a request from '" + request.getAddress().getHostAddress() + ":" + request.getPort() + 
				                   "' -> " + new String(request.getData()));
				reply = processRequest(request);
				
				if (reply != null) {
					udpSocket.send(reply);				
						/*System.out.println(" - Sent a reply to '" + reply.getAddress().getHostAddress() + ":" + reply.getPort() + 
								           "' -> " + new String(reply.getData()));*/
				}
				buffer = new byte[1024];
			}
		} catch (SocketException e) {
			System.err.println("# UDPServer Socket error: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("# UDPServer IO error: " + e.getMessage());
		}
	}

	private static DatagramPacket processRequest(DatagramPacket request) {
		String message = new String(request.getData());
		int op = Integer.valueOf(message.substring(0, 1));
		message = message.substring(1);
		switch (op) {
			case 0:
				return connect(message, request);
	
			case 1:
				disconnect(message, request);
				break;			
			case 2:
				sendMessage(message);
				break;
			case 3:
				return receiveMessage(message, request);
			case 4:
				return sendConnectedUsers(message, request);	
		}
		return null;
	}

	private static DatagramPacket connect(String nick, DatagramPacket request) {
		String temp = "false";
		
		for (ServerUser u:connectedUsers) {
			if (u.getNick().equalsIgnoreCase(nick)) {
				temp = "name";
			}
		}
		if (connectedUsers.size() < 10 && temp != "name") {
			ServerUser user = new ServerUser(nick, request.getAddress(), request.getPort());
			connectedUsers.add(user);
			System.out.println(" - " + nick + " connected at "+request.getAddress().getHostAddress()+":"+request.getPort());
			
			temp = "true";
		}
		byte[] buffer = temp.getBytes();
		return new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
	}
	
	private static void disconnect(String nick, DatagramPacket request) {
		ServerUser user = new ServerUser(nick, request.getAddress(), request.getPort());
		connectedUsers.remove(user);
		System.out.println(" - " + nick + " disconnected");
	}
	
	private static DatagramPacket sendConnectedUsers(String nick, DatagramPacket request) {
		String list = "";
		for (ServerUser u:connectedUsers) {
			if (!u.getNick().equalsIgnoreCase(nick)) {
				list += u.getNick() + "/n";
			}
		}
		byte[] buffer = list.getBytes();
		return new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
	}

	private static void sendMessage(String message) {
		String nick = "";
		String mes = "";
		for (int i = 0; i < message.indexOf("/"); i++) {
			nick += message.charAt(i);
		}
		for (int i = message.indexOf("/") + 1; i < message.length(); i++) {
			mes += message.charAt(i);
		}
		messages.add(nick+"/"+mes);
	}
	
	private static DatagramPacket receiveMessage(String nick, DatagramPacket request) {
		ServerUser user = new ServerUser(nick, request.getAddress(), request.getPort());
		int index = connectedUsers.indexOf(user);
		String message = "~false";
		if (connectedUsers.get(index).getLastMessageReceived() < messages.size()-1) {
			connectedUsers.get(index).messageSent();
			message = messages.get(connectedUsers.get(index).getLastMessageReceived());
			if (connectedUsers.get(index).getLastMessageReceived() == 0) {
				connectedUsers.get(index).setMessageReceived(messages.size() - 1);
			}
		}
		
		byte[] buffer = message.getBytes();
		return new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
	}
}
