package com.example.nextdoorapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private EditText confirm_password;
    private EditText area_name;
    private Button create_account_button;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        email = findViewById(R.id.enter_email_id);
        password = findViewById(R.id.enter_password_id);
        confirm_password = findViewById(R.id.conform_password_id);
        area_name = findViewById(R.id.areaname_id);
        create_account_button = findViewById(R.id.creat_account_button_id);
        create_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // createAccount();
                String emailForValidation = email.getText().toString();
                isEmailValid(emailForValidation);
                isPasswordValid( password);
//                isAreaValid(area_name);
//                creteFirebaseUser();
                if(isAreaValid(area_name)==true){
                    creteFirebaseUser();
                }else {
                    Toast.makeText(getApplicationContext(),"You can not  open an account ",Toast.LENGTH_SHORT).show();
                }
        }
        });
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
    }

    @Override
    protected void onStart() {
        if(mAuth.getCurrentUser()!= null){
            sendUserToMainActivity();
        }
        super.onStart();
    }

    private void sendUserToMainActivity() {
        Intent intent =new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void creteFirebaseUser() {

        String emailFieldForThisMethod = email.getText().toString();
        String passwordFieldForThisMethod = password.getText().toString();

        loadingBar.setTitle("Creating Account");
        loadingBar.setMessage("Account is creatin.......");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);
        mAuth.createUserWithEmailAndPassword(emailFieldForThisMethod,passwordFieldForThisMethod).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    sendUserToSetUpActivity();
                    Toast.makeText(getApplicationContext(),"You are authenticated successfully",Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }else {
                    String message = task.getException().getMessage();
                    Toast.makeText(getApplicationContext(),"Error ouccured: " +message,Toast.LENGTH_SHORT).show();
                    Log.d("nextdoorApp","Error: " +message);
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void sendUserToSetUpActivity() {
        Intent setupIntent = new Intent(this,SetUpActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();

    }

    private boolean isAreaValid(EditText area_name) {
        String area_nameInMethod = area_name.getText().toString();
        String ourSelectedAreaName = "Rajarbag";
        if(TextUtils.equals(area_nameInMethod,ourSelectedAreaName)){

            return true;
        }else {
          //  Toast.makeText(getApplicationContext(),"You can not  open an account ",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void isPasswordValid(EditText password) {
        String confirm_password_inMethod = confirm_password.getText().toString();
        String passwordInMethod = password.getText().toString();
          if(confirm_password_inMethod.equals(passwordInMethod) && confirm_password_inMethod.length()>4){
              Toast.makeText(getApplicationContext(),"Your password is matched",Toast.LENGTH_SHORT).show();
          }else{
              Toast.makeText(getApplicationContext(),"Your confirm password is not matched with your given password",Toast.LENGTH_SHORT).show();
          }

    }

    private void isEmailValid(String email) {

        if(email.contains("@")){

        }
        else {
            Toast.makeText(getApplicationContext(),"This is not an valid emai adress",Toast.LENGTH_SHORT).show();
        }
    }

//    private void createAccount() {
//        String email_inMethod = email.getText().toString();
//        String password_inMethod = password.getText().toString();
//        String confirm_passwordkk_inMethod = confirm_password.getText().toString();
//        String areaname_inMethod = confirm_password.getText().toString();
//        if(TextUtils.isEmpty(email_inMethod)){
//            Toast.makeText(getApplicationContext(),"Please enter your email",Toast.LENGTH_SHORT).show();
//        }else if (TextUtils.isEmpty(password_inMethod)){
//            Toast.makeText(getApplicationContext(),"Please enter your password",Toast.LENGTH_SHORT).show();
//        }else if(TextUtils.isEmpty(confirm_passwordkk_inMethod)){
//            Toast.makeText(getApplicationContext(),"Please confirm your email",Toast.LENGTH_SHORT).show();
//        }else if(TextUtils.isEmpty(areaname_inMethod)){
//            Toast.makeText(getApplicationContext(),"Please enter your area name",Toast.LENGTH_SHORT).show();
//        }
//
//
//
//    }
}
