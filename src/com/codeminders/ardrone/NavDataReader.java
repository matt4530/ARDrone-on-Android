package com.codeminders.ardrone;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import android.util.Log;

public class NavDataReader implements Runnable {
	private static final int BUFSIZE = 4096;

	private DatagramChannel channel;
	private ARDrone drone;
	private Selector selector;
	private boolean done;

	public NavDataReader(ARDrone drone, InetAddress drone_addr, int navdata_port) throws IOException {
		this.drone = drone;

		channel = DatagramChannel.open();
		channel.configureBlocking(false);
		channel.socket().setReuseAddress(true);
		channel.socket().bind(new InetSocketAddress(navdata_port));
		channel.connect(new InetSocketAddress(drone_addr, navdata_port));

		selector = Selector.open();
	}

	private void disconnect() {
		try {
			selector.close();
		} catch (IOException iox) {
			// Ignore
		}

		try {
				channel.socket().disconnect();
				channel.socket().close();
			//}
			channel.disconnect();
			Log.v("Drone Control", "dissss");
			channel.close();
		} catch (IOException iox) {
			// Ignore
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void run() {
		try {
			ByteBuffer inbuf = ByteBuffer.allocate(BUFSIZE);
			boolean channelIsReadable = false;
			done = false;
			
			channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

			/* Loop until channel is ready to read. Note that we are 
			 * gonna produce packets at a pretty high rate here.
			 */
			while (!channelIsReadable && !done)
			{
				/* Wait until channel is ready to write */
				selector.select();
				Set readyKeys = selector.selectedKeys();
				Iterator iterator = readyKeys.iterator();
				while (iterator.hasNext() && !done) {
					SelectionKey key = (SelectionKey) iterator.next();
					iterator.remove();
					if (key.isWritable()) {
						Log.v("NavDataReader", "Send TRIGGER");
						byte[] trigger_bytes = { 0x01, 0x00, 0x00, 0x00 };
						ByteBuffer trigger_buf = ByteBuffer.allocate(trigger_bytes.length);
						trigger_buf.put(trigger_bytes);
						trigger_buf.flip();
						channel.write(trigger_buf);
					}
					/* Exit loop when channel is readable */
					if (key.isReadable())
						channelIsReadable = true;
				}
			}
			
			/* Since we won't write any more trigger packets, change to read only */
			channel.register(selector, SelectionKey.OP_READ);
			
			while (!done) {
				selector.select();
				Set readyKeys = selector.selectedKeys();
				Iterator iterator = readyKeys.iterator();
				while (iterator.hasNext() && !done) {
					SelectionKey key = (SelectionKey) iterator.next();
					iterator.remove();
					if (key.isReadable()) {
						inbuf.clear();
						int len = channel.read(inbuf);
						byte[] packet = new byte[len];
						inbuf.flip();
						inbuf.get(packet, 0, len);

						NavData nd = NavData.createFromData(packet);

						drone.navDataReceived(nd);
					}
				}
			}
			
			Log.v("Drone Control", "Disconnect");
			disconnect();
		} catch (Exception e) {
			drone.changeToErrorState(e);
		}

	}

	public void stop() {
		done = true;
		selector.wakeup();
	}

}
