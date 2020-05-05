package com.cws.qoutestolistentoo.audioplayer;

import com.cws.qoutestolistentoo.Text;

import java.util.ArrayList;

public interface AudioPlayerServiceInterface {
	public void setQueue(ArrayList<Text> texts);
	public void skipToPoint(int time);
	public void play();
	public void play(int position);
	public void pause();
}
