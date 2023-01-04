package com.example.projecto_cm;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.android.gms.tasks.Task;

public class DialogReceiver extends BroadcastReceiver {

    private DAO_helper dao;

    @Override
    public void onReceive(Context context, Intent intent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String requestUsername = intent.getStringExtra("request_username");
        String myUsername = intent.getStringExtra("my_username");
        builder.setTitle("Friend Request Confirmation");
        builder.setMessage("Are you sure you want to add " + requestUsername + " as your friend");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                System.out.println("-----------------------------------------------" + requestUsername);
                dao = new DAO_helper((FragmentActivity) context);
                dao.add_friend_request(requestUsername, myUsername, "add");



            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });
        builder.show();
    }

}