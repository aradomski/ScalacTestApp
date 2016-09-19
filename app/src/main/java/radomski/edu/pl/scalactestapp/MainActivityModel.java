package radomski.edu.pl.scalactestapp;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.google.android.gms.location.places.Place;

public class MainActivityModel extends BaseObservable {

    Place carPlace;
    Place walletPlace;

    @Bindable
    public Place getCarPlace() {
        return carPlace;
    }

    public void setCarPlace(Place carPlace) {
        this.carPlace = carPlace;
        notifyPropertyChanged(BR.carPlace);
    }

    @Bindable
    public Place getWalletPlace() {
        return walletPlace;
    }

    public void setWalletPlace(Place walletPlace) {
        this.walletPlace = walletPlace;
        notifyPropertyChanged(BR.walletPlace);
    }
}
