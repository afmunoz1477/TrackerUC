package Server;

//TCPServer.java
//A server program implementing TCP socket

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.output.ThresholdingOutputStream; 


public class TCPServer { 

	private static final String SERVER = "157.253.236.163";
	private static final String AGENT = "157.253.239.33";
	private static final int AGENT_PORT = 10031;
	private static final String SHUTDOWN_AGENT = "SHUTDOWN";
	private static final String STATE = "STATE";
	public static final String ERRASE = "ER";
	public static final String START = "START";
	
	public static void main (String args[]) throws IOException, NoSuchAlgorithmException 
	{ 
	
		Boolean terminado = false;
		Boolean runningTracker = false;
		Socket socketAgent = null;
		Boolean downloading = false;
		BufferedReader br = null;
		String guide = "1 - Action Tracker \n"+"2 - Shutdown Tracker \n"+"3 - Start agent \n"+"4 - Shutdown agent \n"+"5 - Errase file \n"+"6 - Broadcast Start \n"+"7 - ResultAgent \n"+"8 - Boadcast Results \n"+"9 - BroadCast Errase \n"+"10 - Broadcast shutdown agent";
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			TrackerUC tracker = new TrackerUC();
			File ips =  new File("./announce/ips.txt");
			FileReader fr = new FileReader(ips);
			BufferedReader readIps = new BufferedReader(fr);
			String ip;
			ip = readIps.readLine();
			System.out.println("Waiting on: "+AGENT_PORT+", ServerIp: "+InetAddress.getByName(SERVER)+", Static ip: "+ip);
			while(!terminado){
				System.out.println("<------------------------>");
				System.out.println("Type what you need:");
//				InputStream inFrom = accept.getInputStream();
//				DataInputStream read = new DataInputStream(inFrom);
//				OutputStream out = accept.getOutputStream();
//				DataOutputStream outToAdmin = new DataOutputStream(out);
				String receive = br.readLine();
				System.out.println("Write: "+receive);
				Boolean create;
				//start tracker
				if ("h".equals(receive)) {
					System.out.println(guide);
				}
				else if(receive.equals("1")){
					create = false;
					if(!runningTracker){
						try {
							tracker.startTorrent(create);
							runningTracker = tracker.state;
							System.out.println("Running tracker? ->"+runningTracker);
							if(runningTracker){
								System.out.println("Tracker ready");
							}
						} catch (Exception e) {
							// TODO: handle exception
							System.out.println("Problem with tracker: "+e.getMessage());
						}
					}else{
						System.out.println("Tracker already running!!");	
					}
				}else if(receive.equals("2")){
					if (runningTracker){
						System.out.println("You have shutdown tracker!!");
						tracker.shutdown();
						runningTracker = false;
					}else{
						System.out.println("The tracker is down, please start it!!");
					}
				}
				else if(receive.equals("3")){
					try{
						
						String toAdmin = comunicateWithAgent(socketAgent,ip, AGENT_PORT, START);
						System.out.println(toAdmin);
					}catch (Exception e) {
						// TODO: handle exception
						System.out.println(e.getMessage());
					}
				}
				else if(receive.equals("4")){
					try{
						String toAdmin = comunicateWithAgent(socketAgent,ip, AGENT_PORT, SHUTDOWN_AGENT);
						System.out.println(toAdmin);
					}catch (Exception e) {
						// TODO: handle exception
						System.out.println(e.getMessage());
					}
				}
				else if(receive.equals("5")){
					try{
						String toAdmin = comunicateWithAgent(socketAgent,AGENT, AGENT_PORT, ERRASE);
						System.out.println(toAdmin);
					}catch (Exception e) {
						// TODO: handle exception
						System.out.println(e.getMessage());
					}
				}
				else if(receive.equals("6")){
					try{						
						broadCastStart(socketAgent,AGENT_PORT,START);
					}catch (Exception e) {
						// TODO: handle exception
						System.out.println(e.getMessage());
					}
				}
				else if(receive.equals("7")){
					try{
						String toAdmin = comunicateWithAgent(socketAgent,AGENT, AGENT_PORT, STATE);
						System.out.println(toAdmin);
					}catch (Exception e) {
						// TODO: handle exception
						System.out.println(e.getMessage());
					}
				}
				else if(receive.equals("8")){
					try{						
						broadCastStart(socketAgent,AGENT_PORT,STATE);
					}catch (Exception e) {
						// TODO: handle exception
						System.out.println(e.getMessage());
					}
				}
				else if(receive.equals("9")){
					try{						
						broadCastStart(socketAgent,AGENT_PORT,ERRASE);
					}catch (Exception e) {
						// TODO: handle exception
						System.out.println(e.getMessage());
					}
				}
				else if(receive.equals("10")){
					try{						
						broadCastStart(socketAgent,AGENT_PORT,SHUTDOWN_AGENT);
					}catch (Exception e) {
						// TODO: handle exception
						System.out.println(e.getMessage());
					}
				}
				else if(receive.equals("q")){
					//Command to shutdown server and client!
					terminado = true;
					System.out.println("shutdown");
					broadCastStart(socketAgent, AGENT_PORT, "q");
					System.out.println("Server disconected!");
				}else{
					System.out.println(receive);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error: "+ e.getMessage());
		}


	}

	private static void broadCastStart( Socket socketAgent, int agentPort, String action) throws IOException {
		// TODO Auto-generated method stub

		File ips =  new File("./announce/ips.txt");
		FileReader fr = new FileReader(ips);
		String resp;
		BufferedReader readIps = new BufferedReader(fr);
		if(action.equals(START)){
			String ip;
			System.out.println("BroadCast");
			
			while((ip = readIps.readLine()) != null){
				try{
					resp = comunicateWithAgent(socketAgent, ip, agentPort, START);
					System.out.println("Agent "+ip+"; "+resp);
				}catch (Exception e) {
					// TODO: handle exception
					System.out.println("Agent: "+ip+", Error: "+e.getMessage());
				}
			}
			System.out.println("Finish");
		}else if(action.equals(SHUTDOWN_AGENT)){
			String ip;
			System.out.println("BroadCast");
			while((ip = readIps.readLine()) != null){
				try{
					resp = comunicateWithAgent(socketAgent, ip, agentPort, SHUTDOWN_AGENT);
					System.out.println("Agent "+ip+"; "+resp);
				}catch (Exception e) {
					// TODO: handle exception
					System.out.println("Agent: "+ip+", Error: "+e.getMessage());
				}
			}
			System.out.println("Finish");
			
		}else if (action.equals(ERRASE)) {
			String ip;
			System.out.println("BroadCast");
			while((ip = readIps.readLine()) != null){
				try{
					resp = comunicateWithAgent(socketAgent, ip, agentPort, ERRASE);
					System.out.println("Agent: "+ip+"; "+resp);
				}catch (Exception e) {
					// TODO: handle exception
					System.out.println("Agent: "+ip+", Error: "+e.getMessage());
				}
			}
			System.out.println("Finish");
			
		}else if (action.equals(STATE)) {
			String ip;
			System.out.println("BroadCast");
			while((ip = readIps.readLine()) != null){
				try{
					resp = comunicateWithAgent(socketAgent, ip, agentPort, STATE);
					System.out.println("Agent "+ip+"; "+resp);
				}catch (Exception e) {
					// TODO: handle exception
					System.out.println("Agent: "+ip+", Error: "+e.getMessage());
				}
			}
			System.out.println("Finish");
		}
		else if (action.equals("test")) {
			String ip;
			System.out.println("BroadCast");
			while((ip = readIps.readLine()) != null){
				try{
					resp = comunicateWithAgent(socketAgent, ip, agentPort, "test");
					System.out.println("Agent "+ip+"; "+resp);
				}catch (Exception e) {
					// TODO: handle exception
					System.out.println("Agent: "+ip+", Error: "+e.getMessage());
				}
			}
			System.out.println("Finish");
		}
		else if (action.equals("q")) {
			String ip;
			System.out.println("BroadCast");
			while((ip = readIps.readLine()) != null){
				try{
					resp = comunicateWithAgent(socketAgent, ip, agentPort, "q");
					System.out.println("Agent "+ip+"; "+resp);
				}catch (Exception e) {
					// TODO: handle exception
					System.out.println("Agent: "+ip+", Error: "+e.getMessage());
				}
			}
			System.out.println("Finish");
		}

	}

	@SuppressWarnings("resource")
	private static String comunicateWithAgent(Socket socket, String ip, int port, String action) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		String back = null;
		Boolean ans = false;
		socket = new Socket(ip, port);
		InputStream fromAgente = socket.getInputStream();
		DataInputStream read = new DataInputStream(fromAgente);
		BufferedReader console = null;
		OutputStream outTo = socket.getOutputStream();
		DataOutputStream write = new DataOutputStream(outTo);

		if (action.equals(START)) {
			write.writeUTF(START);
		}
		else if (action.equals(SHUTDOWN_AGENT)) {
			write.writeUTF(SHUTDOWN_AGENT); 
		}else if (action.equals(ERRASE)) {
			write.writeUTF(ERRASE);
		}else if (action.equals(STATE)) {
			write.writeUTF("RESULT");
		}else if(action.equals("test")){
			System.out.println("Test: "+socket.toString());
		}
		else if(action.equals("q")){
			write.writeUTF("q");
		}



		while(!ans){
			String ansFromAgent = read.readUTF();
			if(ansFromAgent!=null){
				back = ansFromAgent;
				ans = true;
			}
		}

		return back;


	} 

} 


