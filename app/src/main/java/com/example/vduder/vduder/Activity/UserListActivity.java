package com.example.vduder.vduder.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.vduder.vduder.Core.IdGenerator;
import com.example.vduder.vduder.Core.UserListInfo;
import com.example.vduder.vduder.Core.UserListViewAdapter;
import com.example.vduder.vduder.Model.Role;
import com.example.vduder.vduder.Model.User;
import com.example.vduder.vduder.R;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UserListActivity extends AppCompatActivity
{
    private DatabaseReference dataBase;

    private String myRole;
    private ListView userListView;
    UserListViewAdapter adapter;

    private ArrayList<User> allUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        myRole = getIntent().getStringExtra(Role.RoleIntentKey);

        userListView = (ListView) findViewById(R.id.userListView);

        allUsers = new ArrayList<>();
        dataBase = FirebaseDatabase.getInstance().getReference();

        InitUserLoading();
    }

    public void OnListViewItemButtonClicked(int i, String userId, String status)
    {
        switch (status)
        {
            case "send":
                SendOrder(userId, status);
                adapter.SetButtonAction(i, "wait", false);
                break;
            case "go":
                GoToInterview(userId);
                break;
        }
    }

    private void ActivateInterviewButton(String userId)
    {
        adapter.SetButtonAction(userId, "go", true);
    }

    private void GoToInterview(String userId) {
        Intent intent = new Intent(this, InterviewActivity.class);
        intent.putExtra(Role.RoleIntentKey, myRole);
        startActivity(intent);
    }

    private void SendOrder(String userId, String status)
	{
        Toast.makeText(this, IdGenerator.GenerateId(), Toast.LENGTH_SHORT).show();
    }

    private void InitUserLoading()
    {
        dataBase
                .child("role")
                .child(myRole)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<Role> roles = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        {
                            Role role = snapshot.getValue(Role.class);
                            roles.add(role);
                        }
                        InitUsers(roles);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        ShowDataBaseError("error role load");
                    }
                });
    }

    private void InitUsers(ArrayList<Role> roles)
    {
        for (int i = 0; i < roles.size(); ++i)
        {
            dataBase
                    .child("users")
                    .orderByChild("ID")
                    .equalTo(roles.get(i).userID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                User user = snapshot.getValue(User.class);
                                allUsers.add(user);
                                ReloadView();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            ShowDataBaseError("error user load");
                        }
                    });
        }
    }

    private void ReloadView()
    {
        ArrayList<UserListInfo> infos = new ArrayList<>(allUsers.size());
        for (int i = 0; i < allUsers.size(); ++i)
        {
            UserListInfo info = new UserListInfo();
            User user = allUsers.get(i);
            info.userId = user.ID;
            info.userName = user.username;
            info.status = "send";
            infos.add(info);
        }
        adapter = new UserListViewAdapter(this, infos);
        userListView.setAdapter(adapter);
    }

    private static void ShowDataBaseError(String message)
    {
        Log.e("Error", message);
    }

}