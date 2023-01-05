package com.example.projecto_cm.Interfaces;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

public interface Interface_Edit_Profile {
    void showResultMessage(String result);
    void changePassResult(String result);
    void deleteAccountResult(String result);
    void searchResult (String username) throws MqttException, IOException;
}
