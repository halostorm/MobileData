package com.ustc.wsn.mobileData.Listenter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ReceiveThread {

	private Thread mThread;
	public Socket socket;

	public int sendNumber = 0;

	public int blockSize = 5;

	public ReceiveThread(Socket socket) {
		this.socket = socket;
		mThread = new Thread(runnable);
		mThread.start();
	}

	Runnable runnable = new Runnable() {
		public void run() {

			try {
				/*
				 * Reader reader = new
				 * InputStreamReader(socket.getInputStream());
				 *
				 * CharBuffer charBuffer = CharBuffer.allocate(1024); int
				 * readIndex = -1; while ((readIndex = reader.read(charBuffer))
				 * != -1) { charBuffer.flip(); String temp =
				 * charBuffer.toString(); //System.out.println("server-->" +
				 * temp); sendNumber = Integer.valueOf(temp); }
				 */
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String msg;
				while ((msg = bufferedReader.readLine()) != null) {
					sendNumber = Integer.valueOf(msg);
				}
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (socket != null) {
					if (!socket.isClosed()) {
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			}

		}
	};

	private boolean reConnect() {
		socket = null;
		try {
			socket = new Socket("219.219.216.181", 8080);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean isConnected() {
		try {
			socket.sendUrgentData(0xFF);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public Boolean isServerClose(Socket socket) {
		try {
			socket.sendUrgentData(0);// 发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
			return false;
		} catch (Exception se) {
			return true;
		}
	}

}
