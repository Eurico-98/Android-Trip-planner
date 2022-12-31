package com.example.projecto_cm;

import androidx.annotation.NonNull;

import com.example.projecto_cm.DB_entities.MyUser;
import com.example.projecto_cm.DB_entities.Trip;
import com.example.projecto_cm.Fragments.Frag_Home_Screen;
import com.example.projecto_cm.Fragments.Frag_Trip_Planner;
import com.example.projecto_cm.Fragments.Frag_List_My_Trips;
import com.example.projecto_cm.Fragments.Frag_Login;
import com.example.projecto_cm.Fragments.Frag_Register;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class DAO_helper{

    // get firebase database reference
    private final DatabaseReference databaseReference;

    /**
     * constructor to get fire base data base instance
     */
    public DAO_helper() {

        // since the firebase database is at the default location us-central1
        // it is necessary to pass the location of this database - in this case is https://projecto-cm-aa116-default-rtdb.europe-west1.firebasedatabase.app/
        // this was something i defined when creating the database in the firebase website
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://projecto-cm-aa116-default-rtdb.europe-west1.firebasedatabase.app/");

        // get the reference for the root of the database
        databaseReference = db.getReference();
    }



    // ------------------------------------------ Methods for registration ------------------------------------------------------------

    /**
     * check username name when registering new account - also used to edit username
     * @param username
     * @param email
     * @param password
     * @param dao
     * @param fg
     */
    public void checkInsertedUsername(String username, String email, String password, DAO_helper dao, Frag_Register fg) {

        DatabaseReference userNameRef = databaseReference.child("Users").child(username);

        // check if username is already registered
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // if username is not registered call method of Frag_Register to check if email is registered
                if(!dataSnapshot.exists()) {
                    fg.checkMail(username, email, password, dao, 0);
                }
                else{
                    fg.checkMail(username, email, password, dao, 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }



    /**
     * check email inserted when registering new account
     * @param username
     * @param email
     * @param password
     * @param dao
     * @param fg
     */
    public void checkInsertedEmail(String username, String email, String password, DAO_helper dao, Frag_Register fg) {

        DatabaseReference emailRef = databaseReference.child("Users"); //.child(username).child("email");

        System.out.println("------------------------------- aqui 4");

        // check if mail is already registered
        ValueEventListener mailEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String user_data;
                int mail_exists = 0;

                // if email is not registered call method of Frag_Register to create new account
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    user_data = child.getValue().toString();

                    if(user_data.equals(email)) {
                        System.out.println("------------------------------- aqui 5");
                        mail_exists = 1;
                        break;
                    }
                }

                if(mail_exists == 0){
                    fg.createNewAccount(username, email, password, dao, 0);
                }
                else {
                    fg.createNewAccount(username, email, password, dao, 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        emailRef.addListenerForSingleValueEvent(mailEventListener);
    }

    /**
     * add user account to firebase
     * @param username
     * @param password
     * @param email
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public Task<Void> addNewUserAccount(String username, String password, String email) throws NoSuchAlgorithmException, InvalidKeySpecException {

        // encrypt password
        String hashedPass = generateStrongPasswordHash(password);

        MyUser new_user = new MyUser(username, email, hashedPass);

        // insert new user (child) into the Users reference tree
        return databaseReference.child("Users").child(username).setValue(new_user);
    }
    // ------------------------------------------ Methods for registration ------------------------------------------------------------



    // ------------------------------------------ Methods for login -------------------------------------------------------------------
    /**
     * check user credentials
     * @param username
     * @param password
     * @param fg
     */
    public void checkCredentials(String username, String password, Frag_Login fg) {

        DatabaseReference userNameRef = databaseReference.child("Users").child(username);

        // check if username exists
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // if it exists check password
                if(dataSnapshot.exists()) {

                    DatabaseReference passwordRef = databaseReference.child("Users").child(username).child("password");
                    ValueEventListener passwordEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            // decrypt password in database and compare
                            try {
                                if(validatePassword(password, Objects.requireNonNull(dataSnapshot.getValue()).toString())){
                                    fg.result("Valid user", username);
                                }
                                else{
                                    fg.result("Wrong password!", "");
                                }
                            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    };
                    passwordRef.addListenerForSingleValueEvent(passwordEventListener);
                }
                else{
                    fg.result("Username not registered!", "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }
    // ------------------------------------------ Methods for login -------------------------------------------------------------------


    // ------------------------------------------ Methods for adding trips and getting trips to user data ------------------------------
    /**
     * get user trips from firebase
     * @param username
     * @param fg
     */
    public void getUserTrips(String username, Frag_List_My_Trips fg){

        DatabaseReference userNameRef = databaseReference.child("Users").child(username).child("my_trips");

        // check if username is already registered
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<String> my_trips = new ArrayList<>();

                // if user has trips return them
                if(dataSnapshot.exists()) {

                    // convert data snapshots to hashmap
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        my_trips.add(Objects.requireNonNull(ds.getValue()).toString());
                    }
                }
                fg.getMyTrips(my_trips);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }

    /**
     * add new trips to firebase or update existing trips
     * @param username
     * @param fg_create
     * @param trip_to_edit
     */
    public void addOrUpdateOrDeleteTrip(String username, Frag_Trip_Planner fg_create, Frag_List_My_Trips fg_list, Trip trip_to_edit, String operation, int position){

        DatabaseReference userNameRef = databaseReference.child("Users").child(username).child("my_trips");

        // check if username is already registered
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Trip> my_trips = new ArrayList<>();

                // if user already has a list of trips
                if(dataSnapshot.exists()) {
                    System.out.println("------------------------------------------------ lista de viagens: "+ dataSnapshot.getValue());
                    my_trips = (List<Trip>) dataSnapshot.getValue();
                }

                // add new trip
                if(Objects.equals(operation, "add")){
                    my_trips.add(trip_to_edit);
                }

                // update trip
                else if(Objects.equals(operation, "update")){
                    my_trips.set(position, trip_to_edit);
                }
                else {
                    my_trips.remove(position);
                }

                // return task that has a on success listener
                if(fg_create != null){
                    fg_create.getDAOResultMessage(databaseReference.child("Users").child(username).child("my_trips").setValue(my_trips));
                }
                else if(fg_list != null) {
                    fg_list.getDAOResultMessage(databaseReference.child("Users").child(username).child("my_trips").setValue(my_trips));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }
    // ------------------------------------------ Methods for adding trips and getting trips to user data ------------------------------


    // ------------------------------------------ Methods to edit user profile -----------------------------------------------------
    public void editUserProfile(String username, Frag_Home_Screen fhs, String new_username, String new_email, String new_password, String new_fullname) {

        DatabaseReference userNameRef = databaseReference.child("Users").child(username);

        // check if username is already registered
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // if username is not registered call method of Frag_Register to check if email is registered
                if(!dataSnapshot.exists()) {

                    // convert data to user entity to update values
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    MyUser updated_user = new MyUser((String) map.get("username"), (String) map.get("email"), (String) map.get("password"));

                    // edit username if inserted
                    if(new_username != "")
                        updated_user.setUsername(username);

                    // edit email if inserted
                    if(new_email != "")
                        updated_user.setUsername(new_email);

                    // edit password if inserted
                    if(new_password != "") {
                        try {
                            updated_user.setUsername(generateStrongPasswordHash(new_password));
                        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                    }

                    // add trips if they exists
                    try{
                        List<Trip> my_trips = (List<Trip>) map.get("my_trips");
                        updated_user.setMy_trips(my_trips);
                    } catch (Exception ignored) {}


                    // add fullname
                    try {
                        String fullname = (String) map.get("fullName");
                        updated_user.setFullName(fullname);
                    } catch (Exception ignored) {}

                    // if it was updated change it
                    if(new_fullname != "")
                        updated_user.setFullName(new_fullname);


                    // ------------------------------------- FALTA A PORRA DA FOTO

                    // ------------------------- vai faltar a lista de amigos

                    // remove old entry of user
                    userNameRef.removeValue();

                    // add updated user
                    fhs.showResultMessage(databaseReference.child("Users").child(new_username).setValue(updated_user));
                }
                else{
                    fhs.showResultMessage("Username already exists!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }
    // ------------------------------------------ Methods to edit user profile -----------------------------------------------------



    // ------------------------------------------------- password encryption methods --------------------------------------------------
    private static String generateStrongPasswordHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {

        int iterations = 1000;
        char[] chars = password.toCharArray();
        byte[] salt = getSalt();

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64*8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        byte[] hash = skf.generateSecret(spec).getEncoded();
        return iterations + ":" + toHex(salt) + ":" + toHex(hash);
    }

    private static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    private static String toHex(byte[] array) {

        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);

        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    private static boolean validatePassword(String originalPassword, String storedPassword)throws NoSuchAlgorithmException, InvalidKeySpecException {

        String[] parts = storedPassword.split(":");
        int iterations = Integer.parseInt(parts[0]);

        byte[] salt = fromHex(parts[1]);
        byte[] hash = fromHex(parts[2]);

        PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] testHash = skf.generateSecret(spec).getEncoded();

        int diff = hash.length ^ testHash.length;
        for (int i = 0; i < hash.length && i < testHash.length; i++) {
            diff |= hash[i] ^ testHash[i];
        }
        return diff == 0;
    }

    private static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
    // ------------------------------------------------- password encryption methods --------------------------------------------------

}
