package com.shihab.cryptchat;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.shihab.cryptchat.model.UserModel;
import com.shihab.cryptchat.utils.AndroidUtil;
import com.shihab.cryptchat.utils.FirebaseUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("userId")) {
            String userId = extras.getString("userId");

            if (userId != null && !userId.isEmpty()) {
                FirebaseUtil.allUserCollectionReference().document(userId).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                UserModel model = task.getResult().toObject(UserModel.class);

                                Intent mainIntent = new Intent(this, MainActivity.class);
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(mainIntent);

                                Intent intent = new Intent(this, ChatActivity.class);
                                if (model != null) {
                                    AndroidUtil.passUserModelAsIntent(intent, model);
                                }
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                // fallback if document not found or error
                                goToMainOrLogin();
                            }
                        });
            } else {
                goToMainOrLogin(); // fallback if userId is null or empty
            }
        } else {
            new Handler().postDelayed(this::goToMainOrLogin, 1000);
        }
    }

    private void goToMainOrLogin() {
        if (FirebaseUtil.isLoggedIn()) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, LoginPhoneNumberActivity.class));
        }
        finish();
    }

}