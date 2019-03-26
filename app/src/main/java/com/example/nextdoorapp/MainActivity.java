package com.example.nextdoorapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postlist;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference,postsreference,likeReference;
    private CircleImageView userProfile_imageView;
    private TextView navUserFullName;
    private ImageButton addNewPostButton;
    String currentUserId ;
    Boolean likeChecker = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        postsreference = FirebaseDatabase.getInstance().getReference().child("posts");
        likeReference = FirebaseDatabase.getInstance().getReference().child("likes");
        navigationView = (NavigationView)findViewById(R.id.navigation_viewid);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerlayout_id);
        postlist = (RecyclerView)findViewById(R.id.allUser_post_list);
        postlist.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postlist.setLayoutManager(linearLayoutManager);
        addNewPostButton = findViewById(R.id.add_newpost_button_id);
        toolbar = (Toolbar)findViewById(R.id.appbar_layout_id) ;
        userProfile_imageView = navView.findViewById(R.id.image_view_id);
        navUserFullName = navView.findViewById(R.id.username_id);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");
        //the work of ActionBarDrawerToggle is unknown to me i have to know it clearly later
        //and what is the work of syncState() is also unkown
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_drawer,R.string.close_drawer);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

      //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        databaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("user name : ")){
                        String fullName = (String) dataSnapshot.child("user name : ").getValue();
                        navUserFullName.setText(fullName);
                    }if(dataSnapshot.hasChild("profileimage")){
                        String image = (String) dataSnapshot.child("profileimage").getValue();
                        Picasso.get().setLoggingEnabled(true);
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(userProfile_imageView);
                    }else {
                        Toast.makeText(getApplicationContext(),"fullname or profile image does not exist...",Toast.LENGTH_SHORT).show();
                    }




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                String message = databaseError.getMessage().toString();
                Toast.makeText(getApplicationContext(),"Error: "+message,Toast.LENGTH_SHORT).show();
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                navigationMenuItemSelector(id);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToTheAddNewPostActivity();
            }
        });
        diaplayAllUserPosts();

    }

    private void diaplayAllUserPosts() {

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(postsreference,Posts.class).build();
        FirebaseRecyclerAdapter<Posts,PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {
                final String postKey = getRef( position).getKey();
                holder.username.setText(model.getFullname());
                holder.time.setText(""+model.getTime());
                holder.date.setText(""+model.getDate());
                holder.description.setText(model.getDescription());
                Picasso.get().load(model.getProfileimage()).into(holder.user_profiel_image);
                Picasso.get().load(model.getPostimage()).into(holder.post_image);
                holder.setLikeButtonStatus(postKey);
                holder.commentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(MainActivity.this,CommentsActivity.class);
                        commentIntent.putExtra("postKey",postKey);
                        startActivity(commentIntent);
                    }
                });
                holder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        likeChecker = true;

                        likeReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(likeChecker.equals(true)){
                                    if(dataSnapshot.child(postKey).hasChild(currentUserId)){

                                        likeReference.child(postKey).child(currentUserId).removeValue();
                                        likeChecker = false;
                                    }else {
                                        likeReference.child(postKey).child(currentUserId).setValue(true);
                                        likeChecker = false;
                                    }
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });



            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alluser_posts_layout,parent,false);
                PostsViewHolder viewHolder = new PostsViewHolder(view);
                return viewHolder;
            }
        };
        postlist.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }
    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        TextView username,date,time,description,displayNoOfLike;
        CircleImageView user_profiel_image;;
        ImageView post_image;
        ImageButton likeButton,commentButton;
        int likeCounter;
        String currentUserId;
        DatabaseReference likesReference;


        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.post_user_name);
            date = itemView.findViewById(R.id.date_text);
            time = itemView.findViewById(R.id.time_text);
            description = itemView.findViewById(R.id.description_text_id);
            post_image = itemView.findViewById(R.id.post_image_id);
            user_profiel_image = itemView.findViewById(R.id.users_post_id);
            likeButton = itemView.findViewById(R.id.like_button_id);
            commentButton = itemView.findViewById(R.id.comment_button_id);
            displayNoOfLike = itemView.findViewById(R.id.display_numberof_like);

            likesReference = FirebaseDatabase.getInstance().getReference().child("likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

        public void setLikeButtonStatus(final String postKey){
            likesReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(postKey).hasChild(currentUserId)){
                        likeCounter = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeButton.setImageResource(R.drawable.like);
                        displayNoOfLike.setText(Integer.toString(likeCounter)+ "Likes");
                    }else {
                        likeCounter = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeButton.setImageResource(R.drawable.dislike);
                        displayNoOfLike.setText(Integer.toString(likeCounter));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void sendUserToTheAddNewPostActivity() {
        Intent addPostIntent = new Intent(MainActivity.this,AddNewPostActivity.class);
        startActivity(addPostIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {


      //  Toast.makeText(getApplicationContext(),"onStart() is called ",Toast.LENGTH_LONG).show();

        super.onStart();
        FirebaseUser current_user = mAuth.getCurrentUser();
       // Toast.makeText(getApplicationContext(),"The current user is : "+current_user,Toast.LENGTH_LONG).show();
        if(current_user  == null){
            sendToLoginActivity();
        }else if(current_user!=null){
           // Toast.makeText(getApplicationContext(),"i am here",Toast.LENGTH_LONG).show();
            checkUserExistance();
        }

    }

    public void checkUserExistance() {
        final String currentUserId = mAuth.getCurrentUser().getUid();
       // Toast.makeText(getApplicationContext(),"current user id is :" +currentUserId,Toast.LENGTH_LONG).show();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Toast.makeText(getApplicationContext(),"i am in to of if condition ",Toast.LENGTH_LONG).show();
                if(!dataSnapshot.hasChild(currentUserId)){
                    //Toast.makeText(getApplicationContext(),"I am in  data change method " ,Toast.LENGTH_LONG).show();
                   sendUserToTheSetUpActivitiy();
                   Toast.makeText(getApplicationContext(),"if",Toast.LENGTH_SHORT).show();

                }
                Toast.makeText(getApplicationContext(),"else",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToTheSetUpActivitiy() {
       // Toast.makeText(getApplicationContext(),"i am in setup ",Toast.LENGTH_LONG).show();
        Intent setupintent = new Intent(MainActivity.this,SetUpActivity.class);
        setupintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupintent);
        finish();
    }

    private void sendToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(loginIntent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }
     void navigationMenuItemSelector(int id){
        switch (id){

            case R.id.addnew_post_id:
                sendUserToTheAddNewPostActivity();
                break;
            case R.id.navigation_profile_id:
               Toast.makeText(getApplicationContext(),"Profile is selected",Toast.LENGTH_SHORT).show();
                break;
            case R.id.navigation_home_id:
                Toast.makeText(getApplicationContext(),"Home is selected",Toast.LENGTH_SHORT).show();
                break;
            case R.id.navigation_find_friends:
                Toast.makeText(getApplicationContext(),"Find Friend is selected",Toast.LENGTH_SHORT).show();
                break;
            case R.id.navigation_friendss:
                Toast.makeText(getApplicationContext(),"Friend is selected",Toast.LENGTH_SHORT).show();
                break;
            case R.id.navigation_settings:
                Toast.makeText(getApplicationContext(),"Setting is selected",Toast.LENGTH_SHORT).show();
                break;
            case R.id.navigation_message:
                Toast.makeText(getApplicationContext(),"Message is selected",Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout_id:
                mAuth.signOut();
                sendToLoginActivity();
                break;


        }


    }
}
