package com.ustc.wsn.mobileData.Application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.os.Message;

import com.ustc.wsn.mobileData.Listenter.ReceiveThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

@SuppressLint("ShowToast")
public class ExceptionUtil extends Application {

	private Thread mThread;

	public Socket socket;

	public boolean isBusy = false;
	public boolean sendSuccessFlag = true;
	private int sendNumber = 0;

	public int messageQueueSize = 2000;
	public int blockSize = 5;
	public int msgSendThreshold = 20;

	private int lastsenditer = 0;

	public Vector<String> messageQueue = new Vector<String>();

	public static final int EXCEPTION_ROAD = 1;
	public static final int EXCEPTION_STOP = 2;
	public static final int EXCEPTION_SHIFT = 3;

	ReceiveThread receiveThread;

	public ExceptionUtil() {
		// this.data=data;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// handle the UI
		}
	};

	Runnable runnable = new Runnable() {
		public void run() {

			// socket = null;
			int port = 8080;
			try {
				isBusy = true;
				socket = new Socket("219.219.216.181", port);// input the
																// Server,
																// Address
				// receiveThread = new ReceiveThread(socket);

				sendNumber = messageQueue.size();
				socket.setKeepAlive(true);
				socket.setTcpNoDelay(true);
				socket.setSendBufferSize(1024 * 256);
				sendMessageToServer();
				isBusy = false;
			} catch (Exception e) {
				int reConCounter = 0;
				isBusy = true;
				while (reConCounter++ <= 5000001) {
					if (reConCounter % 1000000 == 1) {
						if (reConnect() == true) {
							// SEND
							isBusy = true;
							try {
								sendMessageToServer();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							isBusy = false;
						} else {
							sendSuccessFlag = false;
						}
					}
				}

				isBusy = false;
			} //

			// Log.d("Report Exception", "Report Over");
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mHandler.sendEmptyMessage(0);

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

	private void ReceiveMessage() {
	}

	private BufferedReader getReader(Socket socket) throws IOException {
		InputStream socketIn = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(socketIn));
	}

	private void sendMessageToServer() throws IOException {
		// String totalMessage = "";
		sendNumber = messageQueue.size();
		int blockNumber = sendNumber / blockSize;
		OutputStream socketOut = socket.getOutputStream();
		for (int i = 0; i <= blockNumber; i++) {
			// socketOut = socket.getOutputStream();
			String packageMessage = "\n";
			for (int j = blockSize * i; j < blockSize * (i + 1) && j < sendNumber; j++) {
				String accMsg = j + " " + sendNumber + " " + messageQueue.elementAt(0);
				packageMessage = packageMessage + "\n" + accMsg;
				// if(j == sendNumber-1)
				// packageMessage += "\n" + accMsg;
				// messageQueue.remove(0);
			}

			// totalMessage += "\n"+packageMessage;
			// packageMessage += "\n end \n";
			// String returnMsg;
			// while((returnMsg = bufferedReader.readLine())!=null)

			socketOut.write(packageMessage.toString().getBytes());
			socketOut.flush();
			/*
			 * if(isConnected()) {
			 * 
			 * sendNumber -= blockNumber; lastsenditer += blockNumber; for(int p
			 * = 1; p <= blockNumber; p++) messageQueue.remove(0); } else {
			 * socket.shutdownOutput(); while(sendNumber > 0) { socket.close();
			 * reConnect(); sendMessageToServer(); } return; }
			 */

			// int counter = 1;
			// while(counter++ < 30000000);
		}

		socketOut.write(("cellphone is over").toString().getBytes());
		socketOut.flush();

		//
		System.out.println(receiveThread.sendNumber);
		sendSuccessFlag = true;
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

	/**
	 * 涓婃姤寮傚父
	 * 
	 * @param type
	 *            寮傚父绉嶇被锛�: EXCEPTION_ROAD, 2:EXCEPTION_STOP, 3:EXCEPTION_SHIFT
	 */
	public void reportException(int type/* , LocationData currLoc, long time */) {
		// Toast.makeText(this, "Location changed, Send Data to Server!",
		// Toast.LENGTH_LONG).show();
		// currLocation = currLoc;
		// currTime = time;
		if (isBusy == false) {
			/*
			 * switch(type){ case EXCEPTION_ROAD: Log.e("Road", "Excetion");
			 * mThread = new Thread(runnable); mThread.start(); Log.e("Road",
			 * "Report Over"); break; case EXCEPTION_STOP: Log.e("Driving Stop",
			 * "Suddenly Stop or Start"); mThread = new Thread(runnable);
			 * mThread.start(); Log.e("Driving Stop", "Report Over"); break;
			 * case EXCEPTION_SHIFT: Log.e("Driving Shift", "Suddenly Shift");
			 * mThread = new Thread(runnable); mThread.start();
			 * Log.e("Driving Shift", "Report Over"); break; }
			 */
			isBusy = true;
			mThread = new Thread(runnable);
			mThread.start();
		} else {
			return;
		}
	}
}

/*
 * private void sendMessageToServer() throws IOException { for (int p =
 * (lastSendPointer)%10+1; p <= 10; p++) { if(messageQCounter[p] ==
 * messageQueueSize) { lastSendPointer++; totalQCounter -= messageQueueSize;
 * messageQCounter[p] = 0; String totalMessage = ""; int blockSize = 5; int
 * blockNumber = messageQueueSize/blockSize; OutputStream socketOut =
 * socket.getOutputStream(); for (int i = 0; i <= blockNumber; i++) { String
 * packageMessage = ""; for (int j = blockSize*i; j < blockSize*(i+1) && j <
 * messageQCounter; j++) { String accMsg = "("+j+")"+messageQueue.elementAt(j);
 * //String message = accMsg + currLocation.toString(); packageMessage =
 * packageMessage + "\n" + accMsg; }
 * 
 * totalMessage += "\n"+packageMessage;
 * socketOut.write(packageMessage.toString().getBytes()); socketOut.flush(); }
 * 
 * socketOut.write(("Data in Buffer"+p).toString().getBytes());
 * 
 * for (int i = 0; i < blockNumber; i++) { String packageMessage = "\n"; for
 * (int j = blockSize*i+1; j <= blockSize*(i+1); j++) { //totalQCounter--;
 * //messageQCounter[p]--; String accMsg = "("+j+")" + messageQueue[p][j]; + " "
 * + totalQCounter +" "+ messageQCounter; //String message = accMsg +
 * currLocation.toString();
 * 
 * packageMessage = packageMessage + "\n" + accMsg;
 * 
 * if(j == messageQueueSize-1) packageMessage = packageMessage + "\n" +
 * "("+messageQueueSize+")" + messageQueue[p][messageQueueSize]; }
 * 
 * + " " + totalQCounter +" "+ messageQCounter; //String message = accMsg +
 * currLocation.toString();
 * 
 * totalMessage += "\n"+packageMessage;
 * socketOut.write(packageMessage.toString().getBytes());
 * 
 * }
 * 
 * socketOut.write(("Data Send End.").toString().getBytes());
 * 
 * if(socketOut != null) { socketOut.flush(); socketOut.close(); }
 * 
 * 
 * 
 * //messageQCounter[p] = 0;
 * 
 * StoreData sd=new StoreData(); sd.storeExceptionExptLocation(totalMessage,
 * ExceptionUtil.EXCEPTION_ROAD); messageQCounter[p] = 0; } } sendSuccessFlag =
 * true; }
 */
