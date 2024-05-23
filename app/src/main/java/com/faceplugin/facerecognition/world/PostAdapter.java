package com.faceplugin.facerecognition.world;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.faceplugin.facerecognition.R;
import com.faceplugin.facerecognition.world.profile.Profile;
import com.makeramen.roundedimageview.RoundedImageView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {
    private List<PostList> postLists;
    private final Context context;

    public PostAdapter(List<PostList> postLists, Context context) {
        this.postLists = postLists;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_adapter_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PostList postList = postLists.get(position);

        if (postList.getProfileImage() != null) {
            Uri profileImageUri = Uri.parse(postList.getProfileImage());
            Picasso.get().load(profileImageUri).into(holder.profileImage);
        }

        holder.userName.setText(postList.getUserName());
        if(postList.getCaption() != null){
            holder.caption.setVisibility(View.VISIBLE);
            holder.caption.setText(postList.getCaption());
        }
        else{
            holder.caption.setVisibility(View.GONE);
        }
        Uri postUri = Uri.parse(postList.getPost());

        if (Objects.equals(postList.getType(), "video")) {
            holder.imagePost.setVisibility(View.GONE);
            holder.videoPost.setVideoURI(postUri);
            holder.videoPost.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {

                    holder.videoPost.start();
                }
            });
            holder.videoPost.start();
        } else {
            holder.videoPost.setVisibility(View.GONE);
            Picasso.get().load(postUri).into(holder.imagePost);

        }
        if(postList.getProfileVerified()){
         holder.verifiedMark.setVisibility(View.VISIBLE);

        }
        else{
           holder.verifiedMark.setVisibility(View.GONE);

        }

        holder.userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Profile.class);
                intent.putExtra("usersAuthUserId", postList.getAuthUserId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postLists.size();
    }

    public void updateData(List<PostList> postLists) {
        this.postLists = postLists;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage,verifiedMark;
        TextView userName, caption;
        VideoView videoPost;
        RoundedImageView imagePost;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            imagePost = itemView.findViewById(R.id.imagePost);
            videoPost = itemView.findViewById(R.id.videoPost);
            caption = itemView.findViewById(R.id.Caption);
            verifiedMark = itemView.findViewById(R.id.verifiedMark);
        }
    }
}
