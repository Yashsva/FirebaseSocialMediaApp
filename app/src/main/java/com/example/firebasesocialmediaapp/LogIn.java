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

public class LogIn extends AppCompatActivity implements View.OnClickListener {

    private EditText edtLoginEmail,edtLoginPassword;
    private Button btnLogin,btnSignUpPage;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        setTitle("Log In");

        mAuth=FirebaseAuth.getInstance();

        edtLoginEmail=findViewById(R.id.edtEmailLogin);
        edtLoginPassword=findViewById(R.id.edtPasswordLogin);
        btnLogin=findViewById(R.id.btnLogin);
        btnSignUpPage=findViewById(R.id.btnSignUpPage);

        btnSignUpPage.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

        edtLoginPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if(keyCode==KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN)
                {
                    onClick(btnLogin);
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
            Intent intent=new Intent(LogIn.this,Home.class);
            startActivity(intent);
            finish();
        }


    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnLogin:

                if(edtLoginPassword.getText().toString().equals("") || edtLoginPassword.getText().toString().equals(""))
                {
                    Toast.makeText(this,"Please fill all fields",Toast.LENGTH_SHORT).show();

                }
                else
                {

                    String email=edtLoginEmail.getText().toString();
                    String password=edtLoginPassword.getText().toString();

                    Log.i("App Credential Login ", "\nLogin\nEmail : " + email + "\n Password :" + password);

                   //LogIn code to be implemented

                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(LogIn.this,"Login Successful",Toast.LENGTH_SHORT).show();
                                TransitionToHomepage();
                            }
                            else
                            {
                                Toast.makeText(LogIn.this,"Error : "+task.getResult(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });




                }


                break;

            case R.id.btnSignUpPage:

                Intent intent=new Intent(LogIn.this,SignUp.class);
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
        Intent intent=new Intent(LogIn.this,Home.class);
        startActivity(intent);
        finish();
    }

}
