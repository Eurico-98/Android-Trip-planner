package com.example.projecto_cm;

import static android.app.PendingIntent.FLAG_MUTABLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import androidx.lifecycle.ViewModelProvider;

import android.app.NotificationChannel;
import android.app.NotificationManager;
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
    DialogReceiver receiver;

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

        receiver = new DialogReceiver();
        IntentFilter filter = new IntentFilter("com.example.projecto_cm.DIALOG_INTENT");
        registerReceiver(receiver, filter);

        model = new ViewModelProvider(Main_Activity.this).get(Shared_View_Model.class);
        model.sendActivityInstance(this);

        builder = new NotificationCompat.Builder(this, "Friend request notification");

        // if build version is oreo or latter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Friend request notification", "Friend request notification channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = this.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
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

    /**
     * to show notification of friend request in android
     * @param requestUsername
     * @param requestedUsername
     */
    @SuppressLint("LaunchActivityFromNotification")
    public void friendNotification(String requestUsername, String requestedUsername){

        System.out.println("--------------------------------------- Chegou Aqui: " + requestUsername);

        Intent intent = new Intent("com.example.projecto_cm.DIALOG_INTENT");
        intent.putExtra("request_username", requestUsername);
        intent.putExtra("my_username", requestedUsername);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, FLAG_MUTABLE);

        builder.setContentTitle("New Friend Request");
        builder.setContentText(requestUsername + " wants to be your friend");
        builder.setSmallIcon(R.drawable.ic_friend_request);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1, builder.build());
    }

    /*private void requestNotificationPolicyAccess() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            }
        }
    }*/
}