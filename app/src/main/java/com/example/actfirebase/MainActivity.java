package com.example.actfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.actfirebase.adapter.UserAdapter;
import com.example.actfirebase.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity<FirebaseFirestore> extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<User> list = new ArrayList<>();
    private UserAdapter userAdapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        recyclerView = findViewById(R.id.recycler_view);
        btnAdd = findViewById(R.id.btn_add);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Take the Data...");
        userAdapter = new UserAdapter(getApplicationContext(), list);

        userAdapter.setDialog(new UserAdapter.Dialog()
        {
            @Override
            public void onClick(int pos)
            {
                final CharSequence[] dialogItem = {"Edit", "Delete"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setItems(dialogItem, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        switch (i)
                        {
                            case 0:
                                Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
                                intent.putExtra("id", list.get(pos).getId());
                                intent.putExtra("name", list.get(pos).getName());
                                intent.putExtra("email", list.get(pos).getEmail());
                                startActivity(intent);
                                break;
                            case 1:
                                deleteData(list.get(pos).getId());
                                break;
                        }
                    }
                });
                dialog.show();
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,false);
        RecyclerView.ItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(userAdapter);

        btnAdd.setOnClickListener(v ->
        {
            startActivity(new Intent(getApplicationContext(), EditorActivity.class));
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        getData();
    }

    private <QuerySnapshot> void getData()
    {
        progressDialog.show();

        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                list.clear();
                if(task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult())
                    {
                        User user = new User(document.getString("name"), document.getString("email"));
                        user.setId(document.getID());
                        list.add(user);
                    }
                    userAdapter.notifyDataSetChanged();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Failed to take Data!", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });
    }


    private void deleteData(String id)
    {
        progressDialog.show();
        db.collection("users").document(id).delete().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(!task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(), "Failed to Delete Data!", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
                getData();
            }
        });
    }
}