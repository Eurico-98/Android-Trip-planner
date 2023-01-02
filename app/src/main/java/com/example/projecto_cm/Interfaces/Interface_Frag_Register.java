package com.example.projecto_cm.Interfaces;

import com.example.projecto_cm.DAO_helper;

public interface Interface_Frag_Register {

    void create_new_account(String username, String email, String password, DAO_helper dao, int mail_exists);
    void check_mail(String username, String email, String password, DAO_helper dao, int username_exists);
}
