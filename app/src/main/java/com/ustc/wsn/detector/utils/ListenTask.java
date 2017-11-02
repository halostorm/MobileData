package com.ustc.wsn.detector.utils;

import com.ustc.wsn.detector.detectorservice.ReceiveThread;

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
