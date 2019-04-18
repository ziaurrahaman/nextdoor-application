package com.example.nextdoorapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton  postImageButton;
    private EditText comment_input_text;
    private RecyclerView commentList;
    private String postkey;
    private DatabaseReference userRef,postRef;
    private FirebaseAuth mAuth;
    private String current_user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        postkey = getIntent().getExtras().get("postKey").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        postRef = FirebaseDatabase.getInstance().getReference().child("posts").child(postkey).child("comments");
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        postImageButton = findViewById(R.id.send_button_id);
        comment_input_text = findViewById(R.id.comments_input_id);
        commentList = findViewById(R.id.comments_list_id);
        commentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentList.setLayoutManager(linearLayoutManager);
        postImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String username = (String) dataSnapshot.child(current_user_id).child("user name : ").getValue().toString();
                            Toast.makeText(getApplicationContext(),"your user name is "+username,Toast.LENGTH_SHORT).show();

                            validateComment(username);
                            comment_input_text.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Comment> options = new FirebaseRecyclerOptions.Builder<Comment>().setQuery(postRef,Comment.class).build();
        FirebaseRecyclerAdapter<Comment,CommentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comment model) {
                holder.userName.setText("@"+model.getUsername());
                holder.date.setText("Date: "+model.getDate());
                holder.comment.setText(model.getComment());
                holder.time.setText("Time: "+model.getTime());


            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_layout,parent,false);
                CommentsViewHolder viewHolder = new CommentsViewHolder(view);
                return viewHolder;
            }
        };
        commentList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        TextView userName,date,comment,time;
        DatabaseReference commentReference;
       // public static String postkey;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.comment_user_name);
           // normalText = itemView.findViewById(R.id.simple_comment_text);
            date = itemView.findViewById(R.id.comment_date_id);
            comment = itemView.findViewById(R.id.user_commetn_text);
            time = itemView.findViewById(R.id.comment_time_id);
           // postkey = getIntent().getExtras().get("postKey").toString();
          //  commentReference = FirebaseDatabase.getInstance().getReference().child("posts").child(postkey).child("comments");
        }
    }

    private void validateComment(String username) {

        String commentInput = comment_input_text.getText().toString();
        if(TextUtils.isEmpty(commentInput)){
            Toast.makeText(getApplicationContext(),"Please write something on commentbox",Toast.LENGTH_SHORT).show();
        }else {

            Calendar calenderForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calenderForDate.getTime());

            Calendar calenderForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calenderForTime.getTime());
            final String randomKey = current_user_id+saveCurrentDate+saveCurrentTime;
            HashMap commentHashmap = new HashMap();
            commentHashmap.put("username",username);
            commentHashmap.put("userid",current_user_id);
            commentHashmap.put("time",saveCurrentTime);
            commentHashmap.put("date",saveCurrentDate);
            commentHashmap.put("comment",commentInput);
            postRef.child(randomKey).updateChildren(commentHashmap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"Your comment saved successfully",Toast.LENGTH_SHORT).show();
                    }else {
                        String error = task.getException().getMessage();
                        Toast.makeText(getApplicationContext(),"Error: "+error,Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}
