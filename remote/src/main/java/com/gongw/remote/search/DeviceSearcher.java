package com.gongw.remote.search;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.gongw.remote.Device;
import com.gongw.remote.RemoteConst;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用于搜索局域网中的设备
 */
public class DeviceSearcher {

	private static final String TAG = "DeviceSearcher";
	private static ExecutorService executorService = Executors.newSingleThreadExecutor();
	private static Handler uiHandler = new Handler(Looper.getMainLooper());
	private static SearchRunnable searchRunnable;
	/**
	 * 开始搜索
	 * @param onSearchListener
	 */
	public static void search(OnSearchListener onSearchListener){
//		executorService.execute(new SearchRunnable(onSearchListener));
		if (searchRunnable == null) {
			searchRunnable = new SearchRunnable(onSearchListener);
		}
		new Thread(searchRunnable).start();
	}

	public interface OnSearchListener{
		void onSearchStart();
		void onSearchedNewOne(Device device);
		void onSearchFinish();
		void onSearchException();
		void onSearchBindFail();
	}

	public static boolean isCloseSocket(){
		if (searchRunnable!=null){
			return searchRunnable.isClose();
		}else{
			return true;
		}
	}

	private static class SearchRunnable implements Runnable {

		OnSearchListener searchListener;
		DatagramSocket socket ;
		public SearchRunnable(OnSearchListener listener){
			this.searchListener = listener;
		}

		private void close(){
			if (socket!=null) {
				socket.close();
				socket = null;
			}
		}

		private boolean isClose(){
			if (socket==null) {
				return true;
			}else{
				return false;
			}
		}

		@Override
		public void run() {
			try {
				if(searchListener!=null){
					uiHandler.post(new Runnable() {
						@Override
						public void run() {
							searchListener.onSearchStart();
						}
					});
				}
				if (socket == null) {
					socket = new DatagramSocket(RemoteConst.DEVICE_SEARCH_PORT);
				}
				//设置接收等待时长
				socket.setSoTimeout(RemoteConst.RECEIVE_TIME_OUT);
				byte[] sendData = new byte[1024];
				byte[] receData = new byte[1024];
                DatagramPacket recePack = new DatagramPacket(receData, receData.length);
                //使用广播形式（目标地址设为255.255.255.255）的udp数据包
                DatagramPacket sendPacket = new DatagramPacket(
                		sendData,
						sendData.length,
						InetAddress.getByName("255.255.255.255"),
						RemoteConst.DEVICE_SEARCH_PORT);
                //用于存放已经应答的设备
                HashMap<String, Device> devices = new HashMap<>();
                //搜索指定次数
				for(int i=0;i<RemoteConst.SEARCH_DEVICE_TIMES;i++) {
					Log.i(TAG, " =====第" + (i + 1) + "次搜索 ===== ");
					sendPacket.setData(packSearchData(i + 1));
					//发送udp数据包
					socket.send(sendPacket);
					//限定搜索设备的最大数量
					int rspCount = RemoteConst.SEARCH_DEVICE_MAX;
					while (rspCount > 0) {
//						Log.i(TAG, "respCount ... " + rspCount);
						socket.receive(recePack);
						Log.i(TAG,"receive datagramPacket");
						final Device device = parseRespData(recePack);
						if (device==null) {
							Log.e(TAG,"********** response data is null **********");
							if (searchListener!=null) {
								uiHandler.post(new Runnable() {
									@Override
									public void run() {
										searchListener.onSearchException();
									}
								});
							}
							return;
						}
						if (devices.get(device.getIp()) == null) {
							//保存新应答的设备
							devices.put(device.getIp(), device);
							if (searchListener != null) {
								uiHandler.post(new Runnable() {
									@Override
									public void run() {
										searchListener.onSearchedNewOne(device);
									}
								});
							}
						}
						rspCount--;
					}
					Log.i(TAG,"execute while method finish");
				}
			} catch (IOException e) {
				e.printStackTrace();
				if (searchListener!=null) {
					uiHandler.post(new Runnable() {
						@Override
						public void run() {
							searchListener.onSearchException();
						}
					});
				}
			}finally {
				close();
			}
		}

        /**
         * 校验和解析应答的数据包
		 * @param pack udp数据包
		 * @return
         */
		private Device parseRespData(DatagramPacket pack) {
//			if (pack.getLength() < 2) {
//	            return null;
//	        }
	        byte[] data = pack.getData();
//	        int offset = pack.getOffset();
//	        Log.i(TAG,"offset ... "+offset);
//	       	int prefix = data[offset++];
//	       	int response = data[offset++];
//	       	Log.i(TAG,"DeviceSearcher parseRespData prefix ... "+data[0]+" response ... "+data[1]);
	        //检验数据包格式是否符合要求
//	        if (prefix != RemoteConst.PACKET_PREFIX || response != RemoteConst.PACKET_TYPE_SEARCH_DEVICE_RSP) {
//	            return null;
//	        }
//	        int length = data[offset++];
			String name = null;
			try {
//				name = new String(data, offset, length,"UTF-8");
				name = new String(data, 0, data.length,"UTF-8");
				name = name.trim();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			String ip = pack.getAddress().getHostAddress();
        	int port = pack.getPort();
        	String os = "";
			if (!TextUtils.isEmpty(name)) {
//				if (name.contains("$")) {
//					os = Device.ANDROID;
//				}else{
//					os = Device.PC;
//				}
				if (name.contains("-PHONE")) {
					os = Device.ANDROID;
				}else{
					os = Device.WINDOWS;
				}
			}
			Log.i(TAG, "name ... "+name+" ip ... " + ip + ":" + port);
			return new Device(ip, port, name,os);
		}

		/**
         * 生成搜索数据包
		 * 格式：$(1) + packType(1) + sendSeq(4) + dataLen(1) + [data]
		 *  packType - 报文类型
		 *  sendSeq - 发送序列
		 *  dataLen - 数据长度
		 *  data - 数据内容
		 * @param seq
         * @return
         */
		private byte[] packSearchData(int seq) {
			byte[] data = new byte[6];
			int offset = 0;
			data[offset++] = RemoteConst.PACKET_PREFIX;
			data[offset++] = RemoteConst.PACKET_TYPE_SEARCH_DEVICE_REQ;
			data[offset++] = (byte) seq;
			data[offset++] = (byte) (seq >> 8);
			data[offset++] = (byte) (seq >> 16);
			data[offset++] = (byte) (seq >> 24);
			Log.i(TAG,"data[0] ... "+data[0]+ "data[1] .. "+data[1]);
			return data;
		}
	}

	public static InetAddress getBroadcastAddress(Context context) throws UnknownHostException {
		WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		if(dhcp==null) {
			return InetAddress.getByName("255.255.255.255");
		}
		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		return InetAddress.getByAddress(quads);
	}


}
