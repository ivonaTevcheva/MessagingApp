package com.example.quizapp;

import static android.content.ContentValues.TAG;

import static com.google.android.material.color.utilities.MaterialDynamicColors.error;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.internal.NavigationMenuItemView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private DatabaseReference lastRef;
    private NavigationMenuItemView RegisterNav;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private Button mRegisterButton;
    private Integer flag;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    ProgressDialog progressDialog;
    public ArrayList<String> values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        flag = 0;
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        lastRef = FirebaseDatabase.getInstance("https://quizapp-4b486-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        values = new ArrayList<String>();

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEmailEditText = findViewById(R.id.email_hint);
        mPasswordEditText = findViewById(R.id.password_hint);
        mLoginButton = findViewById(R.id.login_button);
        mRegisterButton = findViewById(R.id.register_button);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                } else {
                    PerformLogin();
                }
            }
        });

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // override the onOptionsItemSelected()
    // function to implement
    // the item click listener callback
    // to open and close the navigation
    // drawer when the icon is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        RegisterNav = findViewById(R.id.nav_register);
        RegisterNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    private void PerformLogin() {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        String topic = email.split("@")[0];
        lastRef.child("emails").child(topic).setValue(email);

        if (!isEmailValid(email))
        {
            mEmailEditText.setError("Enter correct email.");
        }
        else if (password.isEmpty() || password.length()<6)
        {
            mPasswordEditText.setError("Enter correct password.");
        }
        else
        {
            progressDialog.setMessage("Logging in...");
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            String topicname = email.replace("@", "at");

            FirebaseMessaging.getInstance().subscribeToTopic(topicname)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "Subscribed to " + topicname;
                            if (!task.isSuccessful()) {
                                msg = "Subscribe failed";
                            }
                            Log.d(TAG, msg);
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        progressDialog.dismiss();
                        //FirebaseMessaging.getInstance().subscribeToTopic("all")
                              //  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                //    @Override
                                //    public void onComplete(@NonNull Task<Void> task) {
                                   //     String msg = "Subscribed";
                                  //      if (!task.isSuccessful()) {
                                   //         msg = "Subscribe failed";
                                   //     }
                                    //    Log.d(TAG, msg);
                                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                //    }
                              //  });
                        sendUserToNextActivity();
                        //    FirebaseMessaging.getInstance().subscribeToTopic("all")
                        //   .addOnCompleteListener(new OnCompleteListener<Void>() {
                        //   @Override
                        // public void onComplete(@NonNull Task<Void> task) {
                        //   String msg = "Subscribed";
                        //   if (!task.isSuccessful()) {
                        //      msg = "Subscribe failed";
                        //   }
                        //   Log.d(TAG, msg);
                        //   Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        //  }
                        //  });
                        Toast.makeText(LoginActivity.this, "Login is successful.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "" + task.getException() , Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void sendUserToNextActivity() {
        String email = mEmailEditText.getText().toString();
        {
            Intent intent = new Intent(LoginActivity.this, UserActivity.class);
            intent.putExtra("message", email);
            startActivity(intent);


        }
        }

    public static boolean isEmailValid(String email) {

        String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";
        CharSequence inputStr = email;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches())
            return true;
        else
            return false;
    }

    }
