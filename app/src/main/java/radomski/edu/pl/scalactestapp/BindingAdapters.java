package radomski.edu.pl.scalactestapp;

import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;

public class BindingAdapters {
    @BindingAdapter("place")
    public static void setPlace(TextView view, Place place) {
        if (place != null) {
            view.setVisibility(View.VISIBLE);
            view.setText(place.getName() + " " + place.getLatLng());
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("placeVisibility")
    public static void placeVisibility(View view, Place place) {
        if (place != null) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }


}
