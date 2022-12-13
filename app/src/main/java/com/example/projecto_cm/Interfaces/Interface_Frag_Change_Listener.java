package com.example.projecto_cm.Interfaces;

import androidx.fragment.app.Fragment;

public interface Interface_Frag_Change_Listener {

    /**
     * interface for the method to change fragments
     * @param fragment
     */
    void replaceFragment(Fragment fragment, String keep_frag_in_stack);
}
