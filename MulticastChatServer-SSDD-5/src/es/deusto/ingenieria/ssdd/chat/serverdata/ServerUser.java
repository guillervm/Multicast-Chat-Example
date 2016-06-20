package es.deusto.ingenieria.ssdd.chat.serverdata;

import java.net.InetAddress;

import es.deusto.ingenieria.ssdd.chat.data.User;

public class ServerUser extends User {
	private InetAddress ip;
	private int port;
	private int lastMessageReceived;
	
	public ServerUser(String nick, InetAddress ip, int port) {
		this.setNick(nick);
		this.setIp(ip);
		this.setPort(port);
		this.lastMessageReceived = -1;
	}
	
	public InetAddress getIp() {
		return ip;
	}
	
	public void setIp(InetAddress ip) {
		this.ip = ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getLastMessageReceived() {
		return this.lastMessageReceived;
	}
	
	public void setMessageReceived(int num) {
		lastMessageReceived = num;
	}
	
	public void messageSent() {
		lastMessageReceived++;
	}
}
