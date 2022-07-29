package com.elbcalculator;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.elbcalculator.databinding.ActivityAddDataBinding;
import com.elbcalculator.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class add_data extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    ActivityAddDataBinding binding;

    private SharedPreferences file;

    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.numbers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(adapter);
        binding.spinner.setOnItemSelectedListener(this);

        file = getSharedPreferences("file", Activity.MODE_PRIVATE);

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(file.getString("Reading", "")=="1") {

                    String Energy_chagres = binding.energyCharges.getText().toString();
                    String fixed_charges = binding.fixedCharges.getText().toString();
                    String wheeling_charges = binding.wheelingCharges.getText().toString();

                    Db_data datas = new Db_data(Energy_chagres, fixed_charges, wheeling_charges);

                    reference = database.getReference("Charges");

                    reference.child("Charge 0-100").setValue(datas);

                } else if(file.getString("Reading", "")=="2") {

                    String Energy_chagres = binding.energyCharges.getText().toString();
                    String fixed_charges = binding.fixedCharges.getText().toString();
                    String wheeling_charges = binding.wheelingCharges.getText().toString();

                    Db_data datas = new Db_data(Energy_chagres, fixed_charges, wheeling_charges);

                    reference = database.getReference("Charges");

                    reference.child("Charge 100-300").setValue(datas);

                } else if(file.getString("Reading", "")=="3") {

                    String Energy_chagres = binding.energyCharges.getText().toString();
                    String fixed_charges = binding.fixedCharges.getText().toString();
                    String wheeling_charges = binding.wheelingCharges.getText().toString();

                    Db_data datas = new Db_data(Energy_chagres, fixed_charges, wheeling_charges);

                    reference = database.getReference("Charges");

                    reference.child("Charge 300-500").setValue(datas);


                } else if(file.getString("Reading", "")=="4") {

                    String Energy_chagres = binding.energyCharges.getText().toString();
                    String fixed_charges = binding.fixedCharges.getText().toString();
                    String wheeling_charges = binding.wheelingCharges.getText().toString();

                    Db_data datas = new Db_data(Energy_chagres, fixed_charges, wheeling_charges);

                    reference = database.getReference("Charges");

                    reference.child("Charge 500>").setValue(datas);

                }

                Toast.makeText(add_data.this, "Saved", Toast.LENGTH_SHORT).show();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.energyCharges.getWindowToken(), 0);
                binding.energyCharges.clearFocus();
                binding.wheelingCharges.clearFocus();
                binding.fixedCharges.clearFocus();
            }
        });

    }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedClass = parent.getItemAtPosition(position).toString();

            database = FirebaseDatabase.getInstance();
            reference = database.getReference("Charges");

            switch (selectedClass)
            {
                case "Reading 0-100":

                    reference.child("Charge 0-100").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for(DataSnapshot snapshot1: snapshot.getChildren()) {
                                if (snapshot.exists()) {

                                    String ec = snapshot.child("energy_charges").getValue(String.class);
                                    String fc = snapshot.child("fixed_charges").getValue(String.class);
                                    String wc = snapshot.child("wheeling_charges").getValue(String.class);

                                    binding.energyCharges.setText(ec);
                                    binding.fixedCharges.setText(fc);
                                    binding.wheelingCharges.setText(wc);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });

                    file.edit().putString("Reading", "1").commit();

                    break;

                case "Reading 100-300":

                    reference.child("Charge 100-300").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for(DataSnapshot snapshot1: snapshot.getChildren()) {
                                if (snapshot.exists()) {

                                    String ec = snapshot.child("energy_charges").getValue(String.class);
                                    String fc = snapshot.child("fixed_charges").getValue(String.class);
                                    String wc = snapshot.child("wheeling_charges").getValue(String.class);

                                    binding.energyCharges.setText(ec);
                                    binding.fixedCharges.setText(fc);
                                    binding.wheelingCharges.setText(wc);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });

                    file.edit().putString("Reading", "2").commit();

                    break;

                case "Reading 300-500":

                    reference.child("Charge 300-500").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for(DataSnapshot snapshot1: snapshot.getChildren()) {
                                if (snapshot.exists()) {

                                    String ec = snapshot.child("energy_charges").getValue(String.class);
                                    String fc = snapshot.child("fixed_charges").getValue(String.class);
                                    String wc = snapshot.child("wheeling_charges").getValue(String.class);

                                    binding.energyCharges.setText(ec);
                                    binding.fixedCharges.setText(fc);
                                    binding.wheelingCharges.setText(wc);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });

                    file.edit().putString("Reading", "3").commit();

                    break;

                case "Reading 500>":

                    reference.child("Charge 500>").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for(DataSnapshot snapshot1: snapshot.getChildren()) {
                                if (snapshot.exists()) {

                                    String ec = snapshot.child("energy_charges").getValue(String.class);
                                    String fc = snapshot.child("fixed_charges").getValue(String.class);
                                    String wc = snapshot.child("wheeling_charges").getValue(String.class);

                                    binding.energyCharges.setText(ec);
                                    binding.fixedCharges.setText(fc);
                                    binding.wheelingCharges.setText(wc);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });

                    file.edit().putString("Reading", "4").commit();

                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }