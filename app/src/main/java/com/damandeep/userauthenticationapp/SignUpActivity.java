package com.damandeep.userauthenticationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText etName, etEmail, etPassword, etMobileNumber;
    Button btnSignUp;
    TextView txtSignIn;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtSignIn = findViewById(R.id.txtSignIn);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressBar);

        if(mAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            finish();
        }

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                final String name = etName.getText().toString().trim();
                final String pNumber = etMobileNumber.getText().toString().trim();

                if(TextUtils.isEmpty(name)){
                    etName.setError("Name is Required.");
                    return;
                }

                if(TextUtils.isEmpty(email)){
                    etEmail.setError("Email is Required.");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    etPassword.setError("Password is Required.");
                    return;
                }

                if(password.length() < 6){
                    etPassword.setError("Password Must be >= 6 Characters");
                    return;
                }

                if(TextUtils.isEmpty(pNumber)){
                    etMobileNumber.setError("Mobile Number is Required.");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //register the user in firebase
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            //send verification link
                            FirebaseUser fUser = mAuth.getCurrentUser();
                            fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SignUpActivity.this, "Verification Email sent successfully.", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,"onFailure: Email not sent. "+ e.getMessage());
                                }
                            });

                            Toast.makeText(SignUpActivity.this, "User created.", Toast.LENGTH_SHORT).show();
                            userID = mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("name",name);
                            user.put("email",email);
                            user.put("phone",pNumber);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG,"onSuccess: User profile is created for "+ userID);
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        }else{
                            Toast.makeText(SignUpActivity.this, "Error! "+ task.getException(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signIn = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(signIn);
            }
        });
    }


}


