package com.example.projecto_cm;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

public class DialogReceiver extends BroadcastReceiver {

    private DAO_helper dao;

    @Override
    public void onReceive(Context context, Intent intent) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String requestUsername = intent.getStringExtra("request_username");
        String myUsername = intent.getStringExtra("my_username");

        builder.setTitle("Friend Request Confirmation");
        builder.setMessage("Are you sure you want to add " + requestUsername + " as your friend?");

        builder.setPositiveButton("Yes", (dialog, which) -> {

            System.out.println("-----------------------------------------------" + requestUsername);
            dao = new DAO_helper((FragmentActivity) context);
            dao.add_friend_request(requestUsername, myUsername, "add");
        });

        builder.setNegativeButton("No", (dialog, which) -> {});
        builder.show();
    }
}