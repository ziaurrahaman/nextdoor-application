package com.example.nextdoorapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddNewPostActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton selecttImageButton;
    private EditText postDescription;
    private Button updateButton;
    private final static int GALERY_PIC = 1;
    private Uri imageUri;
    private String description;
    private FirebaseAuth mAuth;
    private StorageReference postImageReference;
    private DatabaseReference usersRef,postsref;
    private String saveCurrentDate,saveCurrentTime,randomName,downloadUrl,current_user_id;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_post);

        toolbar = findViewById(R.id.add_new_post_toolbar);
        selecttImageButton = findViewById(R.id.post_imagebutton_id);
        updateButton = findViewById(R.id.update_post_button);
        postDescription = findViewById(R.id.about_post_image_text);
        setSupportActionBar(toolbar);
        postImageReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        postsref = FirebaseDatabase.getInstance().getReference().child("posts");
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        progressDialog = new ProgressDialog(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        selecttImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostDescription();
            }
        });

    }

    private void validatePostDescription() {
         description = postDescription.getText().toString();
        if(imageUri==null){
            Toast.makeText(getApplicationContext(),"Please select an image",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(description)){
            Toast.makeText(getApplicationContext(),"Please say something about your image",Toast.LENGTH_SHORT).show();
        }else {


            progressDialog.setTitle("Add New Post");
            progressDialog.setMessage("Wait while we are updating your New Post.....");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);
            storeImageToFirebaseStorage();
        }
    }

    private void storeImageToFirebaseStorage() {

        Calendar calenderForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calenderForDate.getTime());

        Calendar calenderForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calenderForTime.getTime());
        randomName = saveCurrentDate + saveCurrentTime ;
        final StorageReference filepath = postImageReference.child("postImage").child(imageUri.getLastPathSegment() + randomName + ".jpg");
        filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Your post image saved successfully",Toast.LENGTH_SHORT).show();
                     filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            savingPostInformationToDatabase();

                        }
                    });

                }else {
                    String message = task.getException().getMessage();
                    Toast.makeText(getApplicationContext(),"Error : "+message,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void savingPostInformationToDatabase() {

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("full name : ").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid",current_user_id);
                    postMap.put("date",saveCurrentDate);
                    postMap.put("time",saveCurrentTime);
                    postMap.put("description",description);
                    postMap.put("profileimage",userProfileImage);
                    postMap.put("postimage",downloadUrl);
                    postMap.put("fullname",userFullName);
                    postsref.child(randomName+current_user_id).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if(task.isSuccessful()){
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),"Your post is updated successfully",Toast.LENGTH_SHORT).show();
                                sendUserToTheMainActivity();
                            }else {
                                progressDialog.dismiss();
                                String message = task.getException().getMessage();
                                Toast.makeText(getApplicationContext(),"Error: "+message,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALERY_PIC && resultCode == RESULT_OK && data!= null){
            imageUri = data.getData();
            selecttImageButton.setImageURI(imageUri);
        }

    }

    private void openGallery() {
        Intent galeryIntent = new Intent();
        galeryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galeryIntent.setType("image/*");
        startActivityForResult(galeryIntent,GALERY_PIC);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==android.R.id.home){
            sendUserToTheMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUserToTheMainActivity() {
        Intent intent = new Intent(AddNewPostActivity.this,MainActivity.class);
        startActivity(intent);
    }
}
