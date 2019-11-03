package com.example.mooood;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

import static com.google.firebase.firestore.FieldValue.delete;

public class UserFeedActivity extends AppCompatActivity{

    private static final String TAG = "For Testing";
    public static final String DOC_ID = "Doc Id";

    //Declare the variables for reference later
    SwipeMenuListView postList;
    ArrayAdapter<MoodEvent> postAdapter;
    ArrayList<MoodEvent> postDataList;

    //Firebase setup!
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("MoodEvents");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);

        //basic ArrayAdapter init
        postDataList = new ArrayList<>();
        postList = findViewById(R.id.posts_list);
        postAdapter = new MoodEventsAdapter(postDataList, this);
        postList.setAdapter(postAdapter);

        //adding delete button to SwipeMenu
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                //init delete button
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());

                //custom design for delete button
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                deleteItem.setWidth(200);
                deleteItem.setIcon(R.drawable.ic_delete_forever);

                //add button
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        postList.setMenuCreator(creator);

        //click listener for garbage can icon
        postList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index){
                    case 0:
                        deleteMoodEventFromDB(collectionReference, position);
                        break;
                }
                return false;
            }
        });

        //go to CreatePostActivity
        final FloatingActionButton createPostBtn = findViewById(R.id.fab);
        createPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), CreateEventActivity.class);
                startActivity(intent);

            }
        });

//        //click listener for each item -> ShowEventActivity
//        postList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//                Intent intent = new Intent(getApplicationContext(), ShowEventActivity.class);
//                intent.putExtra(DOC_ID, postDataList.get(i).getDocumentId());
//                startActivity(intent);
//            }
//        });

    } //end of onCreate

    @Override
    protected void onStart(){
        super.onStart();
        collectionReference.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                postDataList.clear();

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){

                    String date = documentSnapshot.getData().get("date").toString();
                    String time = documentSnapshot.getData().get("time").toString();
                    String emotionalState = documentSnapshot.getData().get("emotionalState").toString();
                    String imageURl = "";
                    String reason = documentSnapshot.getData().get("reason").toString();
                    String socialSituation = documentSnapshot.getData().get("socialSituation").toString();

                    MoodEvent moodEvent = new MoodEvent(date, time, emotionalState, imageURl, reason, socialSituation);
                    moodEvent.setDocumentId(documentSnapshot.getId());

                    postDataList.add(moodEvent); //add to datalist
                }

                postAdapter.notifyDataSetChanged();
            }
        });
    }

    //delete from database
    public void deleteMoodEventFromDB(CollectionReference collectionReference, int position){
        collectionReference
                .document(postDataList.get(position).getDocumentId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "City and Province was successfully deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "City and Province were not deleted", e);
                    }
                });

        postDataList.remove(position);
        postAdapter.notifyDataSetChanged();
    }

}