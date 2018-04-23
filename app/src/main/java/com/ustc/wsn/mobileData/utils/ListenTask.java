package com.ustc.wsn.mobileData.utils;

import com.ustc.wsn.mobileData.Listenter.ReceiveThread;

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
