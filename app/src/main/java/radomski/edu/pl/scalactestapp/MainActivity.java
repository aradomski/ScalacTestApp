package radomski.edu.pl.scalactestapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import radomski.edu.pl.scalactestapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int FENCE_REQUEST = 125;
    private static final int WALLET_PLACE_PICKER_REQUEST = 123;
    private static final int CAR_PLACE_PICKER_REQUEST = 124;
    public static final String FENCE_RECEIVER_ACTION = "fenceReciever";
    public static final String KEY_CAR = "key_car";
    public static final String KEY_WALLET = "key_wallet";
    private GoogleApiClient googleApiClient;
    private ActivityMainBinding viewBinding;
    private MainActivityModel model;
    private PendingIntent pendingIntent;
    private FenceReceiver fenceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new MainActivityModel();
        viewBinding.setModel(model);
        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Awareness.API)
                .enableAutoManage(this, this)
                .build();


        viewBinding.walletLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPickerWithPermissionCheck(WALLET_PLACE_PICKER_REQUEST);
            }
        });
        viewBinding.carLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPickerWithPermissionCheck(CAR_PLACE_PICKER_REQUEST);
            }
        });
        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        fenceReceiver = new FenceReceiver();
        registerReceiver(fenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
    }

    private void startPickerWithPermissionCheck(int requestCode) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    requestCode);
            return;
        }
        startPicker(requestCode);
    }

    @RequiresPermission(anyOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void startPicker(int requestCode) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(MainActivity.this), requestCode);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            handleError("Error connecting to google apis");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        handleError("Error connecting to google apis");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAR_PLACE_PICKER_REQUEST:
            case WALLET_PLACE_PICKER_REQUEST:
                handlePermissionResponse(requestCode, permissions, grantResults);
                break;
            case FENCE_REQUEST:
                reqisterCarFence(model.carPlace.getLatLng());
        }
    }

    private void handlePermissionResponse(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            boolean anyPermissionGiven = false;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    anyPermissionGiven = true;
                }
            }
            if (anyPermissionGiven) {
                startPicker(requestCode);
            } else {
                handleError("Need permission to do my job, bye!");
            }
        } else {
            handleError("Need permission to do my job, bye!");
        }
    }

    private void handleError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ")));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAR_PLACE_PICKER_REQUEST:
                    model.setCarPlace(PlacePicker.getPlace(this, data));
                    LatLng latLng = model.getCarPlace().getLatLng();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FENCE_REQUEST);
                        return;
                    }
                    PreferenceHelper.saveLocation(MainActivity.this, KEY_CAR, latLng);
                    reqisterCarFence(latLng);
                    break;
                case WALLET_PLACE_PICKER_REQUEST:
                    Place place = PlacePicker.getPlace(this, data);
                    PreferenceHelper.saveLocation(MainActivity.this, KEY_WALLET, place.getLatLng());
                    model.setWalletPlace(place);
                    break;
            }

        }
    }

    private void reqisterCarFence(LatLng latLng) {
        AwarenessFence latLngFence =
                AwarenessFence.or(LocationFence.entering(latLng.latitude, latLng.longitude, 10),
                        LocationFence.in(latLng.latitude, latLng.longitude, 10, 10L));

        registerFence(FENCE_RECEIVER_ACTION, latLngFence);
    }

    protected void registerFence(final String fenceKey, final AwarenessFence fence) {
        Awareness.FenceApi.updateFences(googleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(fenceKey, fence, pendingIntent)
                        .build())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully registered.");
                        } else {
                            Log.e(TAG, "Fence could not be registered: " + status);
                        }
                    }
                });
    }
}
