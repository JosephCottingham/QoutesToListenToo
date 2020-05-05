package com.cws.qoutestolistentoo.audioplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.cws.qoutestolistentoo.Text;
//import java.util.Random;


public class Queue {
	private int currentIndex=-1;
	private boolean random = false;
	private ArrayList<Text> queue = new ArrayList<Text>();

	public Text getCurrentlyPlaying() {
		return queue.get(currentIndex);
	}


	public void setQueue(ArrayList<Text> queue){
		this.queue = queue;
	}

	public int getSizeOfQueue() {
		return queue.size();
	}
	
	public Text next() {
		if (currentIndex+1<queue.size() && currentIndex>-1){
			currentIndex++;
			return queue.get(currentIndex);
		}
		return null;
	}
	
	public Text last() {
		if (currentIndex-1>=0){
			currentIndex--;
			return queue.get(currentIndex);
		}
		return null;
	}
	
	public Text playGet(int position) {
		currentIndex = position;
		return queue.get(position);
	}
	
	public void clearQueue() {
		queue.clear();
		currentIndex = -1;
	}
	public Text getCurrentText(){
		if (currentIndex!=-1)
			return queue.get(currentIndex);
		return null;
	}
}
