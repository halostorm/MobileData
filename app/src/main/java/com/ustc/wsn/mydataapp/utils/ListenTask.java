package com.ustc.wsn.mydataapp.utils;

import com.ustc.wsn.mydataapp.detectorservice.ReceiveThread;

import java.util.TimerTask;

public class ListenTask extends TimerTask {

	private ReceiveThread receiveThread;

	public ListenTask(ReceiveThread re) {
		receiveThread = re;
	}

	public void run() {
		// Toast.LENGTH_SHORT);
	}
}
