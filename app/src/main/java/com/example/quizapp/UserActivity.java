package com.example.quizapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.internal.NavigationMenuItemView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserActivity extends AppCompatActivity {
    private DatabaseReference lastRef;
    private FirebaseAuth mAuth;

    private Button Send;

    private EditText Message;

    private NavigationMenuItemView LogoutNav;
    private NavigationMenuItemView HistoryNav;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    List<String> values;

    private Spinner userSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mAuth = FirebaseAuth.getInstance();
        lastRef = FirebaseDatabase.getInstance("https://quizapp-4b486-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout_admin);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        values = new ArrayList<String>();
        userSpinner = findViewById(R.id.userSpinner);
        Send = findViewById(R.id.submitButton);
        Message = findViewById(R.id.MessageToSend);

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);

        lastRef.child("emails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snappy: snapshot.getChildren())
                {
                    String mailce = snappy.getValue(String.class);
                    values.add(mailce);
                }
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email = user.getEmail();
                values.remove(email);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(UserActivity.this,
                        android.R.layout.simple_spinner_item, values);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                userSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserActivity.this, "HELLO" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });


        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String poraka = Message.getText().toString();
                String dokogo = userSpinner.getSelectedItem().toString().split("\\.")[0];
                String dokogo1 = userSpinner.getSelectedItem().toString();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email = user.getEmail();
                String odkogo = email.split("\\.")[0];
                lastRef.child("messages").child(dokogo).child(odkogo).setValue(poraka);
                sendNotification(dokogo1, email, poraka);

                Message.setText("");
                Toast.makeText(UserActivity.this, "Message successfuly sent!", Toast.LENGTH_SHORT).show();
            }
        });



    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        LogoutNav = findViewById(R.id.nav_logoutuser);
        LogoutNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(UserActivity.this, LoginActivity.class));
            }
        });
        HistoryNav = findViewById(R.id.nav_history);
        HistoryNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserActivity.this, HistoryActivity.class));
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

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();

        } else {
            // User is not signed in, redirect to login or another activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void sendNotification(String dokogo, String odkogo, String poraka) {

        String topicname = dokogo.replace("@", "at");
        String msg = "Checking at " + topicname;
        Log.d(TAG, msg);
        // Create a new message
        JSONObject message = new JSONObject();
        try {
            message.put("to", "/topics/" + topicname);
            message.put("data", new JSONObject().put("message", odkogo + " says: " + poraka));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send the message using the FCM API
        new SendMessageTask().execute(message);
    }

    private class SendMessageTask extends AsyncTask<JSONObject, Void, String> {
        @Override
        protected String doInBackground(JSONObject... params) {
            try {
                // Send the message to the server
                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "key=AAAAHWNuVXE:APA91bEgzTCVPRWzgHw0U9AtCzcHiazGZgjx25s4x82cXxz27P3H0BmKLQxZgRaFvFOK2IMgsK3jHkkSGWE-e4yYa0mHCqgzabOk8k3xnKkSTYk_IB1wdwG37DEwy_6oAIZ4YM9KIHnl");
                connection.setDoOutput(true);

                // Write the message to the request body
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(params[0].toString().getBytes());
                outputStream.flush();
                outputStream.close();

                // Read the response from the server
                InputStream inputStream = connection.getInputStream();
                String response = convertStreamToString(inputStream);
                inputStream.close();

                return response;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            // Handle the response from the server
            Log.d(TAG, "Response: " + response);
        }
    }

    private String convertStreamToString(InputStream inputStream) {
        // This method reads the response from the server and converts it to a string
        // You can customize this method to handle the response in any way you want
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

}