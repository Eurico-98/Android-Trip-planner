package com.example.projecto_cm;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.UUID;

public class MQTT_Helper {

    public MqttAsyncClient myClient;
    final String server = "tcp://broker.mqttdashboard.com";
    final String TAG = "TAG";
    final String mainTopic = "/cm2023/";

    public MQTT_Helper(Context context, Main_Activity listener) throws MqttException {
        System.out.println("1");
        myClient = new MqttAsyncClient(server, UUID.randomUUID().toString(), new MemoryPersistence());
        System.out.println("2");
        MQTT_Callback callback = new MQTT_Callback(context, listener);
        System.out.println("3");
        myClient.setCallback(callback);
        System.out.println("4");
        IMqttToken token = myClient.connect();
        System.out.println("5");
        token.waitForCompletion();
        System.out.println("MQTT CONNECT FUNCIONOU - 6");
    }

    public void stop() throws MqttException { myClient.disconnect(); }

    /**
     * send friend request to topic
     * @param myUsername
     * @param friendUsername
     * @throws MqttException
     * @throws IOException
     */
    public void addFriendRequest(String myUsername, String friendUsername) throws MqttException, IOException {

        String topic = mainTopic + friendUsername;

        HashMap<String, String> aux = new HashMap<>();

        aux.put("TYPE", "ADDFRIEND");
        aux.put("USERNAME", myUsername);
        aux.put("FRIEND", friendUsername);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(aux);
        byte[] data = baos.toByteArray();

        myClient.publish(topic, data, 2, false);
    }


    /*public void acceptFriendRequest(String myUsername, String friendUsername) throws IOException, MqttException {

        String topic = mainTopic + friendUsername;

        HashMap<String, String> aux = new HashMap<>();

        aux.put("TYPE", "ACCEPTFRIEND");
        aux.put("USERNAME", myUsername);

        byte[] data = sendData(aux);

        myClient.publish(topic, data, 2, false);
    }


    public byte[] sendData(HashMap<String, String> data) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(aux);
        byte[] data = baos.toByteArray();

        myClient.publish(topic, data, 2, false);
    }*/


    /**
     * to subscribe to a topic
     * @param topic
     * @throws MqttException
     */
    public void subscribeToTopic(String topic) throws MqttException {
        
        myClient.subscribe(mainTopic + topic, 2, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.w(TAG, "Subscribed!");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.w(TAG, "Subscribe fail!");
            }
        });
    }

    /**
     * to unsubscribe
     * @param topic
     * @throws MqttException
     */
    public void unSubscribeToTopic(String topic) throws MqttException { myClient.unsubscribe(topic); }
}

