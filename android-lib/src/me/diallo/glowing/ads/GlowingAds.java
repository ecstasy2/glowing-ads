package me.diallo.glowing.ads;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;

import com.google.gson.Gson;

public class GlowingAds {

	public interface Callback {
		void onShowIntertitialResult(boolean displayed, AdsPlateform plateform);

		void didDismissIntertitial(String unitName, AdsPlateform usedPlateform);
	}

	private AdsPlateform[] plateforms;
	private boolean shouldPrecacheAds;
	public GlowingAdChartboost CHART_BOOST;
	public GlowingAdColony AD_COLONY;
	public GlowingFlurryPlateform FLURRY;
	private GlowingAdsSettings settings;
	
	private Handler handler;
	
	private Object lock = new Object();
	
	private Callback visibleIntertitialCallback;
	private WeakReference<Activity> context;
	private GlowingAdsDelegate wrappedDelegate;

	private static String SETTINGS_URL = "http://sudoku.gurumobileapps.com/glowing-ads/settings.json";
	private static GlowingAds _instance;

	public GlowingAds() {
		this.CHART_BOOST = new GlowingAdChartboost();
		this.AD_COLONY = new GlowingAdColony();
		this.FLURRY = new GlowingFlurryPlateform();

		plateforms = new AdsPlateform[] { CHART_BOOST , AD_COLONY, FLURRY};

		reloadSettings();

		this.handler = new Handler();
		preCacheAds(true);
	}

	public static GlowingAds instance() {
		if (_instance == null) {
			_instance = new GlowingAds();
		}
		return _instance;
	}

	public void onCreate(Activity cxt, GlowingAdsDelegate delegate) {
		this.wrappedDelegate = delegate;
		this.context = new WeakReference<Activity>(cxt);
		for (AdsPlateform plateform : plateforms) {
			plateform.configure(context, this.delegate);

			if (this.shouldPrecacheAds && plateform.canCacheAds()) {
				plateform.cacheDefaultAdUnit();
			}
		}
	}

	public void onStart(Activity activty) {
		for (AdsPlateform plateform : plateforms) {
			plateform.onStart(activty);
		}
	}

	public void onPause(Activity activty) {
		for (AdsPlateform plateform : plateforms) {
			plateform.onPause(activty);
		}
	}

	public void onResume(Activity activty) {
		for (AdsPlateform plateform : plateforms) {
			plateform.onResume(activty);
		}
	}

	public void onStop(Activity cxt) {
		for (AdsPlateform plateform : plateforms) {
			plateform.onStop(cxt);
		}
	}

	public void startSession() {
		for (AdsPlateform plateform : plateforms) {
			plateform.startSession();
		}
	}

	/**
	 * Call this method to display an intertitial, if the plateform choosed by
	 * the current strategy support precaching ads and a ads is available from
	 * the cache, it will be displayed from there.
	 * 
	 * If the plateform don't support cache then it will request the intertitial
	 * and display it.
	 * 
	 * Note: If the loading of the ads fail, we ask the strategy for another
	 * plateform until we get an ad or there are not ads available at all. At
	 * which point GlowingAdsDelegate.didFailToLoadInterstitial() will be
	 * called.
	 */
	public void showInterstitial(final String unitName, final Callback callback) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				AdsPlateform[] copy = sortedPlateforms(unitName);

				boolean displayed = false;
				AdsPlateform usedPlateform = null;
				boolean ifCachedOnly = true;
				if (ifCachedOnly) {
					synchronized (lock) {
						if (settings == null || settings.map == null) {
							callback.onShowIntertitialResult(false,
									null);
							return;
						}
						for (AdsPlateform plateform : copy) {
							if (plateform.canCacheAds()) {
								AdUnitPlateform realUnit = settings.map
										.get(unitName + "_" + plateform.getName());
								String realUnitname = realUnit != null ? realUnit.unitName
										: "Default";
								if (plateform.hasCacheForNamedUnit(realUnitname)) {
									plateform.showNamedInterstitial(realUnitname,
											ifCachedOnly);
									displayed = true;
									usedPlateform = plateform;
									break;
								}
							}
						}
					}

					final boolean mydisplayed = displayed;
					final AdsPlateform myusedPlateform = usedPlateform;

					handler.post(new Runnable() {

						@Override
						public void run() {
							if (delegate != null) {
								if (mydisplayed) {
									delegate.didDisplayedIntertitial(unitName,
											myusedPlateform);
								} else {
									delegate.didFailToLoadInterstitial(unitName, myusedPlateform);
								}
							}

							if (callback != null) {
								visibleIntertitialCallback = callback;
								callback.onShowIntertitialResult(mydisplayed,
										myusedPlateform);
							}
						}
					});

				}
			}

		}).start();

		return;
	}

	/**
	 * Call this method from Activity.onBackPressed() if there is an ad
	 * currently displayed it will be hidden and true is returned.
	 * 
	 * @return
	 */
	public boolean onBackPressed() {
		for (AdsPlateform plateform : plateforms) {
			if (plateform.onBackPressed()) {
				return true;
			}
		}
		return false;
	}

	public void preCacheAds(boolean precache) {
		this.shouldPrecacheAds = precache;
	}

	public void reloadSettings() {
		new AsyncTask<String, Integer, InputStream>() {
			@Override
			protected InputStream doInBackground(String... urls) {
				HttpGet httpGet = new HttpGet(urls[0]);
				HttpClient httpClient = new DefaultHttpClient();
				final HttpParams httpParameters = httpClient.getParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters,
						20 * 1000);

				try {
					HttpResponse response = httpClient.execute(httpGet);
					StatusLine statusLine = response.getStatusLine();
					int status = 0;
					if (statusLine != null) {
						status = statusLine.getStatusCode();
					}
					if (status == 200) {
						BufferedReader br = new BufferedReader(
								new InputStreamReader(response.getEntity()
										.getContent()));

						StringBuilder sb = new StringBuilder();

						String line;
						while ((line = br.readLine()) != null) {
							sb.append(line);
						}

						String theString = sb.toString();

						return new ByteArrayInputStream(theString.getBytes());
					} else
						throw new Exception();
				} catch (Exception e) {
				}
				return null;
			}

			protected void onPostExecute(InputStream inputStream) {
				try {
					parseSettings(inputStream);
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}.execute(SETTINGS_URL);
	}

	private void parseSettings(InputStream inputStream) throws JSONException,
			IOException {
		if (inputStream != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					inputStream));

			StringBuilder sb = new StringBuilder();

			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			String theString = sb.toString();

			parseSettings(theString);
		}
	}

	private void parseSettings(String json) {
		synchronized (lock) {
			Gson gson = new Gson();
			this.settings = gson.fromJson(json, GlowingAdsSettings.class);
			HashMap<String, AdUnitPlateform> map = new HashMap<String, GlowingAds.AdUnitPlateform>();
			for (AdUnit unit : settings.units) {
				for (AdUnitPlateform plateform : unit.plateforms) {
					map.put(unit.id + "_" + plateform.plateform, plateform);
				}
			}
			this.settings.setUnitsPlateformsMap(new ConcurrentHashMap<String, GlowingAds.AdUnitPlateform>(map));

			// Precache those units

			for (AdUnitPlateform tmp : map.values()) {
				for (AdsPlateform plateform : plateforms) {
					if (plateform.canCacheAds() && plateform.getName().equals(tmp.plateform)) {
						plateform.cacheNamedAdUnit(tmp.unitName);
					}
				}	
			}
		}
	}

	static class GlowingAdsSettings {
		List<AdUnit> units;
		String selection_strategy;
		ConcurrentMap<String, AdUnitPlateform> map = new ConcurrentHashMap<String, GlowingAds.AdUnitPlateform>();

		public void setUnitsPlateformsMap(ConcurrentMap<String, AdUnitPlateform> map) {
			this.map = map;
		}
	}

	static class AdUnit {
		String id;
		AdUnitPlateform[] plateforms;
	}

	static class AdUnitPlateform {
		String plateform;
		String unitName;
		int priority;
	}
	
	
	private GlowingAdsDelegate delegate = new GlowingAdsDelegate() {
		
		@Override
		public void didClickInterstitial(String unitName, AdsPlateform plateform) {
			if (wrappedDelegate != null) {
				wrappedDelegate.didClickInterstitial(unitName, plateform);
			}
		}
		
		@Override
		public void didDismissInterstitial(String unitName,
				AdsPlateform usedPlateform) {
			if (visibleIntertitialCallback != null) {
				visibleIntertitialCallback.didDismissIntertitial(unitName, usedPlateform);
				visibleIntertitialCallback = null;
			}
			
			if (wrappedDelegate != null) {
				wrappedDelegate.didDismissInterstitial(unitName, usedPlateform);
			}
		}
		
		@Override
		public void didFailToLoadInterstitial(String unitName, AdsPlateform usedPlateform) {
			AdsPlateform[] copy = sortedPlateforms(unitName);
			boolean foundCurrent = false;
			AdsPlateform nextPlateform = null;
			for (AdsPlateform adsPlateform : copy) {
				if (foundCurrent) {
					nextPlateform = adsPlateform;
					break;
				}
				if (adsPlateform.getName().equals(usedPlateform.getName())) {
					foundCurrent = true;
				}
			}
			
			if (nextPlateform != null) {
				
			}
			
			if (wrappedDelegate != null) {
				wrappedDelegate.didFailToLoadInterstitial(unitName, usedPlateform);
			}
		}
		
		@Override
		public void didDisplayedIntertitial(String unitName,
				AdsPlateform usedPlateform) {
			
			if (wrappedDelegate != null) {
				wrappedDelegate.didDisplayedIntertitial(unitName, usedPlateform);
			}
		}
	};
	
	private AdsPlateform[] sortedPlateforms(final String unitName) {
		AdsPlateform[] copy = new AdsPlateform[plateforms.length];
		int i = 0;
		for (AdsPlateform adsPlateform : plateforms) {
			copy[i++] = adsPlateform;
		}
		Arrays.sort(copy, new Comparator<AdsPlateform>() {

			@Override
			public int compare(AdsPlateform lhs, AdsPlateform rhs) {
				if (settings == null) {
					return lhs.getName().compareTo(rhs.getName());
				}

				AdUnitPlateform left = settings.map.get(unitName + "_"
						+ lhs.getName());
				AdUnitPlateform right = settings.map.get(unitName + "_"
						+ rhs.getName());

				Integer leftPrio = left != null ? left.priority
						: Integer.MAX_VALUE;
				Integer rightPrio = right != null ? right.priority
						: Integer.MAX_VALUE;

				return leftPrio.compareTo(rightPrio);
			}
		});
		return copy;
	}
}