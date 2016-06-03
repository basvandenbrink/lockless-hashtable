package nl.utwente.csc.fmt.locklesshashtable.test;

import java.util.HashSet;
import java.util.Set;

public class ThreadListener {
	private Set<TestThread> threads;
	private int activeThreads = 0;

	public ThreadListener() {
		this.threads = new HashSet<TestThread>();
	}
	
	public synchronized void add(TestThread thread){
		threads.add(thread);
		activeThreads ++;
	}
	
	public synchronized void notifyDone(){
		activeThreads --;
		if(activeThreads == 0)
			notifyThreads();
	}
	
	private void notifyThreads(){
		for(TestThread thread: threads){
			thread.quit();
		}
	}

}
