/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * Eternity Pixel Dungeon
 * Copyright (C) 2026 Eternity Pixel Dungeon Contributors
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

package com.shatteredpixel.shatteredpixeldungeon.android;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.billingclient.api.*;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.services.platform.SupporterManager;
import com.watabou.utils.Callback;

import java.util.*;

/**
 * AndroidBillingHandler: Manages Google Play In-App Billing (IAP) and Supporter Donations.
 */
public class AndroidBillingHandler implements PurchasesUpdatedListener, BillingClientStateListener {

	private static final String TAG = "EPD-Billing";

	public static final String SKU_FULL_UNLOCK = "full_unlock";

	private static final List<String> ALL_SKUS = Collections.singletonList(
			SKU_FULL_UNLOCK
	);

	private static AndroidBillingHandler instance;

	private final Activity activity;
	private BillingClient billingClient;
	private boolean isConnected = false;

	private final Map<String, ProductDetails> productDetailsMap = new HashMap<>();
	private Callback pendingPurchaseCallback;

	public AndroidBillingHandler(Activity activity) {
		this.activity = activity;
		instance = this;
		Log.i(TAG, "AndroidBillingHandler initialized for activity: " + activity.getClass().getSimpleName());
		initialize();
	}

	public static AndroidBillingHandler get() {
		return instance;
	}

	private void initialize() {
		try {
			Log.i(TAG, "Building BillingClient with PendingPurchases enabled...");
			billingClient = BillingClient.newBuilder(activity)
					.setListener(this)
					.enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
					.build();
			startConnection();
		} catch (Throwable t) {
			Log.e(TAG, "Failed to initialize BillingClient", t);
		}
	}

	public void startConnection() {
		if (billingClient == null) {
			Log.w(TAG, "startConnection: billingClient is null");
			return;
		}
		try {
			Log.i(TAG, "Connecting to Google Play Billing service...");
			billingClient.startConnection(this);
		} catch (Throwable t) {
			Log.e(TAG, "startConnection failed", t);
		}
	}

	@Override
	public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
		Log.i(TAG, "onBillingSetupFinished: responseCode=" + billingResult.getResponseCode() + ", msg=" + billingResult.getDebugMessage());
		if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
			isConnected = true;
			queryProducts();
			restorePurchases(null);
		} else {
			isConnected = false;
			Log.w(TAG, "Billing setup was NOT OK: " + billingResult.getDebugMessage());
		}
	}

	@Override
	public void onBillingServiceDisconnected() {
		Log.w(TAG, "onBillingServiceDisconnected: Connection lost, will retry on demand.");
		isConnected = false;
	}

	/**
	 * Queries available supporter product details from Google Play.
	 */
	public void queryProducts() {
		if (billingClient == null || !isConnected) {
			Log.w(TAG, "queryProducts aborted: billingClient is not connected");
			return;
		}

		Log.i(TAG, "Querying Google Play ProductDetails for SKUs: " + ALL_SKUS);
		List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
		for (String sku : ALL_SKUS) {
			productList.add(
					QueryProductDetailsParams.Product.newBuilder()
							.setProductId(sku)
							.setProductType(BillingClient.ProductType.INAPP)
							.build()
			);
		}

		QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
				.setProductList(productList)
				.build();

		billingClient.queryProductDetailsAsync(params, (billingResult, result) -> {
			Log.i(TAG, "queryProductDetailsAsync finished: responseCode=" + billingResult.getResponseCode() + ", msg=" + billingResult.getDebugMessage());
			if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && result.getProductDetailsList() != null) {
				Log.i(TAG, "Google Play returned " + result.getProductDetailsList().size() + " products.");
				for (ProductDetails details : result.getProductDetailsList()) {
					Log.i(TAG, " -> Product: ID=" + details.getProductId() + ", Name=" + details.getName() + ", Title=" + details.getTitle());
					productDetailsMap.put(details.getProductId(), details);
				}
			} else {
				Log.w(TAG, "queryProductDetailsAsync did not return products. Response: " + billingResult.getResponseCode() + " " + billingResult.getDebugMessage());
			}
		});
	}

	/**
	 * Launches the Google Play in-app purchase flow for the specified supporter tier rank.
	 */
	public void purchaseSupporter(int tierRank, Callback callback) {
		this.pendingPurchaseCallback = callback;

		String targetSku = SKU_FULL_UNLOCK;

		Log.i(TAG, "purchaseSupporter requested for tierRank=" + tierRank + ", targetSku=" + targetSku + ", isConnected=" + isConnected);

		if (billingClient == null || !isConnected) {
			Log.w(TAG, "purchaseSupporter: Not connected to Google Play Billing! Attempting reconnection...");
			startConnection();
			activity.runOnUiThread(() -> {
				Toast.makeText(activity, "Conectando con Google Play... Intenta de nuevo en unos segundos.", Toast.LENGTH_SHORT).show();
			});
			return;
		}

		ProductDetails details = productDetailsMap.get(targetSku);
		if (details != null) {
			launchBilling(details);
		} else {
			Log.i(TAG, "ProductDetails for '" + targetSku + "' not cached yet. Querying Google Play...");
			List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
			for (String sku : ALL_SKUS) {
				productList.add(
						QueryProductDetailsParams.Product.newBuilder()
								.setProductId(sku)
								.setProductType(BillingClient.ProductType.INAPP)
								.build()
				);
			}
			final String finalTarget = targetSku;
			billingClient.queryProductDetailsAsync(
					QueryProductDetailsParams.newBuilder().setProductList(productList).build(),
					(billingResult, result) -> {
						List<ProductDetails> list = result != null ? result.getProductDetailsList() : null;
						Log.i(TAG, "queryProductDetailsAsync on purchase: code=" + billingResult.getResponseCode() + ", found=" + (list != null ? list.size() : 0));
						if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null && !list.isEmpty()) {
							ProductDetails found = null;
							for (ProductDetails pd : list) {
								productDetailsMap.put(pd.getProductId(), pd);
								Log.i(TAG, " -> Cached SKU: " + pd.getProductId() + " (" + pd.getTitle() + ")");
								if (pd.getProductId().equalsIgnoreCase(finalTarget)) {
									found = pd;
								}
							}
							if (found == null && !list.isEmpty()) found = list.get(0);
							if (found != null) {
								final ProductDetails toLaunch = found;
								activity.runOnUiThread(() -> launchBilling(toLaunch));
								return;
							}
						}

						Log.e(TAG, "Failed to find ProductDetails for targetSku: " + finalTarget);
						activity.runOnUiThread(() -> {
							Toast.makeText(activity, "Google Play: El producto '" + finalTarget + "' aún no está activo en Google Play Console.", Toast.LENGTH_LONG).show();
						});
					}
			);
		}
	}

	private void launchBilling(ProductDetails details) {
		try {
			Log.i(TAG, "Launching Google Play billing flow for: " + details.getProductId() + " (" + details.getTitle() + ")");
			List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
					Collections.singletonList(
							BillingFlowParams.ProductDetailsParams.newBuilder()
									.setProductDetails(details)
									.build()
					);

			BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
					.setProductDetailsParamsList(productDetailsParamsList)
					.build();

			BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);
			Log.i(TAG, "launchBillingFlow result: code=" + billingResult.getResponseCode() + ", msg=" + billingResult.getDebugMessage());
			if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
				activity.runOnUiThread(() -> {
					Toast.makeText(activity, "Google Play Error (" + billingResult.getResponseCode() + "): " + billingResult.getDebugMessage(), Toast.LENGTH_LONG).show();
				});
			}
		} catch (Throwable t) {
			Log.e(TAG, "Exception during launchBilling", t);
		}
	}

	@Override
	public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
		Log.i(TAG, "onPurchasesUpdated: responseCode=" + billingResult.getResponseCode() + ", msg=" + billingResult.getDebugMessage() + ", purchases=" + (purchases != null ? purchases.size() : 0));
		if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
			for (Purchase purchase : purchases) {
				handlePurchase(purchase);
			}
			if (pendingPurchaseCallback != null) {
				pendingPurchaseCallback.call();
				pendingPurchaseCallback = null;
			}
		} else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
			Log.i(TAG, "onPurchasesUpdated: User canceled the purchase.");
		} else {
			Log.w(TAG, "onPurchasesUpdated: Purchase failed or was canceled. Code=" + billingResult.getResponseCode());
		}
	}

	private void handlePurchase(Purchase purchase) {
		Log.i(TAG, "handlePurchase: state=" + purchase.getPurchaseState() + ", orderId=" + purchase.getOrderId() + ", products=" + purchase.getProducts());
		if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
			// Calculate tier from product IDs
			int highestTier = 0;
			for (String prodId : purchase.getProducts()) {
				if (SKU_FULL_UNLOCK.equalsIgnoreCase(prodId) || "02".equalsIgnoreCase(prodId) || "01".equalsIgnoreCase(prodId)) {
					highestTier = Math.max(highestTier, 3);
				}
			}

			if (highestTier > 0) {
				int currentRank = SPDSettings.supporterTier();
				if (highestTier > currentRank) {
					SPDSettings.supporterTier(highestTier);
					Log.i(TAG, "handlePurchase: Supporter tier updated to rank " + highestTier);
				}
				com.shatteredpixel.shatteredpixeldungeon.services.UsernameService.syncSupporterStatusAsync();
			}

			// Acknowledge purchase if not already acknowledged
			if (!purchase.isAcknowledged()) {
				Log.i(TAG, "Acknowledging purchase token...");
				AcknowledgePurchaseParams acknowledgePurchaseParams =
						AcknowledgePurchaseParams.newBuilder()
								.setPurchaseToken(purchase.getPurchaseToken())
								.build();
				billingClient.acknowledgePurchase(acknowledgePurchaseParams, result -> {
					Log.i(TAG, "acknowledgePurchase result: code=" + result.getResponseCode() + ", msg=" + result.getDebugMessage());
				});
			}
		}
	}

	/**
	 * Restores existing purchases and updates local supporter tier accordingly.
	 */
	public void restorePurchases(Callback callback) {
		if (billingClient == null || !isConnected) {
			Log.w(TAG, "restorePurchases: billingClient is not connected.");
			if (callback != null) callback.call();
			return;
		}

		Log.i(TAG, "restorePurchases: Querying active purchases from Google Play...");
		QueryPurchasesParams queryPurchasesParams = QueryPurchasesParams.newBuilder()
				.setProductType(BillingClient.ProductType.INAPP)
				.build();

		billingClient.queryPurchasesAsync(queryPurchasesParams, (billingResult, list) -> {
			Log.i(TAG, "queryPurchasesAsync returned: code=" + billingResult.getResponseCode() + ", count=" + (list != null ? list.size() : 0));
			if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
				int maxTier = 0;
				for (Purchase purchase : list) {
					Log.i(TAG, "Found purchase: state=" + purchase.getPurchaseState() + ", products=" + purchase.getProducts());
					if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
						for (String prodId : purchase.getProducts()) {
							if (SKU_FULL_UNLOCK.equalsIgnoreCase(prodId) || "02".equalsIgnoreCase(prodId) || "01".equalsIgnoreCase(prodId)) {
								maxTier = Math.max(maxTier, 3);
							}
						}
					}
				}
				if (maxTier > 0) {
					SPDSettings.supporterTier(maxTier);
					Log.i(TAG, "restorePurchases: Supporter Tier updated to rank " + maxTier);
					com.shatteredpixel.shatteredpixeldungeon.services.UsernameService.syncSupporterStatusAsync();
				}
			}
			if (callback != null) {
				activity.runOnUiThread(callback::call);
			}
		});
	}

	public int getSupporterTier() {
		return SPDSettings.supporterTier();
	}

	public void destroy() {
		if (billingClient != null) {
			try {
				billingClient.endConnection();
			} catch (Throwable ignored) {}
			billingClient = null;
		}
	}
}
