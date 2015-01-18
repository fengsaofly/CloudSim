package org.cloudbus.cloudsim.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.cloudbus.cloudsim.core.FutureQueue;
import org.cloudbus.cloudsim.core.SimEvent;


public class MyFutureQueue extends FutureQueue {
	private ConcurrentSkipListSet<SimEvent>  flySortedSet = new ConcurrentSkipListSet<SimEvent>();
	@Override
	public void addEvent(SimEvent newEvent) {
		// TODO Auto-generated method stub
			newEvent.setSerial(serial++);
			flySortedSet.add(newEvent);
	}
	@Override
	public boolean remove(SimEvent event) {
			return	flySortedSet.remove(event);
	}
	@Override
	public boolean removeAll(Collection<SimEvent> events) {
			return flySortedSet.removeAll(events);
	}

	/**
	 * Add a new event to the head of the queue.
	 * 
	 * @param newEvent The event to be put in the queue.
	 */
	public void addEventFirst(SimEvent newEvent) {
		newEvent.setSerial(0);
		flySortedSet.add(newEvent);
	}

	/**
	 * Returns an iterator to the queue.
	 * 
	 * @return the iterator
	 */
	public Iterator<SimEvent> iterator() {
		return flySortedSet.iterator();
	}

	/**
	 * Returns the size of this event queue.
	 * 
	 * @return the size
	 */
	public int size() {
		return flySortedSet.size();
	}




	/**
	 * Clears the queue.
	 */
	public void clear() {
		flySortedSet.clear();
	}
}
