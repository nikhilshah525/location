package com.example.user_location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

public class Login extends AppCompatActivity {
    EditText et_name, et_phone;

    CardView login_card_area;


    //    all error's of textinputlayout
    TextInputLayout error_name,error_password_txt;


    //    shared preference saving state
    public static final String MYPREFERENCE = "mypref";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHON = "phone";
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//      initialization
        error_name = findViewById(R.id.error_name);
        error_password_txt = findViewById(R.id.error_phone);
        et_name = findViewById(R.id.name);
        et_phone = findViewById(R.id.phone);
        login_card_area = findViewById(R.id.login_card_area);
        sharedPreferences = getSharedPreferences(MYPREFERENCE, MODE_PRIVATE);


        check_user_login();


        login_card_area.setOnClickListener(view -> {

//          getting values from textbox
            String name = et_name.getText().toString().trim();
            String phone = et_phone.getText().toString().trim();

            if (name.isEmpty() && phone.isEmpty()) {
                error_name.setError("Email can't be Empty");
                error_password_txt.setError("Password can't be Empty");
            }else {
//              removing error from view
                error_password_txt.setError(null);
                error_name.setError(null);

                Toast.makeText(this, "Login Successfully as "+name, Toast.LENGTH_SHORT).show();


                //editing shared preference using editor
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_NAME, name);
                editor.putString(KEY_PHON, phone);
                editor.apply();


                startActivity(new Intent(this,MainActivity.class));
                finish();
            }
        });



    }



    private void check_user_login() {

        try {

           String name=sharedPreferences.getString("name",null);

            // When user reopens the app
            if (name != null) {
                Toast.makeText(this, "Welcome Back", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this,MainActivity.class));
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


}