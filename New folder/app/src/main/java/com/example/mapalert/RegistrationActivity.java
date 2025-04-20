package com.example.mapalert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    EditText signupName, signupEmail, signupUsername, signupPassword;
    TextView loginRedirectText;
    DatabaseReference usersRef, usernamesRef;
    FirebaseAuth auth;
    AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        loginRedirectText = findViewById(R.id.LoginRedirectText);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        usernamesRef = FirebaseDatabase.getInstance().getReference("usernames");

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String loggedInUser = prefs.getString("username", null);

        if (loggedInUser != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        findViewById(R.id.signup_button).setOnClickListener(v -> handleSignup());
        loginRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.main).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard();
                v.performClick();
                return true;
            }
            return false;
        });
    }

    private void handleSignup() {
        String name = signupName.getText().toString().trim();
        String email = signupEmail.getText().toString().trim();
        String username = signupUsername.getText().toString().trim();
        String password = signupPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingDialog();

        // Check if the username is already taken
        usernamesRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progressDialog.dismiss();
                    Toast.makeText(RegistrationActivity.this, "Username already taken", Toast.LENGTH_SHORT).show();
                } else {
                    createFirebaseUser(email, password, name, username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(RegistrationActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createFirebaseUser(String email, String password, String name, String username) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    String uid = user.getUid(); // Use UID instead of email as key
                    User userData = new User(name, email, username, password);

                    usersRef.child(uid).setValue(userData).addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful()) {
                            usernamesRef.child(username).setValue(email).addOnCompleteListener(usernameTask -> {
                                if (usernameTask.isSuccessful()) {
                                    saveLoginInfoAndProceed(username);
                                } else {
                                    progressDialog.dismiss();
                                    Log.e("FirebaseError", "Error saving username: " + usernameTask.getException());
                                    Toast.makeText(RegistrationActivity.this, "Error saving username", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Log.e("FirebaseError", "Error saving user data: " + userTask.getException());
                            Toast.makeText(RegistrationActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                progressDialog.dismiss();
                Log.e("FirebaseError", "Registration failed: " + task.getException());
                Toast.makeText(RegistrationActivity.this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void saveLoginInfoAndProceed(String username) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().putString("username", username).apply();
        progressDialog.dismiss();

        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View loadingView = inflater.inflate(R.layout.activity_dialog_loading, null);
        builder.setView(loadingView);
        builder.setCancelable(false);
        progressDialog = builder.create();
        progressDialog.show();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
