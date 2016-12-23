package com.followit.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class signInActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private static final String TAG = signInActivity.class.getSimpleName();
    private final int PASSWORD_MIN_LENGTH = 6;

    EditText email;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        password.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < PASSWORD_MIN_LENGTH)
                    findViewById(R.id.error_length_password).setVisibility(View.VISIBLE);
                else
                    findViewById(R.id.error_length_password).setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        auth = FirebaseAuth.getInstance();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    public void signIn(View view) {
        String email = this.email.getText().toString();
        String password = this.password.getText().toString();

        if (TextUtils.isEmpty(email))
            Toast.makeText(this, "You did not enter an email", Toast.LENGTH_SHORT).show();
        else if (TextUtils.isEmpty(password))
            Toast.makeText(this, "You did not enter a password", Toast.LENGTH_SHORT).show();
        else if (password.length() >= PASSWORD_MIN_LENGTH) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                    if (task.isSuccessful())
                        //getApplicationContext() because this refer to task
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    else {
                        Log.w(TAG, "signInWithEmail:failed", task.getException());
                        Toast.makeText(signInActivity.this, "echec login",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        FirebaseMessaging.getInstance().subscribeToTopic("graphUpdate");

    }

    public void signUp(View view) {
        Intent intent = new Intent(this, signUpActivity.class);
        startActivity(intent);
    }
}
