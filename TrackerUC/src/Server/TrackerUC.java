package Server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.log4j.BasicConfigurator;

import com.turn.ttorrent.cli.TrackerMain;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;

public class TrackerUC {

	public Boolean state;
	String name = "Prueba2GB.rar";
	String file = PATH +name+".torrent";
	String sharedFile = PATH + name;
	File torrentUbication = new File (""+file);
	public static final String IPDEST = "157.253.236.163";
	public static final String PATH = "./announce/";
	public static final String TRACKER_URL = "http://"+IPDEST+":10031/announce";
	Tracker t;
	Client client;
	InetAddress ip = InetAddress.getByName(IPDEST);

	public TrackerUC() throws IOException, NoSuchAlgorithmException {
		BasicConfigurator.configure();
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void encodeObject(Object o, OutputStream out) throws IOException {
		if (o instanceof String)
			encodeString((String)o, out);
		else if (o instanceof Map)
			encodeMap((Map)o, out);
		else if (o instanceof byte[])
			encodeBytes((byte[])o, out);
		else if (o instanceof Number)
			encodeLong(((Number) o).longValue(), out);
		else
			throw new Error("Unencodable type");
	}
	private static void encodeLong(long value, OutputStream out) throws IOException {
		out.write('i');
		out.write(Long.toString(value).getBytes("US-ASCII"));
		out.write('e');
	}
	private static void encodeBytes(byte[] bytes, OutputStream out) throws IOException {
		out.write(Integer.toString(bytes.length).getBytes("US-ASCII"));
		out.write(':');
		out.write(bytes);
	}
	private static void encodeString(String str, OutputStream out) throws IOException {
		encodeBytes(str.getBytes("UTF-8"), out);
	}
	private static void encodeMap(Map<String,Object> map, OutputStream out) throws IOException{
		// Sort the map. A generic encoder should sort by key bytes
		SortedMap<String,Object> sortedMap = new TreeMap<String, Object>(map);
		out.write('d');
		for (java.util.Map.Entry<String, Object> e : sortedMap.entrySet()) {
			encodeString(((java.util.Map.Entry<String, Object>) e).getKey(), out);
			encodeObject(((java.util.Map.Entry<String, Object>) e).getValue(), out);
		}
		out.write('e');
	}
	private static byte[] hashPieces(File file, int pieceLength) throws IOException {

		MessageDigest sha1;
		try {
			sha1 = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			throw new Error("SHA1 not supported");
		}

		InputStream in = new FileInputStream(file);

		ByteArrayOutputStream pieces = new ByteArrayOutputStream();
		byte[] bytes = new byte[pieceLength];
		int pieceByteCount  = 0, readCount = in.read(bytes, 0, pieceLength);
		while (readCount != -1) {
			pieceByteCount += readCount;
			sha1.update(bytes, 0, readCount);
			if (pieceByteCount == pieceLength) {
				pieceByteCount = 0;
				pieces.write(sha1.digest());
			}
			readCount = in.read(bytes, 0, pieceLength-pieceByteCount);
		}
		in.close();
		if (pieceByteCount > 0)
			pieces.write(sha1.digest());
		return pieces.toByteArray();
	}
	public void createTorrent(File file, File sharedFile, String announceURL) throws IOException {
		final int pieceLength = 512*1024;
		Map<String,Object> info = new HashMap<String,Object>();
		info.put("name", sharedFile.getName());
		info.put("length", sharedFile.length());
		info.put("piece length", pieceLength);
		info.put("pieces", hashPieces(sharedFile, pieceLength));
		Map<String,Object> metainfo = new HashMap<String,Object>();
		metainfo.put("announce", announceURL);
		metainfo.put("info", info);
		OutputStream out = new FileOutputStream(file);
		encodeMap(metainfo, out);
		out.close();
	}

	public Boolean startTorrent(Boolean create) throws IOException{
		
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".torrent");
			}

		};

		//---------------------------------------------------------------------

		System.out.println("Parent Directory: " + torrentUbication.getParent());
		//---------------------------------------------------------------------

		if(create){
			createTorrent(new File(""+file), new File(""+sharedFile), TRACKER_URL);
		}
		try {
			
			t = new Tracker(new InetSocketAddress(10031));
			client = new Client(ip, 
					SharedTorrent.fromFile(new File(""+file),new File(""+sharedFile).getParentFile()));
			File parent = new File(PATH);
			for (File f : parent.listFiles(filter)) {
				System.out.println("Loading torrent from " + f.getName());
				t.announce(TrackedTorrent.load(f));
			}
			System.out.println("Starting tracker with {} announced torrents..."+t.getTrackedTorrents().size());
			t.start();
			
			System.out.printf("Inet Address: "+ip+"\n"+"File: " + file+"\n"+"Shared: "+sharedFile);
			client.share();
			state = true;
			//				client.waitForCompletion();
		} catch (Exception e) {
			state = false;
			System.out.println("{}"+ e.getMessage()+"y .."+ e);
			shutdown();
		}
		return this.state;
	}
	
	public void shutdown(){
		client.stop();
		this.t.stop();
	}
}


