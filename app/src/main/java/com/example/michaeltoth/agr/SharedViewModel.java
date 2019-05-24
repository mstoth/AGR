package com.example.michaeltoth.agr;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> oldName = new MutableLiveData<String>();
    private final MutableLiveData<String> newName = new MutableLiveData<String>();

    public void setOldName(String name) {
        oldName.postValue(name);
    }

    public LiveData<String> getOldName() {
        return oldName;
    }
    public void setNewName(String name) {
        newName.postValue(name);
    }

    public LiveData<String> getNewName() {
        return newName;
    }
}
