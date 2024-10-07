package com.example.SpendingTracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.SpendingTracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private Button btnSignup;
    private TextView mSignin;

    private ProgressDialog mDialog;

    //Firebase setup

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth=FirebaseAuth.getInstance();

        mDialog=new ProgressDialog(this);

        signup();
    }

    private void  signup(){
        mEmail=findViewById(R.id.email_signup);
        mPassword=findViewById(R.id.password_signup);
        btnSignup=findViewById(R.id.button_signup);
        mSignin=findViewById(R.id.sign_in_here);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email=mEmail.getText().toString() .trim();
                String password=mPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Sorry, Email is required");
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    mPassword.setError("Sorry, Password is required");
                }

                mDialog.setMessage("Creating Account...");
                mDialog.show();


                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            mDialog.dismiss();

                            Toast.makeText(getApplicationContext(),"Sign Up Complete", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(getApplicationContext(),HomeActivity.class));


                        }else {
                            mDialog.dismiss();

                            Toast.makeText(getApplicationContext(), "Sign Up Unsucessful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        });

        mSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

    }
}