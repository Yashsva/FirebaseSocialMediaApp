package com.example.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Home extends AppCompatActivity implements AdapterView.OnItemClickListener {


    private FirebaseAuth mAuth;
    private ImageView imageViewPost;
    private Button btnCreatePost;
    private EditText edtDiscription;
    private ListView listViewUsers;
    private Bitmap bitmap;
    private StorageReference mStorageRef;
    private String imageIdentifier;

    private ArrayList<String> arrayList;
    private ArrayAdapter arrayAdapter;

    private ArrayList<String> listUID;
    private String imageDownloadLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth=FirebaseAuth.getInstance();
        mStorageRef= FirebaseStorage.getInstance().getReference();

        imageViewPost=findViewById(R.id.imageViewPost);
        btnCreatePost=findViewById(R.id.btnCreatePost);
        edtDiscription=findViewById(R.id.edtDescription);
        listViewUsers=findViewById(R.id.listViewUsers);

        arrayList=new ArrayList<>();
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,arrayList);

        listViewUsers.setAdapter(arrayAdapter);
        listViewUsers.setOnItemClickListener(this);

        listUID=new ArrayList<>();

        imageViewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectImage();

            }
        });

        btnCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadImageToServer();
            }
        });

    }

    private  void selectImage()
    {
        if(Build.VERSION.SDK_INT<23)
        {
            Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivity(intent);
        }
        else if(Build.VERSION.SDK_INT>=23)
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1000);
            }
            else
            {
                Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1000);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==1000 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            selectImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1000 && resultCode==RESULT_OK && data!=null)
        {
            Uri choosenImageData=data.getData();

            try {
                bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),choosenImageData);
                imageViewPost.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void uploadImageToServer()
    {

        if(bitmap!=null) {

            //Get the data from an imageView as bytes
            imageViewPost.setDrawingCacheEnabled(true);
            imageViewPost.buildDrawingCache();
            ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, BAOS);
            byte[] data = BAOS.toByteArray();

            imageIdentifier = UUID.randomUUID() + ".PNG";

            UploadTask uploadTask = mStorageRef.child("my_images").child(imageIdentifier).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(Home.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(Home.this, " Image Successfully Uploaded ", Toast.LENGTH_SHORT).show();

                    edtDiscription.setVisibility(View.VISIBLE);

                    FirebaseDatabase.getInstance().getReference().child("my_users").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            String username=dataSnapshot.child("username").getValue().toString();
                            if(!username.equals(mAuth.getCurrentUser().getDisplayName()))
                            {
                                listUID.add(dataSnapshot.getKey());
                                arrayList.add(username);
                                arrayAdapter.notifyDataSetChanged();
                            }

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            
                            if(task.isSuccessful())
                            {
                                imageDownloadLink=task.getResult().toString();
                            }
                            
                        }
                    });


                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.itemLogout)
        {
            mAuth.signOut();
            startActivity(new Intent(this,SignUp.class));
        }
        else if(item.getItemId()==R.id.itemViewPosts)
        {
            Intent intent=new Intent(Home.this,ViewPosts.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        HashMap<String,String> dataMap=new HashMap<>();
        dataMap.put("fromWhom",mAuth.getCurrentUser().getDisplayName());
        dataMap.put("imageName",imageIdentifier);
        dataMap.put("imageLink",imageDownloadLink);
        dataMap.put("description",edtDiscription.getText().toString());

        FirebaseDatabase.getInstance().getReference().child("my_users").child(listUID.get(position)).child("recieved_posts").push().setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(Home.this,"Data Sent ",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
