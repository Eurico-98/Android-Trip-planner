package com.example.projecto_cm;

import android.content.Context;

import com.example.projecto_cm.Interfaces.Interface_MQTT_Notifications;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class MQTT_Callback implements MqttCallback{

    Context context;
    Interface_MQTT_Notifications listener;

    public MQTT_Callback(Context context, Main_Activity listener){
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void connectionLost(Throwable cause) {}

    /**
     * when a message is received execute this
     * @param topic
     * @param message
     * @throws Exception
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        System.out.println("-------------------------------------------- detetou mensagem");

        ByteArrayInputStream bais = new ByteArrayInputStream(message.getPayload());
        ObjectInputStream ois = new ObjectInputStream(bais);
        HashMap<String, String> data = (HashMap<String, String>) ois.readObject();
        String type = data.get("TYPE");

        System.out.println("-------------------------------------------- Recebeu");

        // send notification to destination user
        if(type.equals("ADDFRIEND")){

            System.out.println("----------------------------------------------------------------- ADICIONANDO AMIGO");
            String name = data.get("USERNAME");
            String friend = data.get("FRIEND");
            listener.friendNotification(name, friend);
        }

        else if(type.equals("ACCEPTFRIEND")){
            System.out.println("----------------------------------------------------------------- ACEITANDO CONVITE");
            //eliminar notificaçao do firebase
            //adicionar amigo à minha friendlist
            //adicionar-me a mim à friendlist do amigo
        }
    }

    /**
     * notify sender if message was received by the receiver
     * @param token
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

        // Check if the delivery was successful
        if (token.isComplete()) {
            // Delivery was successful
            System.out.println("Message was successfully delivered");
        } else {
            // Delivery failed
            System.out.println("Message delivery failed");
        }
    }
}

