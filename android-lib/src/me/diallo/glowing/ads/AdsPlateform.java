package me.diallo.glowing.ads;

import java.lang.ref.WeakReference;

import android.app.Activity;

public interface AdsPlateform {
	public String getName();
	public boolean canCacheAds();
	public boolean supportInterstitial();
	public void showDefaultInterstitial();
	public void showNamedInterstitial(String unit);
	public void showNamedInterstitial(String unit, boolean ifCachedOnly);
	public void startSession();
	public void configure(WeakReference<Activity> cxt, GlowingAdsDelegate delegate);
	public void cacheDefaultAdUnit();
	public void cacheNamedAdUnit(String unit);
	public void onPause(Activity activty);
	public void onResume(Activity activty);
	void setDefaultUnitName(String defaultUnitName);
	public boolean hasCacheForNamedUnit(String unitName);
	public void onStart(Activity activty);
	public void onStop(Activity activty);
	public boolean onBackPressed();
}
