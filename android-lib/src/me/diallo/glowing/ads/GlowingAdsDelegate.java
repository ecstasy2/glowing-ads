package me.diallo.glowing.ads;

public interface GlowingAdsDelegate {

	void didDismissInterstitial(String unitName, AdsPlateform usedPlateform);

	void didClickInterstitial(String unitName, AdsPlateform plateform);

	void didDisplayedIntertitial(String unitName, AdsPlateform usedPlateform);

	void didFailToLoadInterstitial(String unitName, AdsPlateform usedPlateform);

}
