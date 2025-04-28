package gurux.serial.android.ui.media;

import androidx.lifecycle.ViewModel;

import gurux.common.IGXMedia;

public class MediaViewModel extends ViewModel {

    private IGXMedia mMedia;

    public IGXMedia getMedia() {
        return mMedia;
    }


    public void setMedia(IGXMedia device) {
        mMedia = device;
    }
}