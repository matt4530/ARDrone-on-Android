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

import com.codeminders.ardrone.video.BufferedVideoImage;

public class VideoReader implements Runnable {
	/**
	 * Image data buffer. It should be big enough to hold single full frame
	 * (encoded).
	 */
	private static final int BUFSIZE = 100 * 1024;

	private DatagramChannel channel;
	private ARDrone drone;
	private Selector selector;
	private boolean done;

	public VideoReader(ARDrone drone, InetAddress drone_addr, int video_port) throws IOException {
		this.drone = drone;

		channel = DatagramChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(video_port));
		channel.connect(new InetSocketAddress(drone_addr, video_port));

		selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	}

	private void disconnect() {
		Log.v("Drone Control", "Video Reader: Told to disconnect");
		try {
			selector.close();
		} catch (IOException iox) {
			// Ignore
		}

		try {
			if (channel.isConnected()) {
				channel.socket().disconnect();
				channel.socket().close();
			}
			channel.disconnect();
			channel.close();
		} catch (IOException iox) {
			// Ignore
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void run() {
		// while(true)
		// {
		try {
			ByteBuffer inbuf = ByteBuffer.allocate(BUFSIZE);
			done = false;
			while (!done) {
				//Log.v("Drone Control", "Video Reader: !done");
				selector.select();
				//Log.v("Drone Control", "Video Reader: !done2");
				if (done) {
					//Log.v("Drone Control", "Video Reader: !done2.5");
					disconnect();
					break;
				}
				//Log.v("Drone Control", "Video Reader: !done3");
				Set readyKeys = selector.selectedKeys();
				Iterator iterator = readyKeys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = (SelectionKey) iterator.next();
					iterator.remove();
					if (key.isWritable()) {
						byte[] trigger_bytes = { 0x01, 0x00, 0x00, 0x00 };
						ByteBuffer trigger_buf = ByteBuffer.allocate(trigger_bytes.length);
						trigger_buf.put(trigger_bytes);
						trigger_buf.flip();
						channel.write(trigger_buf);
						channel.register(selector, SelectionKey.OP_READ);
					} else if (key.isReadable()) {
						inbuf.clear();
						int len = channel.read(inbuf);
						Log.v("Drone Control", "Video Reader: key.isReadable() + video frame recieved + length = " + len);
						if (len > 0) {
							inbuf.flip();
							final BufferedVideoImage vi = new BufferedVideoImage();
							vi.addImageStream(inbuf);
							drone.videoFrameReceived(0, 0, vi.getWidth(), vi.getHeight(), vi.getJavaPixelData(), 0, vi.getWidth());
						}
					}
					//Log.v("Drone Control", "Video Reader: Iterator has next");
				}
				//Log.v("Drone Control", "Video Reader: HI!");
			}

		} catch (Exception e) {
			drone.changeToErrorState(e);
		}
		/*
		 * try { Thread.sleep(50); } catch (InterruptedException e) {
		 * e.printStackTrace(); }
		 */
		Log.v("Drone Control", "Video Reader is running");
		// }

	}

	public void stop() {
		done = true;
		selector.wakeup();
	}

}
