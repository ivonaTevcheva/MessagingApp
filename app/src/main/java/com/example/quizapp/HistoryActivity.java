package com.example.quizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.internal.NavigationMenuItemView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseReference lastRef;

    private TextView userEmail;
    private FirebaseAuth mAuth;
    private NavigationMenuItemView LogoutNav;
    private NavigationMenuItemView OverviewNav;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    TextView messages;
    ArrayList<String> values;
    ArrayList<String> prakjaci;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        values = new ArrayList<String>();
        prakjaci = new ArrayList<String>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getEmail();
        messages = findViewById(R.id.messagehistory);
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

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);

        lastRef.child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = user.getEmail();
                Integer proverka = 0;
                for(DataSnapshot snappy: snapshot.getChildren())
                {
                    String mailce = snappy.getKey() + ".com";
                    if (mailce.equals(email))
                    {
                        proverka = 1;
                        for(DataSnapshot snep: snappy.getChildren())
                        {
                            String porace = snep.getValue(String.class);
                            String prakjac = snep.getKey();
                            prakjaci.add(prakjac + ".com");
                            values.add(porace);
                        }
                        StringBuilder displayText = new StringBuilder();
                        // Iterate over the messages and senders simultaneously
                        for (int i = 0; i < values.size() && i < prakjaci.size(); i++) {
                            String sender = prakjaci.get(i);
                            String message = values.get(i);

                            // Concatenate the sender and message
                            String displayMessage = sender + ": " + message;

                            // Append the display message to the StringBuilder
                            displayText.append(displayMessage);

                            // Add a new line after each message
                            displayText.append("\n");
                        }

                        // Set the final display text in the TextView
                        messages.setText(displayText.toString());
                    }
                    if(proverka == 1)
                    {
                        break;
                    }
                    //values.add(porace);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistoryActivity.this, "HELLO" + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                startActivity(new Intent(HistoryActivity.this, LoginActivity.class));
            }
        });
        OverviewNav = findViewById(R.id.nav_useroverview);
        OverviewNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HistoryActivity.this, UserActivity.class));
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

}