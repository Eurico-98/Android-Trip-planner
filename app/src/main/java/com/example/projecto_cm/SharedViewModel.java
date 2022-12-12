package com.example.projecto_cm;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Object> username = new MutableLiveData<>();

    /**
     * function to send username
     * @param username_
     */
    public void send_username(Object username_) { username.setValue(username_); }

    /**
     * function to get username
     * @return username
     */
    public LiveData<Object> get_username() { return username; }
}