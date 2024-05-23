package com.faceplugin.facerecognition.world;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.faceplugin.facerecognition.R;
import com.faceplugin.facerecognition.world.upload.ImagePostUpload;
import com.faceplugin.facerecognition.world.upload.VideoPostUpload;
import com.squareup.picasso.Picasso;

import me.virtualiz.blurshadowimageview.BlurShadowImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Upload#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Upload extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    BlurShadowImageView post;
    ActivityResultLauncher<String> mGetContent;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Upload() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Upload.
     */
    // TODO: Rename and change types and number of parameters
    public static Upload newInstance(String param1, String param2) {
        Upload fragment = new Upload();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upload, container, false);
        post = view.findViewById(R.id.post);
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/video/*");
            }
        });
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    // Do something with the selected image URI
                    // For example, load the image into ImageView
                    String File = String.valueOf(result);

                    if (File.contains("video")) {
                        Intent i = new Intent(view.getContext(), VideoPostUpload.class);
                        i.putExtra("File", File);
                        startActivity(i);
                    } else {
                        Intent intent = new Intent(view.getContext(), ImagePostUpload.class);
                        intent.putExtra("File", File);
                        startActivity(intent);
                    }
                }
            }
        });
        return view;
    }
}
