package com.example.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    private EditText edtSignUpEmail,edtSignUpUsername,edtSignUpPassword;
    private Button btnSignUp,btnLoginPage;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth=FirebaseAuth.getInstance();

        setTitle("Sign Up");

        edtSignUpEmail=findViewById(R.id.edtEmailSignUp);
        edtSignUpPassword=findViewById(R.id.edtPasswordSignUp);
        edtSignUpUsername=findViewById(R.id.edtUsernameSignUp);

        btnSignUp=findViewById(R.id.btnSignUp);
        btnLoginPage=findViewById(R.id.btnLoginPage);

        btnSignUp.setOnClickListener(this);
        btnLoginPage.setOnClickListener(this);

        edtSignUpPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if(keyCode==KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN)
                {
                    onClick(btnSignUp);

                }

                return false;
            }
        });





    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser()!=null)
        {
//            ParseUser.logOut();

            Toast.makeText(this,mAuth.getCurrentUser().getDisplayName()+" Logged In",Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(SignUp.this,Home.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.btnSignUp:

                if(edtSignUpEmail.getText().toString().equals("")
                        || edtSignUpUsername.getText().toString().equals("")
                        || edtSignUpPassword.getText().toString().equals(""))
                {
                    Toast.makeText(this,"Please fill all fields",Toast.LENGTH_SHORT).show();

                }
                else
                {

                    String email=edtSignUpEmail.getText().toString();
                    String password=edtSignUpPassword.getText().toString();
                    Log.i("App Credential SignUp ", "\nSign Up\nEmail : " + email + "\nName : " + edtSignUpUsername.getText().toString() + "\n Password :" + password);

                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(SignUp.this,"Sign Up Successful",Toast.LENGTH_SHORT).show();

                                FirebaseDatabase.getInstance().getReference().child("my_users").child(task.getResult().getUser().getUid()).child("username").setValue(edtSignUpUsername.getText().toString());


                                UserProfileChangeRequest profileUpdates=new UserProfileChangeRequest.Builder()
                                        .setDisplayName(edtSignUpUsername.getText().toString()).build();

                                mAuth.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(SignUp.this,"Display name updated",Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });



                                TransitionToHomepage();
                            }
                            else
                            {
                                Toast.makeText(SignUp.this,"Error : "+task.getResult().toString(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }



                break;

            case R.id.btnLoginPage:

                Intent intent=new Intent(SignUp.this,LogIn.class);
                startActivity(intent);
                finish();

                break;
        }

    }


    public void ConstraintLayoutClicked(View view)
    {

        try {
            InputMethodManager inputMethodManager= (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void TransitionToHomepage()
    {
        Intent intent=new Intent(SignUp.this,Home.class);
        startActivity(intent);
        finish();
    }
}