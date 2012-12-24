package me.diallo.glowing.ads;

import java.lang.ref.WeakReference;

import android.app.Activity;

import com.jirbo.adcolony.AdColony;
import com.jirbo.adcolony.AdColonyVideoAd;
import com.jirbo.adcolony.AdColonyVideoListener;

public class GlowingAdColony implements AdsPlateform{

	private static final String DefaultUnit1 = "vz1f10610355bb47f4b50c13";
	private static final String DefaultUnit2 = "vze1b02e0d79114d83bcffaf";
	private static final String DefaultUnit3 = "vzc0e2e343b4a54c2bb42295";
	static final String AppSecret = "app33304796b650408dacf4aa";
	protected String defaultUnitName;
	private GlowingAdsDelegate delegate;

	@Override
	public String getName() {
		return "AdColony";
	}

	@Override
	public boolean canCacheAds() {
		return true;
	}

	@Override
	public boolean supportInterstitial() {
		return true;
	}

	@Override
	public void startSession() {}

	@Override
	public void configure(WeakReference<Activity> cxt, GlowingAdsDelegate delegate) {
		AdColony.configure(cxt.get(), "1.0", AppSecret, DefaultUnit1, DefaultUnit2, DefaultUnit3);
		this.delegate = delegate;
	}

	@Override
	public void cacheDefaultAdUnit() {}

	@Override
	public void onStart(Activity activty) {
	}
	
	@Override
	public void onStop(Activity activty) {
		
	}
	
	@Override
	public void onPause(Activity activty) {
		AdColony.pause();
	}

	@Override
	public void onResume(Activity activty) {
		AdColony.resume(activty);
	}

	@Override
	public void cacheNamedAdUnit(String unit) {}

	@Override
	public void showDefaultInterstitial() {
		
	}

	@Override
	public void showNamedInterstitial(String unit) {
		showNamedInterstitial(unit, true);
	}

	@Override
	public void showNamedInterstitial(String unit, boolean ifCachedOnly) {
		AdColonyVideoAd ad = new AdColonyVideoAd(unit);
		
		if (ifCachedOnly) {
			if (hasCacheForNamedUnit(unit)) {
				showAd(unit, ad);
			}
		}else{
			showAd(unit, ad);
		}
	}
	
	protected void showAd(final String unit, AdColonyVideoAd ad){
		ad.show(new AdColonyVideoListener() {
			
			@Override
			public void onAdColonyVideoStarted() {
				delegate.didDisplayedIntertitial(unit, GlowingAdColony.this);
			}
			
			@Override
			public void onAdColonyVideoFinished() {
				delegate.didDismissInterstitial(unit, GlowingAdColony.this);
			}
		});
	}

	@Override
	public void setDefaultUnitName(String defaultUnitName) {
		this.defaultUnitName = defaultUnitName;
	}

	@Override
	public boolean hasCacheForNamedUnit(String unitName) {
		AdColonyVideoAd ad = new AdColonyVideoAd(unitName);
		return ad.isReady();
	}
	
	@Override
	public boolean onBackPressed() {
		return false;
	}

}
