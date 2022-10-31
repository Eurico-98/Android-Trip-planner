package com.example.projecto_cm;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Object> user_id = new MutableLiveData<>();

    /**
     * function to send user ID
     * @param _id
     */
    public void send_user_id(Object _id) { user_id.setValue(_id); }

    /**
     * function to get user id
     * @return
     */
    public LiveData<Object> get_user_id() {return user_id; }
}