package com.example.projecto_cm;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Shared_View_Model extends ViewModel {

    private final MutableLiveData<Object> data = new MutableLiveData<>();
    private final MutableLiveData<Main_Activity> activity = new MutableLiveData<>();

    /**
     * function to send username
     * @param username_
     */
    public void sendData(Object data_) { data.setValue(data_); }

    public void sendActivityInstance(Main_Activity activity_) { activity.setValue(activity_); }

    /**
     * function to get username
     * @return username
     */
    public LiveData<Object> getData() { return data; }

    public LiveData<Main_Activity> getActivityInstance() { return activity; }
}