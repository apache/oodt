// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: DatagramLogger.java,v 1.1 2004-03-02 19:28:57 kelly Exp $

package jpl.eda.activity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * The datagram logger accepts incidents in UDP datagrams and saves them in a {@link
 * Storage}.  The datagrams contain serialized {@link Incident}s, and are probably sent by
 * a {@link DatagramLoggingActivityFactory}.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
class DatagramLogger {
	public static void main(String[] argv) throws Throwable {
		if (argv.length > 0) {
			System.err.println("This program takes NO command line arguments.");
			System.err.println("Set the activity.port property to adjust the port number.");
			System.err.println("Set the activity.storage property to set the Storage class to use.");
			System.exit(1);
		}
		int port = Integer.getInteger("activity.port", 4556).intValue();
		String className = System.getProperty("activity.storage");
		if (className == null) {
			System.err.println("No Storage class defined via the `activity.storage' property; exiting...");
			System.exit(1);
		}
		Class storageClass = Class.forName(className);
		storage = (Storage) storageClass.newInstance();
		DatagramSocket socket = new DatagramSocket(port);
		byte[] buf = new byte[2048];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		for (;;) {
			socket.receive(packet);
			byte[] received = new byte[packet.getLength()];
			System.arraycopy(packet.getData(), packet.getOffset(), received, 0, packet.getLength());
			new ReceiverThread(received).start();
		} 
	}

	/** Long term storage for incidents. */
	private static Storage storage;

	/** History awaiting long-term storage.  Keys are {@link String} activity IDs, and values are {@link History} objects. */
	private static Map histories = new HashMap();

	/**
	 * Thread that saves off an incident into a history so the main thread can go back
	 * to receiving more datagrams.
	 */
	private static class ReceiverThread extends Thread {
		/**
		 * Creates a new {@link ReceiverThread} instance.
		 *
		 * @param data Copy of bytes received in a datagram.
		 */
		private ReceiverThread(byte[] data) {
			this.data = data;
		}

		/**
		 * Reconstitute the incident in the received byte array and store it into
		 * a {@link History}.
		 */
		public void run() {
			try {
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
				Incident incident = (Incident) in.readObject();
				String id = incident.getActivityID();
				in.close();
				History history;
				synchronized (histories) {
					history = (History) histories.get(id);
					if (history == null) {
						histories.put(id, new History(incident, storage));
						return;
					}
				}
				history.addIncident(incident);
			} catch (ClassNotFoundException ex) {
				System.err.println("Dropping Incident of unknown class: " + ex.getMessage());
			} catch (InvalidClassException ex) {
				System.err.println("Dropping Incident of invalid class: " + ex.getMessage());
			} catch (StreamCorruptedException ex) {
				System.err.println("Unable to read Incident from packet: " + ex.getMessage());
			} catch (OptionalDataException ex) {
				System.err.println("Primitive data instead of Incident in packet: " + ex.getMessage());
			} catch (IOException ex) {
				throw new IllegalStateException("Unexpected IOException: " + ex.getMessage());
			}
		}

		/** Bytes received in a datagram. */
		private byte[] data;
	}
}
