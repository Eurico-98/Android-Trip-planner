package com.example.projecto_cm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;

import com.example.projecto_cm.Fragments.Frag_Login;
import com.example.projecto_cm.Interfaces.FragmentChangeListener;

public class Main_Activity extends AppCompatActivity implements FragmentChangeListener {

    /**
     * on create of login_register activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);

        // load login fragment
        Frag_Login frag_login = new Frag_Login();
        replaceFragment(frag_login);
    }

    /**
     * to navigate through fragments
     * @param fragment
     */
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.activity_login_register_container, fragment);
        fragmentTransaction.commit();
    }
}