/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.services.updates;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;

import javax.net.ssl.SSLProtocolException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubUpdates extends UpdateService {

	private static final String REPO_API_URL = "https://api.github.com/repos/tonymanpro/Eternity-Pixel.Dungeon";
	private static final String RELEASES_API_URL = REPO_API_URL + "/releases";

	private static Pattern descPattern = Pattern.compile("(.*?)(\r\n|\n|\r)(\r\n|\n|\r)---", Pattern.DOTALL + Pattern.MULTILINE);
	private static Pattern versionCodePattern = Pattern.compile("internal version number: ([0-9]*)", Pattern.CASE_INSENSITIVE);
	private static Pattern tagVersionPattern = Pattern.compile("v?([0-9]+)\\.([0-9]+)(?:\\.([0-9]+))?", Pattern.CASE_INSENSITIVE);

	private static Pattern minAndroidPattern = Pattern.compile("Android .*\\(API ([0-9]*)\\)\\+ Devices", Pattern.CASE_INSENSITIVE);
	private static Pattern minIOSPattern = Pattern.compile("iOS ([0-9]*)\\+ Devices", Pattern.CASE_INSENSITIVE);

	public static int parseVersionCodeFromTag(String tag) {
		if (tag == null) return -1;
		Matcher m = tagVersionPattern.matcher(tag);
		if (m.find()) {
			try {
				int major = Integer.parseInt(m.group(1));
				int minor = Integer.parseInt(m.group(2));
				int patch = m.group(3) != null ? Integer.parseInt(m.group(3)) : 0;
				return major * 1000 + minor * 100 + patch;
			} catch (NumberFormatException ignored) {}
		}
		return -1;
	}

	public static int compareSemver(String v1, String v2) {
		if (v1 == null || v2 == null) return 0;
		Matcher m1 = tagVersionPattern.matcher(v1);
		Matcher m2 = tagVersionPattern.matcher(v2);
		if (!m1.find() || !m2.find()) return 0;

		try {
			int maj1 = Integer.parseInt(m1.group(1));
			int min1 = Integer.parseInt(m1.group(2));
			int pat1 = m1.group(3) != null ? Integer.parseInt(m1.group(3)) : 0;

			int maj2 = Integer.parseInt(m2.group(1));
			int min2 = Integer.parseInt(m2.group(2));
			int pat2 = m2.group(3) != null ? Integer.parseInt(m2.group(3)) : 0;

			if (maj1 != maj2) return Integer.compare(maj1, maj2);
			if (min1 != min2) return Integer.compare(min1, min2);
			return Integer.compare(pat1, pat2);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public boolean supportsUpdatePrompts() {
		return true;
	}

	@Override
	public boolean supportsBetaChannel() {
		return true;
	}

	@Override
	public void checkForUpdate(boolean useMetered, boolean includeBetas, UpdateResultCallback callback) {

		if (!useMetered && !Game.platform.connectedToUnmeteredNetwork()){
			callback.onConnectionFailed();
			return;
		}

		Net.HttpRequest httpGet = new Net.HttpRequest(Net.HttpMethods.GET);
		httpGet.setUrl(RELEASES_API_URL);
		httpGet.setHeader("Accept", "application/vnd.github.v3+json");

		Gdx.net.sendHttpRequest(httpGet, new Net.HttpResponseListener() {
			@Override
			public void handleHttpResponse(Net.HttpResponse httpResponse) {
				try {
					com.badlogic.gdx.utils.JsonValue latestRelease = null;
					int latestVersionCode = Game.versionCode;
					String latestTagName = "";

					String rawJson = httpResponse.getResultAsString();
					if (rawJson != null && rawJson.trim().startsWith("[")) {
						com.badlogic.gdx.utils.JsonValue array = new com.badlogic.gdx.utils.JsonReader().parse(rawJson);
						for (com.badlogic.gdx.utils.JsonValue b = array.child; b != null; b = b.next) {
							String body = b.getString("body", "");
							String tagName = b.getString("tag_name", "");
							String releaseName = b.getString("name", "");

							int releaseVersion = -1;
							Matcher m = versionCodePattern.matcher(body);
							if (m.find()){
								try {
									releaseVersion = Integer.parseInt(m.group(1));
								} catch (NumberFormatException ignored) {}
							}

							// Fallback to tag_name (or releaseName) if internal version number wasn't found in body
							if (releaseVersion <= 0) {
								if (!tagName.isEmpty()) {
									releaseVersion = parseVersionCodeFromTag(tagName);
								}
								if (releaseVersion <= 0 && !releaseName.isEmpty()) {
									releaseVersion = parseVersionCodeFromTag(releaseName);
								}
							}

							if (releaseVersion <= 0) {
								continue;
							}

							// Check if newer than current game version
							boolean isNewer = false;
							if (releaseVersion > Game.versionCode) {
								isNewer = true;
							} else if (!tagName.isEmpty() && Game.version != null && compareSemver(tagName, Game.version) > 0) {
								isNewer = true;
							} else if (!releaseName.isEmpty() && Game.version != null && compareSemver(releaseName, Game.version) > 0) {
								isNewer = true;
							}

							if (!isNewer) {
								continue;
							}

							// Check if this release is newer than previously found release in this check
							if (latestRelease != null) {
								if (releaseVersion < latestVersionCode) {
									continue;
								}
								if (releaseVersion == latestVersionCode && !tagName.isEmpty() && !latestTagName.isEmpty()) {
									if (compareSemver(tagName, latestTagName) <= 0) {
										continue;
									}
								}
							}

							// or that are betas when we haven't opted in
							if (!includeBetas && !b.getBoolean("prerelease", false)){
								continue;

							// or that aren't compatible
							} else if (DeviceCompat.isAndroid()){
								Matcher minAndroid = minAndroidPattern.matcher(body);
								if (minAndroid.find() && DeviceCompat.getPlatformVersion() < Integer.parseInt(minAndroid.group(1))){
									continue;
								}
							} else if (DeviceCompat.isiOS()){
								Matcher minIOS = minIOSPattern.matcher(body);
								if (minIOS.find() && DeviceCompat.getPlatformVersion() < Integer.parseInt(minIOS.group(1))){
									continue;
								}
							}

							latestRelease = b;
							latestVersionCode = releaseVersion;
							latestTagName = tagName;
						}
					}

					if (latestRelease == null){
						callback.onNoUpdateFound();
					} else {

						AvailableUpdateData update = new AvailableUpdateData();

						String relName = latestRelease.getString("name", "");
						if (relName.isEmpty()) relName = latestRelease.getString("tag_name", "");
						update.versionName = relName;
						update.versionCode = latestVersionCode;

						String body = latestRelease.getString("body", "");
						Matcher m = descPattern.matcher(body);
						if (m.find()) {
							update.desc = m.group(1);
						} else {
							update.desc = body != null ? body.replaceAll("(?i)internal version number:\\s*[0-9]+", "").trim() : "";
						}
						update.URL = latestRelease.getString("html_url", "");

						callback.onUpdateAvailable(update);
					}
				} catch (Exception e) {
					Game.reportException( e );
					callback.onConnectionFailed();
				}
			}

			@Override
			public void failed(Throwable t) {
				//Failure in SSL handshake, possibly because GitHub requires TLS 1.2+.
				// Often happens for old OS versions with outdated security protocols.
				// Future update attempts won't work anyway, so just pretend nothing was found.
				if (t instanceof SSLProtocolException){
					callback.onNoUpdateFound();
				} else {
					Game.reportException(t);
					callback.onConnectionFailed();
				}
			}

			@Override
			public void cancelled() {
				callback.onConnectionFailed();
			}
		});

	}

	@Override
	public void initializeUpdate(AvailableUpdateData update) {
		Game.platform.openURI( update.URL );
	}

	@Override
	public boolean supportsReviews() {
		return false;
	}

	@Override
	public void initializeReview(ReviewResultCallback callback) {
		//does nothing, no review functionality here
		callback.onComplete();
	}

	@Override
	public void openReviewURI() {
		//does nothing
	}
}
