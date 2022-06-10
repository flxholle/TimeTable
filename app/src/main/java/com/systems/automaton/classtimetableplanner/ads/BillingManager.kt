package com.systems.automaton.classtimetableplanner.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.android.billingclient.api.*
import com.systems.automaton.classtimetableplanner.BuildConfig
import com.systems.automaton.classtimetableplanner.R

private const val TAG: String = "BillingManager"

class BillingManager {

    private lateinit var billingClient: BillingClient
    private var productDetails: ProductDetails? = null
    var isInitialized: Boolean = false; private set

    fun initialize(context: Context) {
        if (isInitialized) {
            throw IllegalStateException("Can't initialize an already initialized singleton.")
        }

        billingClient = BillingClient.newBuilder(context)
            .setListener { _, _ ->
                checkPurchases(context)
            }
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
                    && productDetails == null) {

                    val productList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(context.getString(R.string.ad_remove_product_name))
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build())
                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                        .build()
                    billingClient.queryProductDetailsAsync(params) { queryProductBillingResult, productDetailsList ->
                        if (queryProductBillingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            productDetails = productDetailsList.firstOrNull()
                        }
                    }

                    checkPurchases(context)
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private fun checkPurchases(context: Context) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { billingResult, purchaseList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val purchaseItem = purchaseList.firstOrNull { purchase ->
                    purchase.products.contains(context.getString(R.string.ad_remove_product_name))
                }
                val unAckPurchasedItem = purchaseList.firstOrNull { purchase ->
                    !purchase.isAcknowledged
                            && purchase.products.contains(context.getString(R.string.ad_remove_product_name))
                }
                if (unAckPurchasedItem != null) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(unAckPurchasedItem.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(ackParams) { }

                    context.getActivity()?.let {
                        it.runOnUiThread {
                            AdManager.instance.disableAds()
                            it.recreate()
                            Toast.makeText(context, context.getString(R.string.thank_you_purchase), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                if (purchaseItem != null) {
                    AdManager.instance.disableAds()

                    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
                    val editor = sharedPrefs.edit()
                    editor.putBoolean(context.getString(R.string.sp_remove_ads), true)
                    editor.apply()

                    // TESTING: consume the purchase if we refunded.
                    //testingConsumePurchase(purchaseItem)
                } else {
                    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
                    val editor = sharedPrefs.edit()
                    editor.putBoolean(context.getString(R.string.sp_remove_ads), false)
                    editor.apply()
                }
            } else {
                Log.d(TAG, "checkPurchases has billingResult of ${billingResult.responseCode} -- ${billingResult.debugMessage}")
            }
        }
    }

    fun buy(activity: Activity) {
        (activity as Context).getActivity()

        val context = activity as Context
        context.getActivity()?.let {
            it.runOnUiThread {
                AdManager.instance.disableAds()
                it.recreate()
                Toast.makeText(context, context.getString(R.string.thank_you_purchase), Toast.LENGTH_SHORT).show()

                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
                val editor = sharedPrefs.edit()
                editor.putBoolean(context.getString(R.string.sp_remove_ads), true)
                editor.apply()
            }
        }

        return

        productDetails?.let {
            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(it)
                .build()
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()
            billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    fun testingConsumePurchase(purchaseItem: Purchase?) {

        if (!BuildConfig.DEBUG) {
            return
        }

        purchaseItem?.let {
            val params = ConsumeParams.newBuilder()
                .setPurchaseToken(it.purchaseToken)
                .build()
            billingClient.consumeAsync(params) { billingResult, purchaseToken ->
                Log.d(TAG, "consumedPurchase -- $billingResult $purchaseToken")
            }
        }
    }

    companion object {
        @JvmField
        val instance = BillingManager()
    }
}