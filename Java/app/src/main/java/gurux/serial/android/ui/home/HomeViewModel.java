package gurux.serial.android.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import gurux.serial.GXSerial;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<GXSerial> mSerial = new MutableLiveData<>();

    public LiveData<GXSerial> getSerial() {
        return mSerial;
    }

    public void setSerial(GXSerial net) {
        mSerial.setValue(net);
    }
}