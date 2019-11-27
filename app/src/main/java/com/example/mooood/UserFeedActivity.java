package com.example.mooood;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * FILE PURPOSE: This is for displaying all of User's MoodEvents
 */

public class UserFeedActivity extends AppCompatActivity {

    private static final String TAG = "For Testing";
    public static final String MOOD_EVENT = "Mood Event";

    //Declare the variables for reference later
    RecyclerView postList;
    ArrayList<MoodEvent> postDataList;

    private MoodEventsAdapter postAdapter;
    private RecyclerTouchListener recyclerTouchListener;

    SearchView userSearchView;
    Button feedButton;
    ImageButton mapButton;
    Date moodTimeStamp; //what is this for?

    //Firebase setup!
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;
    private CollectionReference collectionReference;
    private String textSubmitted;

    /**
     * This implements all methods below accordingly
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);

        Intent intent = getIntent();
        final String accountName = intent.getStringExtra("accountKey");
        documentReference = db.collection("MoodEvents").document(accountName);
        collectionReference = db.collection("MoodEvents").document(accountName).collection("MoodActivities");

        createPostBtnClickListener(accountName);

        //recycler view setup
        moodEventAdapterSetup();
        setRecyclerTouchListener();

        //maaz's filter implementation
        filterMood();
        selectFeed(accountName);

        //Max's map implementation
        openMoodMap();


    } //end of onCreate

    /**
     * This will collect and show all of User's MoodEvent in DB
     */
    @Override
    protected void onStart() {
        super.onStart();
        documentReference.collection("MoodActivities")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        postDataList.clear();

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                                String author = (String)documentSnapshot.getData().get("author");
                                String date = (String)documentSnapshot.getData().get("date");
                                String time = (String)documentSnapshot.getData().get("time");
                                String emotionalState = (String)documentSnapshot.getData().get("emotionalState");
                                String imageURl = (String)documentSnapshot.getData().get("imageUrl");
                                String reason = (String)documentSnapshot.getData().get("reason");
                                String socialSituation = (String)documentSnapshot.getData().get("socialSituation");
                                String latitude = (String) documentSnapshot.getData().get("latitude");
                                String longitude= (String) documentSnapshot.getData().get("longitude");
                                String address = (String) documentSnapshot.getData().get("address") ;
                                MoodEvent moodEvent = new MoodEvent(author, date, time, emotionalState, imageURl, reason, socialSituation,latitude,longitude,address);
                                moodEvent.setDocumentId(documentSnapshot.getId());

                                postDataList.add(moodEvent); //add to data list

                        }

                        postAdapter.notifyDataSetChanged();
                    }

                });
    }

    @Override
    public void onResume() {
        super.onResume();
        postList.addOnItemTouchListener(recyclerTouchListener);
    }

    /**
     * This set ups Recycler Touch Listener
     */
    private void setRecyclerTouchListener(){
        recyclerTouchListener = new RecyclerTouchListener(this, postList);
        recyclerTouchListener.setClickable(new RecyclerTouchListener.OnRowClickListener() {
            @Override
            public void onRowClicked(int position) {
                showEventClickListener(position);
            }

            @Override
            public void onIndependentViewClicked(int independentViewID, int position) {

            }

        }).setSwipeOptionViews(R.id.delete_btn)
                .setSwipeable(R.id.mood_event, R.id.menu, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
                    @Override
                    public void onSwipeOptionClicked(int viewID, int position) {
                        if(viewID == R.id.delete_btn){
                            deleteMoodEventFromDB(documentReference, position);
                        }
                    }
                });

        //create a line that separates all MoodEvent inside the RecyclerView
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(postList.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(this.getResources().getDrawable(R.drawable.recyclerview_divider));
        postList.addItemDecoration(dividerItemDecoration);
    }

    /**
     * This set ups the array adapter for RecyclerView
     */
    private void moodEventAdapterSetup(){
        postDataList = new ArrayList<>();
        postList = findViewById(R.id.posts_list);
        postAdapter = new MoodEventsAdapter(postDataList);

        postList.setLayoutManager(new LinearLayoutManager(this));
        postList.setAdapter(postAdapter);
    }

    /**
     * This deletes a MoodEvent from the DB
     * @param documentReference
     *     This is documentReference of MoodEvent in DB
     * @param position
     *     This is the position of MoodEvent in postDataList
     */
    private void deleteMoodEventFromDB(DocumentReference documentReference, int position) {
        documentReference.collection("MoodActivities")
                .document(postDataList.get(position).getDocumentId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Mood was successfully deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Mood was not deleted", e);
                    }
                });

        postDataList.remove(position);
        postAdapter.notifyDataSetChanged();
    }

    /**
     * This is a click listener for create post. Will redirect to CreateEventActivity
     */
    private void createPostBtnClickListener ( final String accountName){
        final FloatingActionButton createPostBtn = findViewById(R.id.fab);
        createPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), CreateEventActivity.class);
                intent.putExtra("key", accountName);
                startActivity(intent);
            }
        });
    }

    /**
     * This is SearchView that will filter through adapter for the Mood entered and display it
     */
    private void filterMood () {
            userSearchView = findViewById(R.id.userSearchView);

            userSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    textSubmitted=s.toUpperCase();
                    collectionReference
                            .orderBy("timeStamp", Query.Direction.DESCENDING)
                            .whereEqualTo("emotionalState", textSubmitted)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    postDataList.clear();
                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {

                                        String author = (String)documentSnapshot.getData().get("author");
                                        String date = (String)documentSnapshot.getData().get("date");
                                        String time = (String)documentSnapshot.getData().get("time");
                                        String emotionalState = (String)documentSnapshot.getData().get("emotionalState");
                                        String imageURl = (String)documentSnapshot.getData().get("imageUrl");
                                        String reason = (String)documentSnapshot.getData().get("reason");
                                        String socialSituation = (String)documentSnapshot.getData().get("socialSituation");
                                        String latitude = (String) documentSnapshot.getData().get("latitude");
                                        String longitude= (String) documentSnapshot.getData().get("longitude");
                                        String address = (String) documentSnapshot.getData().get("address") ;

                                        MoodEvent moodEvent = new MoodEvent(author, date, time, emotionalState, imageURl, reason, socialSituation, latitude,longitude, address);
                                        moodEvent.setDocumentId(documentSnapshot.getId());

                                        postDataList.add(moodEvent); //add to data list
                                    }
                                    postAdapter.notifyDataSetChanged();
                            }
                        });
                    return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }

        });

        userSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                onStart();
                return false;
            }
        });

    }

    private void selectFeed(final String accountName) {
        feedButton = findViewById(R.id.feedButton);

        feedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                db.collection("participant").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            final String participant = doc.getId();
                            Log.d("display", participant);
                            db.collection("MoodEvents").document(participant).collection("MoodActivities")
                                    .orderBy("timeStamp", Query.Direction.DESCENDING)
                                    .limit(1)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd yyyy h:mm a");

                                                String author = (String) documentSnapshot.getData().get("author");
                                                String date = (String) documentSnapshot.getData().get("date");
                                                String time = (String) documentSnapshot.getData().get("time");
                                                String emotionalState = (String) documentSnapshot.getData().get("emotionalState");
                                                String imageURl = (String) documentSnapshot.getData().get("imageUrl");
                                                String reason = (String) documentSnapshot.getData().get("reason");
                                                String socialSituation = (String) documentSnapshot.getData().get("socialSituation");
                                                String latitude = (String) documentSnapshot.getData().get("latitude");
                                                String longitude = (String) documentSnapshot.getData().get("longitude");
                                                String address = (String) documentSnapshot.getData().get("address");

                                                try {
                                                    moodTimeStamp = simpleDateFormat.parse(date + ' ' + time);
                                                    Log.d("Time1", "changing timestamp in Oncreate");
                                                } catch (ParseException e) {
                                                    Log.d("Time1", "catch exception in Oncreate");
                                                    e.printStackTrace();
                                                }
                                                final MoodEvent moodEvent = new MoodEvent(author, date, time, emotionalState, imageURl, reason, socialSituation, latitude, longitude, address);
                                                moodEvent.setDocumentId(documentSnapshot.getId());
                                                moodEvent.setTimeStamp(moodTimeStamp);

                                                db.collection("Users").document(participant).set(moodEvent);
                                                Log.d(TAG, "ADDED to database");

                                                //This will update the following list of the user
                                                //updateFollowingList(name, participant, moodEvent);
                                            }
                                        }
                                    });
                        }
                    }
                });

                Intent intent = new Intent(UserFeedActivity.this, feedActivity.class);
                intent.putExtra("account", accountName);
                startActivity(intent);
            }
        });
    }

    /**
     * This is a click listener for each MoodEvent that goes to ShowEventActivity
     */
    public void showEventClickListener(int position) {
        Intent intent = new Intent(UserFeedActivity.this, ShowEventActivity.class);
        intent.putExtra(MOOD_EVENT, postDataList.get(position));
        startActivity(intent);
    }

    //TODO: Fix bug where the app crashes when the UserFeedMap activity is started.
    private void openMoodMap(){
        mapButton=findViewById(R.id.map_user_feed_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserFeedActivity.this, UserFeedMapActivity.class);
                if(postDataList.size()>0){
                    try{
                        intent.putParcelableArrayListExtra("userMoodList",postDataList);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(UserFeedActivity.this,"You don't have any posts!",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}