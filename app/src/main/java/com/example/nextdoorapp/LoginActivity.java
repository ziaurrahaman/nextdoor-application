package com.example.nextdoorapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button logInButton;
    private TextView signup_link;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText)findViewById(R.id.enter_email_id);
        password = (EditText)findViewById(R.id.enter_password_id);
        logInButton = (Button)findViewById(R.id.login_button_id);
        signup_link = findViewById(R.id.signup_link_id);
        signup_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             sendUserToRegisterActivity();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowUserToLogin();

            }
        });
        loadingbar = new ProgressDialog(this);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

    }

    @Override
    protected void onStart() {
        if(mAuth.getCurrentUser()!= null){
            sendUserToMainActivity();
        }
        super.onStart();
    }

    private void allowUserToLogin() {
        String emailInThisField = email.getText().toString();
        String passwordInThisField = password.getText().toString();
        if(TextUtils.isEmpty(emailInThisField)){
            Toast.makeText(getApplicationContext(),"Please enter your emai adress",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(passwordInThisField)){
            Toast.makeText(getApplicationContext(),"Please enter your password",Toast.LENGTH_SHORT).show();
        }else {

            loadingbar.setTitle("Login");
            loadingbar.setMessage("Wait you are logging to your account .......");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);
            mAuth.signInWithEmailAndPassword(emailInThisField, passwordInThisField).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        sendUserToMainActivity();
                        Toast.makeText(getApplicationContext(),"You are successfully logged in",Toast.LENGTH_SHORT).show();

                        loadingbar.dismiss();

                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(getApplicationContext(), "Error occured: " + message, Toast.LENGTH_SHORT).show();
                        Log.d("Nextdoorapp", "Error: " + message);
                        loadingbar.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity() {


        Intent intent =new Intent(LoginActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }





    private void sendUserToRegisterActivity() {

        Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
        startActivity(intent);
       // finish();
    }
}
