package me.diallo.glowing.ads;

import java.lang.ref.WeakReference;

import android.app.Activity;

import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;

public class GlowingAdChartboost implements AdsPlateform {

	public class MyChartboostDelegate implements ChartboostDelegate {

		@Override
		public void didCacheInterstitial(String arg0) {
		}

		@Override
		public void didCacheMoreApps() {
		}

		@Override
		public void didClickInterstitial(String location) {
			delegate.didClickInterstitial(location, GlowingAdChartboost.this);
		}

		@Override
		public void didClickMoreApps() {

		}

		@Override
		public void didCloseInterstitial(String location) {
		}

		@Override
		public void didCloseMoreApps() {
		}

		@Override
		public void didDismissInterstitial(String location) {
			cb.cacheInterstitial(location);
			delegate.didDismissInterstitial(location, GlowingAdChartboost.this);
		}

		@Override
		public void didDismissMoreApps() {
		}

		@Override
		public void didFailToLoadInterstitial(String unitName) {
			delegate.didFailToLoadInterstitial(unitName, GlowingAdChartboost.this);
		}

		@Override
		public void didFailToLoadMoreApps() {
		}

		@Override
		public void didShowInterstitial(String unitName) {
			delegate.didDisplayedIntertitial(unitName, GlowingAdChartboost.this);
		}

		@Override
		public void didShowMoreApps() {
		}

		@Override
		public boolean shouldDisplayInterstitial(String location) {
			return true;
		}

		@Override
		public boolean shouldDisplayLoadingViewForMoreApps() {
			return true;
		}

		@Override
		public boolean shouldDisplayMoreApps() {
			return true;
		}

		@Override
		public boolean shouldRequestInterstitial(String location) {
			return true;
		}

		@Override
		public boolean shouldRequestInterstitialsInFirstSession() {
			return false;
		}

		@Override
		public boolean shouldRequestMoreApps() {
			return true;
		}

	}

	private Chartboost cb;
	private GlowingAdsDelegate delegate;
	private String defaultUnitName = "Default";

	public GlowingAdChartboost() {
		this.cb = Chartboost.sharedChartboost();
	}

	@Override
	public String getName() {
		return "Chartboost";
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
	public void configure(WeakReference<Activity> cxt,
			GlowingAdsDelegate delegate) {
		cb.onCreate(cxt.get(), "50d6a99216ba47da14000012",
				"0fb9a2227c750ff096df092b832406e4a8237cdd",
				new MyChartboostDelegate());
		this.delegate = delegate;
	}

	@Override
	public void onStop(Activity activty) {
		cb.onStop(activty);
		
	}
	
	@Override
	public void startSession() {
		cb.startSession();
	}

	@Override
	public void cacheDefaultAdUnit() {
		cb.cacheInterstitial();
	}

	@Override
	public void onPause(Activity activty) {
	}

	@Override
	public void onResume(Activity activty) {
	}

	@Override
	public void cacheNamedAdUnit(String unit) {
		cb.cacheInterstitial(unit);
	}

	@Override
	public void showDefaultInterstitial() {
		showNamedInterstitial(this.defaultUnitName, false);
	}

	@Override
	public void showNamedInterstitial(String unit) {
		showNamedInterstitial(unit, false);
	}

	@Override
	public void showNamedInterstitial(String unit, boolean ifCachedOnly) {
		if (ifCachedOnly) {
			if (cb.hasCachedInterstitial(unit)) {
				cb.showInterstitial(unit);
			}
		} else {
			cb.showInterstitial(unit);
		}
	}

	@Override
	public void setDefaultUnitName(String defaultUnitName) {
		this.defaultUnitName = defaultUnitName;
	}

	@Override
	public boolean hasCacheForNamedUnit(String unitName) {
		return cb.hasCachedInterstitial(unitName);
	}

	@Override
	public void onStart(Activity activty) {
		cb.onStart(activty);
	}
	
	@Override
	public boolean onBackPressed() {
		return cb.onBackPressed();
	}
}
