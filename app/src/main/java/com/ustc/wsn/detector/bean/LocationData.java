package com.ustc.wsn.detector.bean;

import android.location.Location;

import com.ustc.wsn.detector.utils.TimeUtil;

public class LocationData {

	private double longitude;
	private double latitude;
	private float speed;
	private long time;
	private float angle;
	// private Location currentBestLocation;
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	//
	// public LocationData(Location loc) {
	// this.currentBestLocation = loc;
	// }

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	public static boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	public LocationData(double longitude, double latitude, long time, float speed,float angle) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.time = time;
		this.speed = speed;
		this.angle = angle;

	}

	public double getLongitude() {
		return longitude;
	}
	
	public float getAngle() {
		return angle;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	@Override
	public String toString() {
		// SimpleDateFormat formatter = new SimpleDateFormat(
		// "yyyy/MM/dd HH:mm:ss:sss");
		// Date curDate = new Date(time);
		// return formatter.format(curDate) + "\t" + time + "\t" + longitude
		// + "\t" + latitude;
		return TimeUtil.getTime(time) + "\t" + time + "\t" + longitude + "\t" + latitude + "\t" + speed+ "\t" + angle;
		// Date curDate = new Date(currentBestLocation.getTime());
		// return formatter.format(curDate) + "\t" +
		// currentBestLocation.getTime() + "\t"
		// + currentBestLocation.getLongitude() + "\t" +
		// currentBestLocation.getLatitude();
		// return "LocationData [longitude=" + longitude + ", latitude="
		// + latitude + ", time=" + time + "]";
	}

}
