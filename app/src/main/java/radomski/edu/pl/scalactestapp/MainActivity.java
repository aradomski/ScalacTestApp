package radomski.edu.pl.scalactestapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import radomski.edu.pl.scalactestapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    @IntDef({WALLET_PLACE_PICKER_REQUEST, CAR_PLACE_PICKER_REQUEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestCodes {
    }

    private static final int WALLET_PLACE_PICKER_REQUEST = 123;
    private static final int CAR_PLACE_PICKER_REQUEST = 124;
    private GoogleApiClient mGoogleApiClient;
    private ActivityMainBinding viewBinding;
    private MainActivityModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new MainActivityModel();
        viewBinding.setModel(model);
        mGoogleApiClient = new GoogleApiClient
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
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(@RequestCodes int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAR_PLACE_PICKER_REQUEST:
            case WALLET_PLACE_PICKER_REQUEST:
                handlePermissionResponse(requestCode, permissions, grantResults);
                break;
        }
    }

    private void handlePermissionResponse(@RequestCodes int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                handleNoPermissionGiven();
            }
        } else {
            handleNoPermissionGiven();
        }
    }

    private void handleNoPermissionGiven() {
        Toast.makeText(this, "Need permission to do my job, bye!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ")));
    }

    protected void onActivityResult(@RequestCodes int requestCode, int resultCode, Intent data) {
        Place place;
        String toastMsg;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAR_PLACE_PICKER_REQUEST:
                    place = PlacePicker.getPlace(this, data);
                    model.setCarPlace(place);
                    toastMsg = String.format("Place: %s", place.getName());
                    Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                    break;
                case WALLET_PLACE_PICKER_REQUEST:
                    place = PlacePicker.getPlace(this, data);
                    model.setWalletPlace(place);
                    toastMsg = String.format("Place: %s", place.getName());
                    Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                    break;
            }

        }
    }

}
