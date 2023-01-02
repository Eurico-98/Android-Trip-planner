package com.example.projecto_cm;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Shared_View_Model extends ViewModel {

    private final MutableLiveData<Object> data = new MutableLiveData<>();

    /**
     * function to send username
     * @param username_
     */
    public void send_data(Object data_) { data.setValue(data_); }

    /**
     * function to get username
     * @return username
     */
    public LiveData<Object> get_data() { return data; }
}