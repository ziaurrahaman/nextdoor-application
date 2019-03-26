package com.example.nextdoorapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
//import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImageView;

import java.net.URI;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpActivity extends AppCompatActivity {

    private EditText username;
    private EditText fullname;
    private EditText adress;
    private Button save;
    CircleImageView profile_image;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    String currentUserId;
    ProgressDialog progressDialog;
    final static int GALERY_PIC = 1;
    private StorageReference profileImageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        profileImageReference = FirebaseStorage.getInstance().getReference().child("profileimage");

        username = findViewById(R.id.username_id);
        fullname = findViewById(R.id.fullname_id);
        adress = findViewById(R.id.adress_id);
        save = findViewById(R.id.save_button_id);
        profile_image = findViewById(R.id.profile_image_id);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccoutInformation();
            }
        });
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galeryIntent = new Intent();
                galeryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galeryIntent.setType("image/*");
                startActivityForResult(galeryIntent,GALERY_PIC);
            }
        });
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")){
                        String image = dataSnapshot.child("profileimage").getValue().toString();

                        Toast.makeText(getApplicationContext(),"image uri :"+image,Toast.LENGTH_SHORT).show();
                        Log.d("picaso","image url: "+image);
                        Picasso.get().setLoggingEnabled(true);
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profile_image);
                    }else {
                        Toast.makeText(getApplicationContext(),"profile image does not exist",Toast.LENGTH_SHORT).show();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Toast.makeText(getApplicationContext(),"current user id: "+currentUserId,Toast.LENGTH_SHORT).show();

        progressDialog = new ProgressDialog(this);

    }


    @Override
    protected void onActivityResult(int requestCode , int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALERY_PIC && resultCode == RESULT_OK && data!= null){
            progressDialog.setTitle("Profile Image");
            progressDialog.setMessage("Wait while you updating your profie image");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);
            Uri imageuri = data.getData();
            if(imageuri==null){
                Toast.makeText(getApplicationContext(),"image uri is null",Toast.LENGTH_SHORT).show();
            }
            final StorageReference filepath = profileImageReference.child(currentUserId+".jpg");
            filepath.putFile(imageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(SetUpActivity.this, "Your image save successfully", Toast.LENGTH_SHORT).show();

                       //  final  String downloadurl = task.getResult().getDownloadUrl().toString();
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadurl = uri.toString();
                                reference.child("profileimage").setValue(downloadurl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Intent selfIntent = new Intent(SetUpActivity.this, SetUpActivity.class);
                                            startActivity(selfIntent);
                                            progressDialog.dismiss();
                                            Toast.makeText(SetUpActivity.this, "your image path is saved successfully into firebasedatabase", Toast.LENGTH_SHORT).show();

                                        }else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SetUpActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }

                                    }
                                });
                            }
                        });
//                        reference.child("profileimage").setValue(downloadurl).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if (task.isSuccessful()) {
//                                    Intent selfIntent = new Intent(SetUpActivity.this, SetUpActivity.class);
//                                    startActivity(selfIntent);
//                                    progressDialog.dismiss();
//                                    Toast.makeText(SetUpActivity.this, "your image path is saved successfully into firebasedatabase", Toast.LENGTH_SHORT).show();
//
//                                }else {
//                                    String message = task.getException().getMessage();
//                                    Toast.makeText(SetUpActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
//                                    progressDialog.dismiss();
//                                }
//
//                            }
//                        });

                    }else {

                        String message = task.getException().getMessage();
                        Toast.makeText(SetUpActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                }
            });
        }else {

            Toast.makeText(SetUpActivity.this, "you cant save this image " , Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAccoutInformation() {
        String userName = username.getText().toString();
        String fullName = fullname.getText().toString();
        String userAdress = adress.getText().toString();

        if (userName == null) {
            Toast.makeText(getApplicationContext(), "please enter your username", Toast.LENGTH_SHORT).show();
        } else if (fullName == null) {
            Toast.makeText(getApplicationContext(), "please enter yor full name", Toast.LENGTH_SHORT).show();
        } else if (userAdress == null) {
            Toast.makeText(getApplicationContext(), "please enter your username", Toast.LENGTH_SHORT).show();
        }else {

            progressDialog.setTitle("Creating New accocunt");
            progressDialog.setMessage("Wait while creating your account");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);
            HashMap userInformationMap = new HashMap();
            userInformationMap.put("user name : ", userName);

            userInformationMap.put("full name : ", fullName);
            userInformationMap.put("adress : ", userAdress);
            reference.updateChildren(userInformationMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"You are successfully loggedin",Toast.LENGTH_SHORT).show();
                        sendUserToMainActivity();
                    }else{
                        String message = task.getException().getMessage();
                        Toast.makeText(getApplicationContext(),"Error : " +message,Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                }
            });
        }

    }

    private void sendUserToMainActivity() {
        Intent intent =new Intent(SetUpActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
