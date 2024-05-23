package com.faceplugin.facerecognition.world;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.faceplugin.facerecognition.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Home extends Fragment {

    private RecyclerView postRecyclerView;
    private PostAdapter postAdapter;
    private final List<PostList> postLists = new ArrayList<>();

    public Home() {
        // Required empty public constructor
    }

    public static Home newInstance() {
        return new Home();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        postRecyclerView = view.findViewById(R.id.postRecyclerView);
        postRecyclerView.setHasFixedSize(true);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postAdapter = new PostAdapter(postLists, getContext());
        postRecyclerView.setAdapter(postAdapter);

        ProgressDialog pd = new ProgressDialog(getContext());
        pd.setCancelable(false);
        pd.setMessage("Please wait...");
        pd.show();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        String AuthUserId = user.getUid();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        databaseReference.child("Posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String postKey = snapshot1.getKey();
                    if (postKey != null) {
                        String UserAuthUserId = snapshot1.child("authUserId").getValue(String.class);

                                        assert UserAuthUserId != null;

                                            String Post = snapshot1.child("post").getValue(String.class);
                                            String Caption = snapshot1.child("caption").getValue(String.class);
                                            String Type = snapshot1.child("type").getValue(String.class);
                                            databaseReference.child("Users").child(UserAuthUserId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            String UserName = snapshot.child("userName").getValue(String.class);
                                                            String ProfileImage = snapshot.child("profileImage").getValue(String.class);
                                                            String Gender = snapshot.child("gender").getValue(String.class);
                                                            boolean profileVerified = snapshot.child("profileVerified").getValue(Boolean.class);

                                                           PostList postList = new PostList(ProfileImage, UserName, Post, Type, UserAuthUserId, Gender, Caption, profileVerified);
                                                           postLists.add(postList);
                                                           postAdapter.notifyDataSetChanged();
                                                            pd.dismiss();
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            pd.dismiss();
                                                        }
                                                    });
                    }
                }
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
            }
        });

        return view;
    }
}
