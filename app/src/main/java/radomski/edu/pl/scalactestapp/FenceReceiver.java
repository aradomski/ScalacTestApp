package radomski.edu.pl.scalactestapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.maps.model.LatLng;

public class FenceReceiver extends BroadcastReceiver {
    private static final String TAG = FenceReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        FenceState fenceState = FenceState.extract(intent);
        Log.i(TAG, "FENCE RECIEVER");
        if (TextUtils.equals(fenceState.getFenceKey(), MainActivity.FENCE_RECEIVER_ACTION)) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:

                    LatLng carLatLng = PreferenceHelper.loadLocation(context, MainActivity.KEY_CAR);
                    LatLng walletLatLng = PreferenceHelper.loadLocation(context, MainActivity.KEY_WALLET);
                    Location carLocation = new Location("as");
                    carLocation.setLatitude(carLatLng.latitude);
                    carLocation.setLongitude(carLatLng.longitude);

                    Location walletLocation = new Location("as");
                    walletLocation.setLongitude(walletLatLng.longitude);
                    walletLocation.setLatitude(walletLatLng.latitude);


                    if (carLocation.distanceTo(walletLocation) > 10) {
                        Toast.makeText(context, "You forgot your wallet!", Toast.LENGTH_SHORT).show();
                        MediaPlayer mPlayer = MediaPlayer.create(context, R.raw.rick);
                        mPlayer.start();

                    }
                    Log.i(TAG, "inside fence");
                    break;
                case FenceState.FALSE:
                    Log.i(TAG, "outside fence");
                    break;
                case FenceState.UNKNOWN:
                    Log.i(TAG, "dunno");
                    break;
            }
        }
    }
}
