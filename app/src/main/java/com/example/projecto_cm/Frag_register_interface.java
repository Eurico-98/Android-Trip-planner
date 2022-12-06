package com.example.projecto_cm;

import android.widget.EditText;

public interface Frag_register_interface {

    void create_new_account(String username, String email, String password, DAO_helper dao, int mail_exists);
    void check_mail(String username, String email, String password, DAO_helper dao, int username_exists);
}
