package com.mynameistodd.autovolume;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements
        EditCreateAlarm.EditCreateAlarmCallbacks,
        AlarmListFragment.AlarmListCallbacks,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener,
        LocationClient.OnRemoveGeofencesResultListener {

    ServiceConnection mServiceConn;
    IInAppBillingService mService;
    Bundle querySkus;
    PendingIntent pendingBuyIntent;
    //String price;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static final long SECONDS_PER_HOUR = 60;
    private static final long MILLISECONDS_PER_SECOND = 1000;
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_TIME =
            GEOFENCE_EXPIRATION_IN_HOURS *
                    SECONDS_PER_HOUR *
                    MILLISECONDS_PER_SECOND;

    private double mLatitude1 = 42.295875;
    private double mLongitude1 = -83.784239;
    private float mRadius1 = 2;
    private double mLatitude2 = 42.280585;
    private double mLongitude2 = -83.749550;
    private float mRadius2 = 2;
    private SimpleGeofence mUIGeofence1;
    private SimpleGeofence mUIGeofence2;
    List<Geofence> mGeofenceList;
    private SimpleGeofenceStore mGeofenceStorage;

    // Holds the location client
    private LocationClient mLocationClient;
    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent mGeofenceRequestIntent;


    // Defines the allowable request types.
    public enum REQUEST_TYPE { ADD, REMOVE_INTENT, REMOVE_LIST }
    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    // Store the list of geofence Ids to remove
    List<String> mGeofencesToRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

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
        bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"),
                mServiceConn, Context.BIND_AUTO_CREATE);

        ArrayList<String> skuList = new ArrayList<String>();
        skuList.add("donate.99");

        querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

        // Instantiate a new geofence storage area
        mGeofenceStorage = new SimpleGeofenceStore(this);

        // Instantiate the current List of geofences
        mGeofenceList = new ArrayList<Geofence>();

        mInProgress = false;

        createGeofences();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addGeofences();
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
                    startIntentSenderForResult(pendingBuyIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 1001:

                int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
                String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

                switch (resultCode) {
                    case RESULT_OK:
                        try {
                            JSONObject jo = new JSONObject(purchaseData);
                            String orderId = jo.getString("orderId");
                            String productId = jo.getString("productId");
                            String token = jo.getString("purchaseToken");

                            Toast.makeText(this, "You are awesome, thanks!", Toast.LENGTH_LONG).show();

                            EasyTracker tracker = EasyTracker.getInstance(this);
                            tracker.send(MapBuilder.createTransaction(orderId, "In-app Store", Double.valueOf("0.99"), Double.valueOf("0.0"), Double.valueOf("0.0"), "USD").build());
                            tracker.send(MapBuilder.createItem(orderId, "Donate .99 cents", productId, "Donations", Double.valueOf("0.99"), Long.valueOf("1"), "USD").build());

                            new InAppBillingConsumeTask().execute(token);
                        } catch (JSONException e) {
                            Toast.makeText(this, "Something went terribly wrong!.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        break;
                    case RESULT_CANCELED:
                        new InAppBillingPurchasedItemsTask().execute();
                        break;
                }
                break;
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:

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
        EasyTracker.getInstance(this).activityStop(this);
        removeGeofences(getTransitionPendingIntent());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
        mGeofenceStorage.clearGeofence(mUIGeofence1.getId());
        mGeofenceStorage.clearGeofence(mUIGeofence2.getId());
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
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (JSONException e) {
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
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (JSONException e) {
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

    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Geofence Detection", "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Get the error code
            //int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getFragmentManager(), "Geofence Detection");
            }
        }
        return false;
    }

    /**
     * Get the geofence parameters for each geofence from the UI
     * and add them to a List.
     */
    public void createGeofences() {
        /*
         * Create an internal object to store the data. Set its
         * ID to "1". This is a "flattened" object that contains
         * a set of strings
         */
        mUIGeofence1 = new SimpleGeofence(
                "1",
                Double.valueOf(mLatitude1),
                Double.valueOf(mLongitude1),
                Float.valueOf(mRadius1),
                GEOFENCE_EXPIRATION_TIME,
                // This geofence records only entry transitions
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
        // Store this flat version
        mGeofenceStorage.setGeofence("1", mUIGeofence1);

        // Create another internal object. Set its ID to "2"
        mUIGeofence2 = new SimpleGeofence(
                "2",
                Double.valueOf(mLatitude2),
                Double.valueOf(mLongitude2),
                Float.valueOf(mRadius2),
                GEOFENCE_EXPIRATION_TIME,
                // This geofence records both entry and exit transitions
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
        // Store this flat version
        mGeofenceStorage.setGeofence("2", mUIGeofence2);

        mGeofenceList.add(mUIGeofence1.toGeofence());
        mGeofenceList.add(mUIGeofence2.toGeofence());
    }

    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
    private PendingIntent getTransitionPendingIntent() {
        // Create an explicit Intent
        Intent intent = new Intent(this,
                ReceiveTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
        mGeofenceRequestIntent = PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofenceRequestIntent;
    }

    /**
     * Start a request for geofence monitoring by calling
     * LocationClient.connect().
     */
    public void addGeofences() {
        // Start a request to add geofences
        mRequestType = REQUEST_TYPE.ADD;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the proper request
         * can be restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        mLocationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }

    /**
     * Start a request to remove geofences by calling
     * LocationClient.connect()
     */
    public void removeGeofences(PendingIntent requestIntent) {
        // Record the type of removal request
        mRequestType = REQUEST_TYPE.REMOVE_INTENT;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the request can be
         * restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        // Store the PendingIntent
        mGeofenceRequestIntent = requestIntent;
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        mLocationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }

    /**
     * Start a request to remove monitoring by
     * calling LocationClient.connect()
     *
     */
    public void removeGeofences(List<String> geofenceIds) {
        // If Google Play services is unavailable, exit
        // Record the type of removal request
        mRequestType = REQUEST_TYPE.REMOVE_LIST;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the request can be
         * restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        // Store the list of geofences to remove
        mGeofencesToRemove = geofenceIds;
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        mLocationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        switch (mRequestType) {
            case ADD:
                // Get the PendingIntent for the request
                mGeofenceRequestIntent = getTransitionPendingIntent();
                // Send a request to add the current geofences
                mLocationClient.addGeofences(mGeofenceList, mGeofenceRequestIntent, this);
                break;
            case REMOVE_INTENT :
                mLocationClient.removeGeofences(mGeofenceRequestIntent, this);
                break;
            case REMOVE_LIST :
                mLocationClient.removeGeofences(mGeofencesToRemove, this);
                break;
        }
    }

    @Override
    public void onDisconnected() {
        // Turn off the request flag
        mInProgress = false;
        // Destroy the current location client
        mLocationClient = null;
    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] strings) {
        // If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            /*
             * Handle successful addition of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        } else {
            // If adding the geofences failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
        }
        // Turn off the in progress flag and disconnect the client
        mInProgress = false;
        mLocationClient.disconnect();
    }

    /**
     * When the request to remove geofences by PendingIntent returns,
     * handle the result.
     *
     *@param statusCode the code returned by Location Services
     *@param requestIntent The Intent used to request the removal.
     */
    @Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode,
                                                       PendingIntent requestIntent) {
        // If removing the geofences was successful
        if (statusCode == LocationStatusCodes.SUCCESS) {
            /*
             * Handle successful removal of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        } else {
            // If adding the geocodes failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
        }
        /*
         * Disconnect the location client regardless of the
         * request status, and indicate that a request is no
         * longer in progress
         */
        mInProgress = false;
        mLocationClient.disconnect();
    }

    /**
     * When the request to remove geofences by IDs returns, handle the
     * result.
     *
     * @param statusCode The code returned by Location Services
     * @param geofenceRequestIds The IDs removed
     */
    @Override
    public void onRemoveGeofencesByRequestIdsResult(
            int statusCode, String[] geofenceRequestIds) {
        // If removing the geocodes was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            /*
             * Handle successful removal of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        } else {
            // If removing the geofences failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
        }
        // Indicate that a request is no longer in progress
        mInProgress = false;
        // Disconnect the location client
        mLocationClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Turn off the request flag
        mInProgress = false;
        /*
         * If the error has a resolution, start a Google Play services
         * activity to resolve it.
         */
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
            // If no resolution is available, display an error dialog
        } else {
            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(
                        getFragmentManager(),
                        "Geofence Detection");
            }
        }
    }
}
