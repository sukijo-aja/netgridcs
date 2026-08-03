package com.androidstarter.app.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.androidstarter.app.R;
import com.androidstarter.app.utils.AppPreference;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.androidstarter.app.data.model.DeviceTokenRequest;
import com.androidstarter.app.data.remote.services.DeviceApiService;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private ProgressBar progressBar;
    private Button btnGoogleSignIn;
    
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLoginEmail;
    private TextView tvRegister;
    private boolean isLoginMode = true;
    
    // NOTE: In a real app, use the actual Web Client ID from google-services.json
    // We will attempt to use default_web_client_id if available, or fallback.
    private String serverClientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        progressBar = findViewById(R.id.progressBar);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLoginEmail = findViewById(R.id.btnLoginEmail);
        tvRegister = findViewById(R.id.tvRegister);
        
        // Attempt to fetch default web client id from string resources
        int stringId = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
        if (stringId != 0) {
            serverClientId = getString(stringId);
        } else {
            // Fallback (this will probably not work unless you have a real one, but it handles the build)
            serverClientId = "YOUR_WEB_CLIENT_ID";
        }

        btnGoogleSignIn.setOnClickListener(v -> handleGoogleSignIn());
        
        tvRegister.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            if (isLoginMode) {
                btnLoginEmail.setText("Sign In");
                tvRegister.setText("Don't have an account? Register");
            } else {
                btnLoginEmail.setText("Register");
                tvRegister.setText("Already have an account? Sign In");
            }
        });
        
        btnLoginEmail.setOnClickListener(v -> handleEmailPasswordAuth());
    }

    private void handleEmailPasswordAuth() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setLoading(true);
        if (isLoginMode) {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        handleAuthSuccess();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                });
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        handleAuthSuccess();
                    } else {
                        Toast.makeText(LoginActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                });
        }
    }
    
    private void handleAuthSuccess() {
        Log.d("LoginActivity", "Auth:success");
        AppPreference appPreference = new AppPreference(LoginActivity.this);
        String uid = "";
        if (mAuth.getCurrentUser() != null) {
            uid = mAuth.getCurrentUser().getUid();
            appPreference.saveString("UID", uid);
            appPreference.saveString("USER_EMAIL", mAuth.getCurrentUser().getEmail());
            String name = mAuth.getCurrentUser().getDisplayName();
            appPreference.saveString("USER_NAME", name != null ? name : "User");
        }
        
        // Hapus status token terakhir agar aplikasi terpaksa melakukan sinkronisasi ulang
        appPreference.remove("last_synced_token");
        
        // Mulai sinkronisasi token di background
        com.androidstarter.app.utils.TokenSyncManager.syncTokenIfNeeded(LoginActivity.this);
        
        Toast.makeText(LoginActivity.this, "Signed in successfully", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void handleGoogleSignIn() {
        if (serverClientId.equals("YOUR_WEB_CLIENT_ID")) {
            Toast.makeText(this, "Web Client ID is not configured properly in google-services.json", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // Use cached thread pool for credential manager callback if possible, or main executor
        // Usually, CredentialManager expects a java.util.concurrent.Executor
        Executor executor = androidx.core.content.ContextCompat.getMainExecutor(this);

        credentialManager.getCredentialAsync(this, request, null, executor,
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignInResult(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e("LoginActivity", "GetCredentialException", e);
                        Toast.makeText(LoginActivity.this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        setLoading(false);
                    }
                });
    }

    private void handleSignInResult(GetCredentialResponse result) {
        try {
            androidx.credentials.Credential credential = result.getCredential();
            if (credential instanceof androidx.credentials.CustomCredential && 
                credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                String idToken = googleIdTokenCredential.getIdToken();
                firebaseAuthWithGoogle(idToken);
            } else {
                Log.e("LoginActivity", "Unexpected credential type");
                Toast.makeText(this, "Sign-in error", Toast.LENGTH_SHORT).show();
                setLoading(false);
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "Error handling sign in result", e);
            Toast.makeText(this, "Sign-in error", Toast.LENGTH_SHORT).show();
            setLoading(false);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d("LoginActivity", "signInWithCredential:success");
                        
                        AppPreference appPreference = new AppPreference(LoginActivity.this);
                        if (mAuth.getCurrentUser() != null) {
                            appPreference.saveString("UID", mAuth.getCurrentUser().getUid());
                            appPreference.saveString("USER_EMAIL", mAuth.getCurrentUser().getEmail());
                            appPreference.saveString("USER_NAME", mAuth.getCurrentUser().getDisplayName());
                        }

                        Toast.makeText(LoginActivity.this, "Signed in as " + mAuth.getCurrentUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                        handleAuthSuccess();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LoginActivity", "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        setLoading(false);
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnGoogleSignIn.setEnabled(!isLoading);
        btnLoginEmail.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        tvRegister.setEnabled(!isLoading);
    }
}
