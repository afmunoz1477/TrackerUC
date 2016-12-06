package Tracker;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggerFactory;

import com.turn.ttorrent.cli.TrackerMain;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;

import jargs.gnu.CmdLineParser;

public class TrackerUC {
	


		/**
		 * Display program usage on the given {@link PrintStream}.
		 */
		private static void usage(PrintStream s) {
			s.println("usage: Tracker [options] [directory]");
			s.println();
			s.println("Available options:");
			s.println("  -h,--help             Show this help and exit.");
			s.println("  -p,--port PORT        Bind to port PORT.");
			s.println();
		}

		/**
		 * Main function to start a tracker.
		 */
		/**
		 * @param args
		 */
		public static void main(String[] args) {
			BasicConfigurator.configure(new ConsoleAppender(
					new PatternLayout("%d [%-25t] %-5p: %m%n")));

				CmdLineParser parser = new CmdLineParser();
				CmdLineParser.Option help = parser.addBooleanOption('h', "help");
				CmdLineParser.Option port = parser.addIntegerOption('p', "port");

				try {
					parser.parse(args);
				} catch (CmdLineParser.OptionException oe) {
					System.err.println(oe.getMessage());
					usage(System.err);
					System.exit(1);
				}

				// Display help and exit if requested
				if (Boolean.TRUE.equals((Boolean)parser.getOptionValue(help))) {
					usage(System.out);
					System.exit(0);
				}
				Integer puerto = 80;
				Integer portValue = (Integer)parser.getOptionValue(port,
					puerto);

				String[] otherArgs = parser.getRemainingArgs();

				if (otherArgs.length > 1) {
					usage(System.err);
					System.exit(1);
				}

				// Get directory from command-line argument or default to current
				// directory
				String directory = otherArgs.length > 0
					? otherArgs[0]
					: ".";

				FilenameFilter filter = new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".torrent");
					}
				};

				try {
					Tracker t = new Tracker(new InetSocketAddress(portValue.intValue()));

					File parent = new File("C:/Users/ANDRES/Desktop/TrackerUC/TrackerUC/announce");
					for (File f : parent.listFiles(filter)) {
						System.out.println("Loading torrent from " + f.getName());
						t.announce(TrackedTorrent.load(f));
					}

					System.out.println("Starting tracker with {} announced torrents..."+
						t.getTrackedTorrents().size());
					t.start();
				} catch (Exception e) {
					System.out.println("{}"+ e.getMessage()+"y .."+ e);
					System.exit(2);
				}
			}
}


