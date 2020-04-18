package com.example.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPosts extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private FirebaseAuth firebaseAuth;
    private ListView listViewPosts;
    private ArrayList<String> arrayListUsernames;
    private ArrayAdapter arrayAdapter;

    private ImageView imageViewPosts;
    private TextView txtDescription;


    private ArrayList<DataSnapshot> dataSnapshotArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        firebaseAuth=FirebaseAuth.getInstance();

        listViewPosts=findViewById(R.id.listViewPosts);
        arrayListUsernames=new ArrayList<>();
        arrayAdapter=new ArrayAdapter(ViewPosts.this,android.R.layout.simple_list_item_1,arrayListUsernames);
        listViewPosts.setAdapter(arrayAdapter);

        dataSnapshotArrayList=new ArrayList<>();

        imageViewPosts=findViewById(R.id.imageViewPost);
        txtDescription=findViewById(R.id.txtDescription);

        listViewPosts.setOnItemClickListener(this);
        listViewPosts.setOnItemLongClickListener(this);

        FirebaseDatabase.getInstance().getReference().child("my_users").child(firebaseAuth.getCurrentUser().getUid()).child("recieved_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                dataSnapshotArrayList.add(dataSnapshot);
                String fromWhomUsername=dataSnapshot.child("fromWhom").getValue().toString();
                arrayListUsernames.add(fromWhomUsername);
                arrayAdapter.notifyDataSetChanged();


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                int i=0;
                for(DataSnapshot snapshot : dataSnapshotArrayList)
                {
                    if(snapshot.getKey().equals(dataSnapshot.getKey()))
                    {
                        dataSnapshotArrayList.remove(i);
                        arrayListUsernames.remove(i);
                    }
                    i++;
                }

                arrayAdapter.notifyDataSetChanged();
                imageViewPosts.setImageResource(R.drawable.camera);
                txtDescription.setText("");

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        DataSnapshot myDataSnapshot=dataSnapshotArrayList.get(position);
        String downloadLink=myDataSnapshot.child("imageLink").getValue().toString();

        Picasso.get().load(downloadLink).into(imageViewPosts);
        txtDescription.setText(myDataSnapshot.child("description").getValue().toString());




    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        AlertDialog.Builder builder;
        if(Build.VERSION.SDK_INT>=23)
        {
            builder=new AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog_Alert);

        }
        else
        {
            builder=new AlertDialog.Builder(this);
        }

        builder.setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this entry")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        //Deleting the image from firebase storage
                        FirebaseStorage.getInstance().getReference()
                                .child("my_images").child((String) dataSnapshotArrayList.get(position).child("imageName").getValue())
                                .delete();


                        //Deleting the Post from firebase database
                        FirebaseDatabase.getInstance().getReference()
                                .child("my_users").child(firebaseAuth.getCurrentUser().getUid())
                                .child("recieved_posts")
                                .child(dataSnapshotArrayList.get(position).getKey()).removeValue();


                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();


        return false;
    }
}
