package com.example.projecto_cm;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // got another page
    @SuppressLint("NonConstantResourceId")
    public void goToActivity(View view) {

        Intent intent;
        switch(view.getId()) {
            case R.id.button:
                intent = new Intent(this, Function1_Activity.class);
                startActivity(intent);
                break;
            case R.id.button2:
                intent = new Intent(this, Function2_Activity.class);
                startActivity(intent);
                break;
            case R.id.button3:
                intent = new Intent(this, Function3_Activity.class);
                startActivity(intent);
                break;
            default:
                throw new RuntimeException("Unknown button ID");
        }
    }
}