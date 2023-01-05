package com.example.projecto_cm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.example.projecto_cm.DB_entities.MyUser;
import com.example.projecto_cm.DB_entities.Trip;
import com.example.projecto_cm.Fragments.Frag_Home_Screen;
import com.example.projecto_cm.Fragments.Frag_Photo_Album;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class DAO_helper extends SQLiteOpenHelper {

    // get firebase database reference
    private final DatabaseReference databaseReference;
    private Context context;

    // SQLITE database
    public static final String DATABASE_NAME = "User_Profile_data.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_USER_PROFILE = "user_profile";
    private static final String COLUMN_ID = "id_";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FULLNAME = "fullName";
    private static final String COLUMN_PHOTO = "photo";
    private static final String COLUMN_LOGIN_CHECK = "login_control";

    /**
     * constructor to get fire base data base instance
     */
    public DAO_helper(@Nullable FragmentActivity context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

        // since the firebase database is at the default location us-central1
        // it is necessary to pass the location of this database - in this case is https://projecto-cm-aa116-default-rtdb.europe-west1.firebasedatabase.app/
        // this was something i defined when creating the database in the firebase website
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://projecto-cm-aa116-default-rtdb.europe-west1.firebasedatabase.app/");

        // get the reference for the root of the database
        databaseReference = db.getReference();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query1 = "CREATE TABLE " + TABLE_USER_PROFILE +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_FULLNAME + " TEXT, " +
                COLUMN_PHOTO + " BLOB, " +
                COLUMN_LOGIN_CHECK + " INTEGER);";
        db.execSQL(query1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROFILE);
        onCreate(db);
    }




    /**
     * check username name when registering new account - also used to edit username
     * @param username
     * @param password
     * @param dao
     * @param fg
     */
    public void checkInsertedUsername(String username, String password, DAO_helper dao, Frag_Register fg) {

        DatabaseReference userNameRef = databaseReference.child("Users").child(username);

        // check if username is already registered
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // if username is not registered call method of Frag_Register to check if email is registered
                if(!dataSnapshot.exists()) {
                    fg.createNewAccount(username, password, dao, 0);
                }
                else{
                    fg.createNewAccount(username, password, dao, 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }

    /**
     * add user account to firebase and sqlite databases
     * @param username
     * @param password
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public Task<Void> addNewUserAccount(String username, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {

        // encrypt password
        String hashedPass = generateStrongPasswordHash(password);

        // save data to sqlite database
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_FULLNAME, "Not set");
        cv.put(COLUMN_PASSWORD, hashedPass);
        cv.put(COLUMN_LOGIN_CHECK, 0); // user not logged in
        db.insert(TABLE_USER_PROFILE, null, cv);

        // save data to firebase database
        MyUser new_user = new MyUser(username, hashedPass, "Not Set");
        return databaseReference.child("Users").child(username).setValue(new_user);
    }

    /**
     * get user data from sqlite database
     * @return
     */
    public Cursor getUserData(String username){
        String query = "SELECT * FROM " + TABLE_USER_PROFILE + " WHERE " + COLUMN_USERNAME + " = '" + username + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }


    /**
     * save picture to databases
     */
    public void savePicture(String username, Bitmap imageBitmap, String type_of_picture){

        // save image in sqlite data base if it is a profile image
        if(Objects.equals(type_of_picture, "profile")){

            // get the database instance
            SQLiteDatabase db = getWritableDatabase();

            // convert the bitmap to a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            byte[] imageByteArray = outputStream.toByteArray();

            // create a ContentValues instance to store the image data
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_PHOTO, imageByteArray);

            Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + TABLE_USER_PROFILE + " WHERE " + COLUMN_USERNAME + " = '" + username + "'", null);
            cursor.moveToNext();
            String id = cursor.getString(0);

            db.update(TABLE_USER_PROFILE, cv, "id_=?", new String[]{id});
        }
    }



    /**
     * check user credentials for login and set login check value in sqlite db to 1 if credentials are valid and correct
     * @param username
     * @param password
     * @param fg
     */
    public void checkCredentials(String username, String password, Frag_Login fg) {

        DatabaseReference userNameRef = databaseReference.child("Users").child(username);
        SQLiteDatabase db = this.getWritableDatabase();

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

                                    // change login status of user in sqlite database
                                    ContentValues cv = new ContentValues();
                                    cv.put(COLUMN_LOGIN_CHECK, 1);
                                    db.update(TABLE_USER_PROFILE, cv, "username=?", new String[]{username});

                                    fg.result("Valid user", username);
                                }
                                else{
                                    fg.result("Wrong password!", "");
                                }
                            } catch (NoSuchAlgorithmException | InvalidKeySpecException ignored) {}
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

    /**
     * to logout user from sqlite - change the status of login column
     * @return
     */
    public void logoutUser(String username){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LOGIN_CHECK, 0);
        db.update(TABLE_USER_PROFILE, cv, "username=?", new String[]{username});
    }



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


    /**
     * to get photos of a trip
     * @param username
     * @param fg
     */
    public void getTripPhotos(String username, String trip_title, Frag_Photo_Album fg){

        DatabaseReference userNameRef = databaseReference.child("Users").child(username).child("my_trips");

        // check if username is already registered
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
                Gson gson = new Gson();

                // find trip
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    // convert firebase data to json object to than convert to trip object
                    String json = gson2.toJson(ds.getValue());
                    Trip trip = gson.fromJson(json, Trip.class);

                    if(trip.getTitle().equals(trip_title)){

                        // return list of photos if it exists
                        fg.setPhotoAlbum(trip.getTrip_album());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }


    /**
     * add new photo to firebase database
     * @param username
     * @param trip_title
     */
    public void addOrDeletePhoto(String username, String trip_title, String imageEncoded, Frag_Photo_Album fg, String operation){

        DatabaseReference userNameRef = databaseReference.child("Users").child(username).child("my_trips");

        // check if username is already registered
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Trip> my_trips = new ArrayList<>();
                Gson gson = new Gson();

                // find trip and update photo album
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    // convert firebase data to json object to than convert to trip object
                    Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
                    String json = gson2.toJson(ds.getValue());
                    Trip trip = gson.fromJson(json, Trip.class);

                    if(trip.getTitle().equals(trip_title)){

                        ArrayList<String> trip_album = new ArrayList<>();

                        // return list of photos if it exists
                        if(trip.getTrip_album() != null){
                            trip_album = trip.getTrip_album();
                        }

                        // to add
                        if(operation.equals("added")){
                            trip_album.add(imageEncoded);
                        }
                        // to delete
                        else{
                            trip_album.remove(Integer.parseInt(imageEncoded));
                        }

                        trip.setTrip_album(trip_album);
                    }

                    my_trips.add(trip);
                }

                fg.getDAOResultMessage(databaseReference.child("Users").child(username).child("my_trips").setValue(my_trips));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }


    /**
     * to edit username, full name of user
     * @param username
     * @param fhs
     * @param new_username
     * @param new_fullname
     */
    public void editUserProfile(String username, Frag_Home_Screen fhs, String new_username, String new_fullname) {

        DatabaseReference userNameRef = databaseReference.child("Users").child(new_username);
        SQLiteDatabase db = this.getWritableDatabase();

        // check if username is already registered
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // if username is not registered call method of Frag_Register to check if email is registered
                if(!dataSnapshot.exists() ||  (dataSnapshot.exists() && new_username.equals(username))) {

                    // get user instance from firebase to get list of trips
                    DatabaseReference userNameRefToDelete = databaseReference.child("Users").child(username);

                    ValueEventListener userNameEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot2) {

                            Map<String, Object> map = (Map<String, Object>) dataSnapshot2.getValue();

                            // update in firebase ----------------------------------------------------------------------------------------------
                            MyUser updated_user = new MyUser(username, (String) map.get("password"), (String) map.get("fullName"));

                            updated_user.setFullName(new_fullname);// edit fullname
                            updated_user.setUsername(new_username);// edit username

                            // add trips and friends list if they exist
                            try {
                                List<Trip> my_trips = (List<Trip>) map.get("my_trips");
                                updated_user.setMy_trips(my_trips);
                            } catch (Exception ignored) {}

                            try {
                                List<String> my_friends = (List<String>) map.get("my_friends");
                                updated_user.setMy_friends(my_friends);
                            } catch (Exception ignored) {}
                            // update in firebase ----------------------------------------------------------------------------------------------

                            // first try to remove old entry of user
                            userNameRefToDelete.removeValue().addOnSuccessListener(suc ->

                                    // than try to insert new entry
                                    databaseReference.child("Users").child(new_username).setValue(updated_user).addOnSuccessListener(suc2 -> {

                                        // update in sqlite ----------------------------------------------------------------------------------------------
                                        ContentValues cv = new ContentValues();
                                        cv.put(COLUMN_FULLNAME, new_fullname);
                                        cv.put(COLUMN_USERNAME, new_username);

                                        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + TABLE_USER_PROFILE + " WHERE " + COLUMN_USERNAME + " = '" + username + "'", null);
                                        cursor.moveToNext();
                                        String id = cursor.getString(0);

                                        db.update(TABLE_USER_PROFILE, cv, "id_=?", new String[]{id});
                                        // update in sqlite ----------------------------------------------------------------------------------------------

                                        // send result
                                        fhs.showResultMessage("Profile edited Successfully!");

                            }).addOnFailureListener(er -> fhs.showResultMessage("Failed to edit profile due to network error! Profile delete from online server!"))).addOnFailureListener(er ->
                                    fhs.showResultMessage("Failed to edit profile due to network error!"));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    };
                    userNameRefToDelete.addListenerForSingleValueEvent(userNameEventListener);
                }
                else{
                    fhs.showResultMessage(new_username + " already exists!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }

    /**
     * to change password
     * @param username
     * @param fhs
     * @param new_password
     */
    public void changeUserPassword(String username, Frag_Home_Screen fhs, String new_password) {

        DatabaseReference userNameRef = databaseReference.child("Users").child(username);
        SQLiteDatabase db = this.getWritableDatabase();

        // check if username is already registered
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    String hashed = generateStrongPasswordHash(new_password);
                    databaseReference.child("Users").child(username).child("password").setValue(hashed)
                            .addOnSuccessListener(suc -> {

                                // update in sqlite
                                ContentValues cv = new ContentValues();
                                Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + TABLE_USER_PROFILE + " WHERE " + COLUMN_USERNAME + " = '" + username + "'", null);
                                cursor.moveToNext();
                                String id = cursor.getString(0);
                                cv.put(COLUMN_PASSWORD, hashed);
                                db.update(TABLE_USER_PROFILE, cv, "id_=?", new String[]{id});

                                fhs.changePassResult("Password changed successfully!");

                        }).addOnFailureListener(er -> fhs.changePassResult("Failed to change password due to network error!"));

                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    fhs.changePassResult("Failed to change password due to encryption error!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }


    /**
     * delete user account from firebase and sqlite
     * @param username
     */
    public void deleteUserAccount(String username, Frag_Home_Screen fhs){

        DatabaseReference userNameRef = databaseReference.child("Users").child(username);
        SQLiteDatabase db = this.getWritableDatabase();

        // check if username is already registered
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                userNameRef.removeValue().addOnSuccessListener(suc -> {

                    Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + TABLE_USER_PROFILE + " WHERE " + COLUMN_USERNAME + " = '" + username + "'", null);
                    cursor.moveToNext();
                    String id = cursor.getString(0);
                    db.delete(TABLE_USER_PROFILE, "id_=?", new String[]{id});
                    fhs.deleteAccountResult("Account deleted successfully!");

                }).addOnFailureListener(er -> fhs.deleteAccountResult("Failed to delete account due to network error!"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }



    //-------------------------------------------------Add Friend --------------------------------

    /**
     * It verifies if a user exists or if a user already is friends with them
     * @param username
     * @param newFriend
     * @param fg
     */
    public void search_friend(String newFriend, String username, Frag_Home_Screen fg){
        DatabaseReference userNameRef = databaseReference.child("Users").child(newFriend);

        // check if username is already registered
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String usernames;

                // if user exists return
                if(dataSnapshot.exists()) {

                    usernames = dataSnapshot.child("username").getValue(String.class);
                    DatabaseReference userNameRef2 = databaseReference.child("Users").child(username).child("my_friends");
                    String finalUsernames = usernames;

                    ValueEventListener userNameEventListener2 = new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int ctrl = 0;

                            if(dataSnapshot.exists()) {

                                System.out.println("-------------------------------------------1");

                                for(DataSnapshot ds : dataSnapshot.getChildren()){

                                    if(Objects.requireNonNull(ds.getValue()).toString().equals(newFriend)){
                                        System.out.println("-------------------------------------------2");
                                        try {
                                            fg.searchResult("Already Friends");
                                            ctrl = 1;
                                        } catch (MqttException | IOException ignored) {}
                                        break;
                                    }
                                }

                                if(ctrl!=1) {
                                    System.out.println("-------------------------------------------3");
                                    try {
                                        fg.searchResult(finalUsernames);
                                    } catch (MqttException | IOException ignored) {}
                                }
                            }
                            else{
                                System.out.println("-------------------------------------------4");
                                try {
                                    fg.searchResult(finalUsernames);
                                } catch (MqttException | IOException ignored) {}
                            }

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    };
                    userNameRef2.addListenerForSingleValueEvent(userNameEventListener2);

                }
                else{
                    try {
                        fg.searchResult("no result found");
                    } catch (MqttException | IOException ignored) {}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }

    /**
     * Adds a new user to the the friends list of another user
     * @param friend_to_add
     * @param username
     * @param operation
     */
    public void addOrDelete_friend_request(String friend_to_add, String username, String operation, int pos, String friend_to_rem){

        DatabaseReference userNameRef = databaseReference.child("Users").child(username).child("my_friends");

        // check if username is already registered
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<String> my_friends = new ArrayList<>();

                // if user already has a list of friends
                if(dataSnapshot.exists()) {
                    System.out.println("------------------------------------------------ lista de amigos: "+ dataSnapshot.getValue());
                    my_friends = (List<String>) dataSnapshot.getValue();
                }

                // add new friend
                if(Objects.equals(operation, "add")){
                    my_friends.add(friend_to_add);
                }
                else {
                    if (pos!=-1){
                        my_friends.remove(pos);
                    }else{
                        my_friends.remove(friend_to_rem);
                    }
                }
                databaseReference.child("Users").child(username).child("my_friends").setValue(my_friends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }


    /**
     * get user friends from firebase
     * @param username
     * @param fg
     */
    public void getUserFriends(String username, Frag_Home_Screen fg){

        DatabaseReference userNameRef = databaseReference.child("Users").child(username).child("my_friends");

        // check if username is already registered
        ValueEventListener userNameEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<String> my_friends = new ArrayList<>();

                // if user has friends return them
                if(dataSnapshot.exists()) {

                    // convert data snapshots to hashmap
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        my_friends.add(Objects.requireNonNull(ds.getValue()).toString());
                    }
                }
                fg.getMyFriends(my_friends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(userNameEventListener);
    }

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
}
