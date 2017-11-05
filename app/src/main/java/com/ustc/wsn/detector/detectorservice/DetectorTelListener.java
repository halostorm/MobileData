package com.ustc.wsn.detector.detectorservice;
/**
 * Created by halo on 2017/7/1.
 */

import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import com.ustc.wsn.detector.Application.AppResourceApplication;
import com.ustc.wsn.detector.bean.CellInfo;
import com.ustc.wsn.detector.bean.StoreData;

public class DetectorTelListener extends PhoneStateListener {

	private TelephonyManager telManager;
	private AppResourceApplication resource;

	public DetectorTelListener(TelephonyManager tm, AppResourceApplication resource) {
		this.telManager = tm;
		this.resource = resource;
	}

	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
		// TODO Auto-generated method stub
		super.onSignalStrengthsChanged(signalStrength);
		StoreData sd = new StoreData();
		CellInfo cellInfo = new CellInfo();
		try {
			int lac, cellIDs;
			String strength;
			// cellInfo.setTime(System.currentTimeMillis());
			// cellInfo.setDeviceID(telManager.getDeviceId());
			switch (telManager.getPhoneType()) {
			case TelephonyManager.PHONE_TYPE_CDMA:
				CdmaCellLocation location = (CdmaCellLocation) telManager.getCellLocation();
				lac = location.getNetworkId();
				cellIDs = location.getBaseStationId();
				// cellIDs = cellIDs / 16;
				strength = String.valueOf(signalStrength.getCdmaDbm());
				// cellInfo.setOperatorName(telManager.getNetworkOperatorName());
				// cellInfo.setPhoneType(TelephonyManager.PHONE_TYPE_CDMA);
				// cellInfo.setSigStrength(signalStrength.getCdmaDbm());
				// cellInfo.setLac(lac);
				// cellInfo.setBaseID(cellIDs);
				break;
			case TelephonyManager.PHONE_TYPE_GSM:
				GsmCellLocation GSMlocation = (GsmCellLocation) telManager.getCellLocation();
				lac = GSMlocation.getLac();
				// cellIDs = GSMlocation.getCid() & 0x00ffff;
				cellIDs = GSMlocation.getCid();
				// strength = String.valueOf(-113 + 2
				// * signalStrength.getGsmSignalStrength());
				strength = String.valueOf(signalStrength.getGsmSignalStrength());
				// cellInfo.setOperatorName(telManager.getNetworkOperatorName());
				// cellInfo.setPhoneType(TelephonyManager.PHONE_TYPE_GSM);
				// cellInfo.setSigStrength(signalStrength.getGsmSignalStrength());
				// cellInfo.setLac(lac);
				// cellInfo.setBaseID(cellIDs);
				break;
			default:
				cellIDs = -1;
				strength = "-1";
				// cellInfo.setOperatorName("NONE");
				// cellInfo.setPhoneType(TelephonyManager.PHONE_TYPE_NONE);
				// cellInfo.setSigStrength(signalStrength.getGsmSignalStrength());
				// cellInfo.setLac(-1);
				// cellInfo.setBaseID(cellIDs);
				break;
			}
			// Log.i("Cell Info", cellInfo.toString());
			// sd.storeBaseStation(cellInfo);
			resource.updateCellInfo(cellInfo);
		} catch (Exception e) {
		}
	}

	@Override
	public void onDataConnectionStateChanged(int state, int networkType) {
		// TODO Auto-generated method stub
		super.onDataConnectionStateChanged(state, networkType);
	}

	@Override
	public void onCellLocationChanged(CellLocation location) {
		// TODO Auto-generated method stub
		super.onCellLocationChanged(location);
	}

}
