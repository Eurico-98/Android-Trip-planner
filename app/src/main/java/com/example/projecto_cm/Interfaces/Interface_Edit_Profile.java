package com.example.projecto_cm.Interfaces;

import com.google.android.gms.tasks.Task;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.ArrayList;

public interface Interface_Edit_Profile {
    void showResultMessage(String result);
    void changePassResult(String result);
    void deleteAccountResult(String result);
    void searchResult (String username) throws MqttException, IOException;
    void getMyFriends(ArrayList<String> resultList);
}
