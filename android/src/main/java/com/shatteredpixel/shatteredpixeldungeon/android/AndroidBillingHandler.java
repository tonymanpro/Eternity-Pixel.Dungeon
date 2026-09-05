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

	public static final String SKU_BRONZE   = "supporter_bronze";
	public static final String SKU_SILVER   = "supporter_silver";
	public static final String SKU_GOLD     = "supporter_gold";
	public static final String SKU_PLATINUM = "supporter_platinum";

	private static final List<String> ALL_SKUS = Arrays.asList(
			SKU_BRONZE, SKU_SILVER, SKU_GOLD, SKU_PLATINUM
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
		initialize();
	}

	public static AndroidBillingHandler get() {
		return instance;
	}

	private void initialize() {
		try {
			billingClient = BillingClient.newBuilder(activity)
					.setListener(this)
					.enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
					.build();
			startConnection();
		} catch (Throwable ignored) {
		}
	}

	public void startConnection() {
		if (billingClient == null) return;
		try {
			billingClient.startConnection(this);
		} catch (Throwable ignored) {
		}
	}

	@Override
	public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
		if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
			isConnected = true;
			queryProducts();
			restorePurchases(null);
		} else {
			isConnected = false;
		}
	}

	@Override
	public void onBillingServiceDisconnected() {
		isConnected = false;
	}

	/**
	 * Queries available supporter product details from Google Play.
	 */
	public void queryProducts() {
		if (billingClient == null || !isConnected) return;

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
			if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && result.getProductDetailsList() != null) {
				for (ProductDetails details : result.getProductDetailsList()) {
					productDetailsMap.put(details.getProductId(), details);
				}
			}
		});
	}

	/**
	 * Launches the Google Play in-app purchase flow for the specified supporter tier rank.
	 */
	public void purchaseSupporter(int tierRank, Callback callback) {
		this.pendingPurchaseCallback = callback;

		String targetSku;
		switch (tierRank) {
			case 1: targetSku = SKU_BRONZE; break;
			case 2: targetSku = SKU_SILVER; break;
			case 3: targetSku = SKU_GOLD; break;
			case 4: targetSku = SKU_PLATINUM; break;
			default: targetSku = SKU_BRONZE; break;
		}

		if (billingClient == null || !isConnected) {
			startConnection();
			if (callback != null) callback.call();
			return;
		}

		ProductDetails details = productDetailsMap.get(targetSku);
		if (details != null) {
			launchBilling(details);
		} else {
			// Query again if cache was cold
			List<QueryProductDetailsParams.Product> productList = Collections.singletonList(
					QueryProductDetailsParams.Product.newBuilder()
							.setProductId(targetSku)
							.setProductType(BillingClient.ProductType.INAPP)
							.build()
			);
			billingClient.queryProductDetailsAsync(
					QueryProductDetailsParams.newBuilder().setProductList(productList).build(),
					(billingResult, result) -> {
						List<ProductDetails> list = result != null ? result.getProductDetailsList() : null;
						if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null && !list.isEmpty()) {
							ProductDetails found = list.get(0);
							productDetailsMap.put(found.getProductId(), found);
							activity.runOnUiThread(() -> launchBilling(found));
						} else {
							if (pendingPurchaseCallback != null) pendingPurchaseCallback.call();
						}
					}
			);
		}
	}

	private void launchBilling(ProductDetails details) {
		try {
			List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
					Collections.singletonList(
							BillingFlowParams.ProductDetailsParams.newBuilder()
									.setProductDetails(details)
									.build()
					);

			BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
					.setProductDetailsParamsList(productDetailsParamsList)
					.build();

			billingClient.launchBillingFlow(activity, billingFlowParams);
		} catch (Throwable ignored) {
			if (pendingPurchaseCallback != null) pendingPurchaseCallback.call();
		}
	}

	@Override
	public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
		if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
			for (Purchase purchase : purchases) {
				handlePurchase(purchase);
			}
		}
		if (pendingPurchaseCallback != null) {
			pendingPurchaseCallback.call();
			pendingPurchaseCallback = null;
		}
	}

	private void handlePurchase(Purchase purchase) {
		if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
			// Calculate tier from product IDs
			int highestTier = 0;
			for (String prodId : purchase.getProducts()) {
				if (SKU_PLATINUM.equalsIgnoreCase(prodId)) highestTier = Math.max(highestTier, 4);
				else if (SKU_GOLD.equalsIgnoreCase(prodId)) highestTier = Math.max(highestTier, 3);
				else if (SKU_SILVER.equalsIgnoreCase(prodId)) highestTier = Math.max(highestTier, 2);
				else if (SKU_BRONZE.equalsIgnoreCase(prodId)) highestTier = Math.max(highestTier, 1);
			}

			if (highestTier > 0) {
				int currentRank = SPDSettings.supporterTier();
				if (highestTier > currentRank) {
					SPDSettings.supporterTier(highestTier);
				}
			}

			// Acknowledge purchase if not already acknowledged
			if (!purchase.isAcknowledged()) {
				AcknowledgePurchaseParams acknowledgePurchaseParams =
						AcknowledgePurchaseParams.newBuilder()
								.setPurchaseToken(purchase.getPurchaseToken())
								.build();
				billingClient.acknowledgePurchase(acknowledgePurchaseParams, result -> {});
			}
		}
	}

	/**
	 * Restores existing purchases and updates local supporter tier accordingly.
	 */
	public void restorePurchases(Callback callback) {
		if (billingClient == null || !isConnected) {
			if (callback != null) callback.call();
			return;
		}

		QueryPurchasesParams queryPurchasesParams = QueryPurchasesParams.newBuilder()
				.setProductType(BillingClient.ProductType.INAPP)
				.build();

		billingClient.queryPurchasesAsync(queryPurchasesParams, (billingResult, list) -> {
			if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
				int maxTier = 0;
				for (Purchase purchase : list) {
					if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
						for (String prodId : purchase.getProducts()) {
							if (SKU_PLATINUM.equalsIgnoreCase(prodId)) maxTier = Math.max(maxTier, 4);
							else if (SKU_GOLD.equalsIgnoreCase(prodId)) maxTier = Math.max(maxTier, 3);
							else if (SKU_SILVER.equalsIgnoreCase(prodId)) maxTier = Math.max(maxTier, 2);
							else if (SKU_BRONZE.equalsIgnoreCase(prodId)) maxTier = Math.max(maxTier, 1);
						}
					}
				}
				if (maxTier > 0) {
					SPDSettings.supporterTier(maxTier);
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
