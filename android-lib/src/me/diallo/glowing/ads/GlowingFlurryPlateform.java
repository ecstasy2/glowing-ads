package me.diallo.glowing.ads;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Handler;
import android.widget.FrameLayout;

import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAgent;

public class GlowingFlurryPlateform implements AdsPlateform{

	public class MyAdListenner implements FlurryAdListener {

		@Override
		public void onAdClosed(String arg0) {
			
		}

		@Override
		public void onApplicationExit(String arg0) {
			
		}

		@Override
		public void onRenderFailed(String arg0) {
			
		}

		@Override
		public boolean shouldDisplayAd(String arg0, FlurryAdType type) {
			return true;
		}

		@Override
		public void spaceDidFailToReceiveAd(String unit) {
			
		}

		@Override
		public void spaceDidReceiveAd(String arg0) {
			
		}

	}

	private MyAdListenner adListenner;
	private String defaultUnitName;
	private WeakReference<Activity> contextRef;
	private String apiKey;
	private GlowingAdsDelegate delegate;
	
	public GlowingFlurryPlateform() {
		this.apiKey = "27XJVZNZGRS28T5NNHMR";
	}
	
	@Override
	public String getName() {
		return "Flurry";
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
	public void showDefaultInterstitial() {
		showNamedInterstitial(defaultUnitName, true);
	}

	@Override
	public void showNamedInterstitial(String unit) {
		showNamedInterstitial(unit, true);
	}

	@Override
	public void showNamedInterstitial(String unit, boolean ifCachedOnly) {
		if (contextRef.get() == null) {
			delegate.didFailToLoadInterstitial(unit, this);
		}
		FrameLayout container = new FrameLayout(contextRef.get());
		if (ifCachedOnly) {
			if (FlurryAgent.isAdAvailable(this.contextRef.get(), unit, FlurryAdSize.FULLSCREEN, 0)) {
				FlurryAgent.getAd(this.contextRef.get(), unit, container, FlurryAdSize.FULLSCREEN, 500);
			}
		} else{
			FlurryAgent.getAd(this.contextRef.get(), unit, container, FlurryAdSize.FULLSCREEN, 0);
		}	
	}

	@Override
	public void startSession() {
		if (contextRef.get() != null) {
			FlurryAgent.onStartSession(contextRef.get(), getApiKey());
		}
	}

	protected String getApiKey() {
		return this.apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	@Override
	public void configure(WeakReference<Activity> cxt, GlowingAdsDelegate delegate) {
		this.contextRef = cxt;
		this.delegate = delegate;
		if (cxt.get() != null) {
			FlurryAgent.initializeAds(cxt.get());
			this.adListenner = new MyAdListenner();
			FlurryAgent.setAdListener(adListenner);
		}
	}

	

	@Override
	public void cacheDefaultAdUnit() {
		cacheNamedAdUnit(defaultUnitName);
	}

	@Override
	public void cacheNamedAdUnit(String unit) {
		if (contextRef.get() != null) {
			FlurryAgent.fetchAdsForSpace(this.contextRef.get(), defaultUnitName, FlurryAdSize.FULLSCREEN);
		}
	}

	@Override
	public void onPause(Activity activty) {
	}

	@Override
	public void onResume(Activity activty) {
	}

	@Override
	public void setDefaultUnitName(String defaultUnitName) {
		this.defaultUnitName = defaultUnitName;
	}
	
	@Override
	public boolean hasCacheForNamedUnit(String unitName) {
		return FlurryAgent.isAdAvailable(this.contextRef.get(), unitName, FlurryAdSize.FULLSCREEN, 0);
	}

	@Override
	public void onStart(Activity activty) {
	}

	@Override
	public void onStop(Activity activt) {
		
	}
	
	@Override
	public boolean onBackPressed() {
		if (isRunning(com.flurry.android.FlurryFullscreenTakeoverActivity.class.getName())) {
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					delegate.didDismissInterstitial(null, GlowingFlurryPlateform.this);
				}
			}, 400);
			
			return true;
		}
		
		return false;
	}
	
	public boolean isRunning(String className) {
		Activity context = contextRef.get();
		if (contextRef.get() == null) {
			return false;
		}
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (RunningTaskInfo task : tasks) {
            if (className.equalsIgnoreCase(task.topActivity.getClassName())) 
                return true;                                  
        }

        return false;
    }
}
