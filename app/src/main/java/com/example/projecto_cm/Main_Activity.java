package com.example.projecto_cm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.example.projecto_cm.Fragments.Frag_List_My_Trips;
import com.example.projecto_cm.Fragments.Frag_Login;
import com.example.projecto_cm.Interfaces.Interface_Frag_Change_Listener;
import com.example.projecto_cm.Interfaces.Interface_On_Trip_Route_Ready;

import java.util.Objects;

public class Main_Activity extends AppCompatActivity implements Interface_Frag_Change_Listener {

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
}