package com.example.projecto_cm;

import android.content.Context;
import android.database.Cursor;

import androidx.fragment.app.FragmentActivity;

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
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("-------------------------------------------- Recebeu");
        HashMap<String, String> data = receiveData(message.getPayload());

        String type = data.get("TYPE");

        if(type.equals("ADDFRIEND")){
            System.out.println("----------------------------------------------------------------- ADICIONANDO AMIGO");
            String name = data.get("USERNAME");
            String friend = data.get("FRIEND");


            //gravar no firebase numa lista de notificaçoes
            listener.friendNotification(name, friend);

        }

        else if(type.equals("ACCEPTFRIEND")){
            System.out.println("----------------------------------------------------------------- ACEITANDO CONVITE");
            //eliminar notificaçao do firebase
            //adicionar amigo à minha friendlist
            //adicionar-me a mim à friendlist do amigo
        }

        // save this note in the data base
        //My_Database_Helper myDB = new My_Database_Helper((FragmentActivity) context);
        //Cursor cursor = myDB.add_note_from_topic_to_db(aux.get("0"), aux.get("1"));
        //cursor.moveToFirst();
        System.out.println("--------------------------------------------- CALLBACK FEZ TUDO");

        // pass ID title and content of note received
        //listener.getSharedNote(cursor.getString(0), aux.get("0"));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public HashMap<String, String> receiveData(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        HashMap<String, String> aux = (HashMap<String, String>) ois.readObject();

        return aux;
    }
}

