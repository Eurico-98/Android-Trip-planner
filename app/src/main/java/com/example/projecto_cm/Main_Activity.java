package com.example.projecto_cm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.pm.ActivityInfo;
import androidx.lifecycle.ViewModelProvider;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.example.projecto_cm.Fragments.Frag_Login;
import com.example.projecto_cm.Interfaces.Interface_Frag_Change_Listener;
import com.example.projecto_cm.Interfaces.Interface_MQTT_Notifications;

import java.util.Objects;

public class Main_Activity extends AppCompatActivity implements Interface_Frag_Change_Listener, Interface_MQTT_Notifications {

    private NotificationCompat.Builder builder;
    private Shared_View_Model model;

    /**
     * on create of login_register activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);

        // Lock the orientation to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        model = new ViewModelProvider(Main_Activity.this).get(Shared_View_Model.class);
        model.sendActivityInstance(this);

        //requestNotificationPolicyAccess();
        builder = new NotificationCompat.Builder(Main_Activity.this, "ID1");

        // if build version is oreo or latter

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ID1", "ID1", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager noti_manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            noti_manager.createNotificationChannel(channel);
            builder.setChannelId("ID1");
        }


        // load login fragment
        Frag_Login frag_login = new Frag_Login();
        replaceFragment(frag_login, "no");
    }

    /**
     * to navigate through fragments
     * @param fragment
     */
    public void replaceFragment(Fragment fragment, String keep_frag_in_stack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.activity_login_register_container, fragment);
        if(Objects.equals(keep_frag_in_stack, "yes")){
            fragmentTransaction.addToBackStack(fragment.toString());
        }
        fragmentTransaction.commit();
    }

    public void friendNotification(String requestUsername){
        System.out.println("--------------------------------------- Chegou Aqui: " + requestUsername);
        builder.setContentTitle("New Friend Request");
        builder.setContentText(requestUsername + " wants to be your friend");
        builder.setSmallIcon(R.drawable.ic_friend_request);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        //builder.setAutoCancel(true);


        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(123, builder.build());


        System.out.println("OLa");
    }

    private void requestNotificationPolicyAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
                Intent intent = new Intent(
                        android.provider.Settings
                                .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            }
        }
    }

}