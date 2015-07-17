package com.mynameistodd.autovolume;

import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.analytics.ecommerce.Product;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements
        EditCreateAlarm.EditCreateAlarmCallbacks,
        AlarmListFragment.AlarmListCallbacks {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    ServiceConnection mServiceConn;
    IInAppBillingService mService;
    Bundle querySkus;
    PendingIntent pendingBuyIntent;
    //String price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        analytics = GoogleAnalytics.getInstance(this);

        tracker = analytics.newTracker(R.xml.global_tracker);
        tracker.enableAdvertisingIdCollection(true);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new AlarmListFragment())
                    .commit();
        }

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name,
                                           IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
                new InAppBillingPurchasedItemsTask().execute();
            }
        };
        bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND").setPackage("com.android.vending"),
                mServiceConn, Context.BIND_AUTO_CREATE);

        ArrayList<String> skuList = new ArrayList<>();
        skuList.add("donate.99");

        querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (pendingBuyIntent != null) {
            menu.findItem(R.id.action_donate).setEnabled(true);
        } else {
            menu.findItem(R.id.action_donate).setTitle("Donated - Thanks!");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        else if (id == R.id.action_add) {
            EditCreateAlarm editCreateAlarm = new EditCreateAlarm();
            Bundle args = new Bundle();
            args.putInt("ID", 0);
            editCreateAlarm.setArguments(args);

            getFragmentManager().beginTransaction()
                    .replace(R.id.container, editCreateAlarm, "editCreate")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.action_donate) {
            if (pendingBuyIntent != null) {
                try {
                    startIntentSenderForResult(pendingBuyIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String orderId = jo.getString("orderId");
                    String productId = jo.getString("productId");
                    String token = jo.getString("purchaseToken");

                    Toast.makeText(this, "You are awesome, thanks!", Toast.LENGTH_LONG).show();

                    tracker.send(new HitBuilders.TransactionBuilder()
                                    .setTransactionId(orderId)
                                    .setAffiliation("In-app Store")
                                    .setRevenue(Double.valueOf("0.99"))
                                    .setTax(Double.valueOf("0.0"))
                                    .setShipping(Double.valueOf("0.0"))
                                    .setCurrencyCode("USD")
                                    .build()
                    );
                    tracker.send(new HitBuilders.ItemBuilder()
                                    .setTransactionId(orderId)
                                    .addProduct(new Product().setName("Donate .99 cents").setId(productId).setCategory("Donations").setPrice(Double.valueOf("0.99")).setQuantity(1))
                                    .setCurrencyCode("USD")
                                    .build()
                    );

                    new InAppBillingConsumeTask().execute(token);
                } catch (JSONException e) {
                    Toast.makeText(this, "Something went terribly wrong!.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else if (resultCode == RESULT_CANCELED) {
                new InAppBillingPurchasedItemsTask().execute();
            }
        }
    }

    @Override
    public void onAlarmDismiss() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void onAlarmSelected(int id) {
        EditCreateAlarm editCreateAlarm = new EditCreateAlarm();
        Bundle args = new Bundle();
        args.putInt("ID", id);
        editCreateAlarm.setArguments(args);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, editCreateAlarm, "editCreate")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    private class InAppBillingPurchasedItemsTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                int response = ownedItems.getInt("RESPONSE_CODE");
                if (response == 0) {
                    ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    if (ownedSkus.contains("donate.99")) { //TODO: fix this in the future when adding more items to purchase

                        ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                        for (String purchaseData : purchaseDataList) {
                            JSONObject jo = new JSONObject(purchaseData);
                            String token = jo.getString("purchaseToken");

                            new InAppBillingConsumeTask().execute(token);
                        }
                        return true;
                    }
                }
            } catch (RemoteException | JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean ownsItem) {
            super.onPostExecute(ownsItem);

            if (!ownsItem) {
                new InAppBillingBuyIntentTask().execute(querySkus);
            }
        }
    }

    private class InAppBillingBuyIntentTask extends AsyncTask<Bundle, Integer, PendingIntent> {
        @Override
        protected PendingIntent doInBackground(Bundle... params) {
            Bundle querySkus = params[0];
            PendingIntent pendingIntent = null;
            try {
                Bundle skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
                int response = skuDetails.getInt("RESPONSE_CODE");
                if (response == 0) {
                    ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                    for (String thisResponse : responseList) {
                        JSONObject object = new JSONObject(thisResponse);
                        String sku = object.getString("productId");

                        if (sku.equals("donate.99")) {
                            //price = object.getString("price");
                            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), sku, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
                            pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                        }
                    }
                }
            } catch (RemoteException | JSONException e) {
                e.printStackTrace();
            }
            return pendingIntent;
        }

        @Override
        protected void onPostExecute(PendingIntent pendingIntent) {
            super.onPostExecute(pendingIntent);
            pendingBuyIntent = pendingIntent;
            invalidateOptionsMenu();
        }
    }

    private class InAppBillingConsumeTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String token = params[0];
            try {
                int response = mService.consumePurchase(3, getPackageName(), token);
                if (response == 0) {
                    return true;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean consumeSuccess) {
            super.onPostExecute(consumeSuccess);
            if (consumeSuccess) {
                new InAppBillingPurchasedItemsTask().execute();
            }
        }
    }
}
