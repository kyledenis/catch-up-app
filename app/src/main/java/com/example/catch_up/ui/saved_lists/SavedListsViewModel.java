package com.example.catch_up.ui.saved_lists;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SavedListsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SavedListsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the Saved Lists Page");
    }

    public LiveData<String> getText() {
        return mText;
    }
}