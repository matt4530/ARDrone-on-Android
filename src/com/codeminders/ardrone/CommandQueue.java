package com.codeminders.ardrone;

import java.util.Iterator;
import java.util.LinkedList;

public class CommandQueue {
	private LinkedList<DroneCommand> data;
	private int maxSize;

	public CommandQueue(int maxSize) {
		data = new LinkedList<DroneCommand>();
		this.maxSize = maxSize;
	}

	public synchronized DroneCommand take() throws InterruptedException {
		while (true) {
			// DroneCommand res = data.pollLast();/*REPLACED TO SUPPORT OLD API
			// VERSIONS

			// **ADDED BY MATTHEW
			DroneCommand res = null;
			if (!data.isEmpty())
				res = data.removeLast();
			// **END

			if (res != null) {
				// log.debug("[" + data.size() + "] Returning " + res);
				if (res.isSticky()) {
					int sc = res.incrementStickyCounter();
					data.addLast(res);
					if (sc > 1)
						Thread.sleep(res.getStickyRate());
				}
				return res;
			} else {
				// log.debug("Waiting for data");
				wait();
			}
		}
	}

	public synchronized void add(DroneCommand cmd) {
		Iterator<DroneCommand> i = data.iterator();
		int p = cmd.getPriority();
		int pos = -1;

		while (i.hasNext()) {
			DroneCommand x = i.next();
			pos++;
			int xp = x.getPriority();
			if (xp < p) {
				// Skipping
				continue;
			} else {
				// Found insertion point.
				if (!x.replaces(cmd)) {
					// log.debug("[" + data.size() + "] Adding command " +
					// cmd);
					data.add(pos, cmd);
					notify();
				} else {
					// log.debug("Replacing duplicate element " + cmd);
					data.set(pos, cmd);
				}
				cmd = null; // inserted
				break;
			}
		}

		if (cmd != null) {
			// log.debug("[" + data.size() + "] Adding command " + cmd);
			data.addLast(cmd);
			notify();
		}

		if (data.size() > maxSize) {
			// TODO: trim
		}
	}

	public synchronized int size() {
		return data.size();
	}

	public synchronized void clear() {
		data.clear();
		notify();
	}

}
