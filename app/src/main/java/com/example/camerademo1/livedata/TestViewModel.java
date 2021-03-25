package com.example.camerademo1.livedata;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class TestViewModel extends ViewModel {

    private final String mKey;

    private MutableLiveData<String> mNameEvent = new MutableLiveData<>();

    public TestViewModel(String key) {
        mKey = key;
    }

    public MutableLiveData<String> getNameEvent() {
        return mNameEvent;
    }

    public void setNameEvent(MutableLiveData<String> nameEvent) {
        mNameEvent = nameEvent;
    }


    public static class Factory implements ViewModelProvider.Factory{
        private String mKey;

        public Factory(String key) {
            mKey = key;
        }
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new TestViewModel(mKey);
        }
    }

    public String getKey() {
        return mKey;
    }

}
