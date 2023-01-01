package com.example.projecto_cm.Interfaces;

import com.example.projecto_cm.DAO_helper;

public interface Interface_Frag_Register {
    void createNewAccount(String username, String password, DAO_helper dao, int mail_exists);
}
