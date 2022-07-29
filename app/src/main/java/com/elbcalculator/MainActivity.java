package com.elbcalculator;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.elbcalculator.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    int reading_100 = 100, reading_200 = 200;

    ProgressDialog dialog;

    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(MainActivity.this);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser User = auth.getCurrentUser();
        if (User == null) {

            auth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInAnonymously:success");
                                FirebaseUser User = auth.getCurrentUser();
                                updateUI(User);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInAnonymously:failure", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        }
                    });
        }


        dialog = new ProgressDialog(this);
        dialog.setMessage("Calculating total amount...");
        dialog.setCancelable(false);

        String deviceID = "";
        deviceID.concat("\nFINGERPRINT {" + Build.FINGERPRINT + "}");


        binding.energyChargesTxt.setVisibility(View.GONE);
        binding.consumerCharges.setVisibility(View.GONE);
        binding.fixedChargesTxt.setVisibility(View.GONE);
        binding.wheelingChargesTxt.setVisibility(View.GONE);
        binding.govtEdChrgsTxt.setVisibility(View.GONE);
        binding.govtTaxTxt.setVisibility(View.GONE);
        binding.totalTxt.setVisibility(View.GONE);

        int maxLength = 8;
        binding.meterReading.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});

        binding.calBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if (binding.meterReading.length() > 0) {

                        float reading = Float.valueOf(binding.meterReading.getText().toString().trim());
                        dialog.show();
                        TotalBillAmount(reading);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(binding.textView8.getWindowToken(), 0);

                    } else {
                        Toast.makeText(MainActivity.this, "Please enter meter reading", Toast.LENGTH_SHORT).show();
                    }
            }
        });

        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, add_data.class);
                startActivity(intent);
            }
        });

    }

    //Change UI according to user data.
    public void updateUI(FirebaseUser User){

        if(User != null){
            Toast.makeText(this,"U Signed In successfully",Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this,"U Didnt signed in",Toast.LENGTH_LONG).show();
        }

    }

    public void TotalBillAmount(Float reading) {

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Charges");

        float govtTax = (float) 0.2604, govt_ED = (float) 16;

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot snapshot1: snapshot.getChildren()) {
                    if (snapshot.exists()) {

                        if(reading<=100) {

                            String ec = snapshot.child("Charge 0-100/energy_charges").getValue(String.class);
                            String fc = snapshot.child("Charge 0-100/fixed_charges").getValue(String.class);
                            String wc = snapshot.child("Charge 0-100/wheeling_charges").getValue(String.class);

                            float unit_charge100 = Float.parseFloat(ec), fixed_charge100 = Float.parseFloat(fc), wheelingCharge = Float.parseFloat(wc);

                            binding.energyChargesTxt.setText("Energy charges: ".concat(Float.toString(reading*unit_charge100)));
                            binding.fixedChargesTxt.setText("Fixed charges: ".concat(Float.toString(fixed_charge100)));
                            float Wheeling_charges = (float) (reading*wheelingCharge);
                            binding.wheelingChargesTxt.setText("Wheeling charges: ".concat(Float.toString(Wheeling_charges)));
                            float consumerCharge_sum = (float) (reading*unit_charge100+fixed_charge100+reading*wheelingCharge);
                            binding.consumerCharges.setText("Consumer charges: ".concat(Float.toString(consumerCharge_sum)));
                            float GovtED = govt_ED*consumerCharge_sum/100;
                            float Govt_Tax = (float) (reading*govtTax);
                            float total_amount = consumerCharge_sum+Govt_Tax+GovtED;
                            binding.govtTaxTxt.setText("Govt tax: ".concat(Float.toString(Govt_Tax)));
                            binding.govtEdChrgsTxt.setText("Govt ED charges: ".concat(Float.toString(GovtED)));
                            binding.totalTxt.setText("Total Amount: ".concat(Float.toString(total_amount)));

                        } else if(reading>100 && reading<=300) {

                            String ec = snapshot.child("Charge 100-300/energy_charges").getValue(String.class);
                            String fc = snapshot.child("Charge 100-300/fixed_charges").getValue(String.class);
                            String wc = snapshot.child("Charge 100-300/wheeling_charges").getValue(String.class);
                            String uc100 = snapshot.child("Charge 0-100/energy_charges").getValue(String.class);

                            float unit_charge100 = Float.parseFloat(uc100);
                            float unit_charge1t3 = Float.parseFloat(ec), fixed_charge1t3 = Float.parseFloat(fc), wheelingCharge = Float.parseFloat(wc); //For meter reading 100 to 300

                            float reading_minused = (float) (reading-reading_100);

                            float energy_charges = reading_100*unit_charge100+reading_minused*unit_charge1t3;
                            binding.energyChargesTxt.setText("Energy charges: ".concat(Float.toString(energy_charges)));
                            binding.fixedChargesTxt.setText("Fixed charges: ".concat(Float.toString(fixed_charge1t3)));
                            float Wheeling_charges = (float) (reading*wheelingCharge);
                            binding.wheelingChargesTxt.setText("Wheeling charges: ".concat(Float.toString(Wheeling_charges)));
                            float consumerCharge_sum = (float) (energy_charges+fixed_charge1t3+reading*wheelingCharge);
                            binding.consumerCharges.setText("Consumer charges: ".concat(Float.toString(consumerCharge_sum)));
                            float GovtED = govt_ED*consumerCharge_sum/100;
                            float Govt_Tax = (float) (reading*govtTax);
                            binding.govtTaxTxt.setText("Govt tax: ".concat(Float.toString(Govt_Tax)));
                            binding.govtEdChrgsTxt.setText("Govt ED charges: ".concat(Float.toString(GovtED)));
                            float total_amount = consumerCharge_sum+Govt_Tax+GovtED;
                            binding.totalTxt.setText("Total Amount: ".concat(Float.toString(total_amount)));

                        } else if(reading>300 && reading<=500) {

                            String ec = snapshot.child("Charge 300-500/energy_charges").getValue(String.class);
                            String fc = snapshot.child("Charge 300-500/fixed_charges").getValue(String.class);
                            String wc = snapshot.child("Charge 300-500/wheeling_charges").getValue(String.class);
                            String uc100 = snapshot.child("Charge 0-100/energy_charges").getValue(String.class);
                            String uc1t3 = snapshot.child("Charge 100-300/energy_charges").getValue(String.class);

                            float unit_charge100 = Float.parseFloat(uc100);
                            float unit_charge1t3 = Float.parseFloat(uc1t3);

                            float unit_charge3t5 = Float.parseFloat(ec), fixed_charge3t5 = Float.parseFloat(fc), wheelingCharge = Float.parseFloat(wc); //For meter reading 300 to 500

                            float reading_minused = (float) (reading-(reading_100+reading_200));

                            float energy_charges = reading_100*unit_charge100+reading_200*unit_charge1t3+reading_minused*unit_charge3t5;
                            binding.energyChargesTxt.setText("Energy charges: ".concat(Float.toString(energy_charges)));
                            binding.fixedChargesTxt.setText("Fixed charges: ".concat(Float.toString(fixed_charge3t5)));
                            float Wheeling_charges = (float) (reading*wheelingCharge);
                            binding.wheelingChargesTxt.setText("Wheeling charges: ".concat(Float.toString(Wheeling_charges)));
                            float consumerCharge_sum = (float) (energy_charges+fixed_charge3t5+reading*wheelingCharge);
                            binding.consumerCharges.setText("Consumer charges: ".concat(Float.toString(consumerCharge_sum)));
                            float GovtED = govt_ED*consumerCharge_sum/100;
                            float Govt_Tax = (float) (reading*govtTax);
                            binding.govtTaxTxt.setText("Govt tax: ".concat(Float.toString(Govt_Tax)));
                            binding.govtEdChrgsTxt.setText("Govt ED charges: ".concat(Float.toString(GovtED)));
                            float total_amount = consumerCharge_sum+Govt_Tax+GovtED;
                            binding.totalTxt.setText("Total Amount: ".concat(Float.toString(total_amount)));

                        } else if(reading>500) {

                            String ec = snapshot.child("Charge 500>/energy_charges").getValue(String.class);
                            String fc = snapshot.child("Charge 500>/fixed_charges").getValue(String.class);
                            String wc = snapshot.child("Charge 500>/wheeling_charges").getValue(String.class);
                            String uc100 = snapshot.child("Charge 0-100/energy_charges").getValue(String.class);
                            String uc1t3 = snapshot.child("Charge 100-300/energy_charges").getValue(String.class);
                            String uc3t5 = snapshot.child("Charge 100-300/energy_charges").getValue(String.class);

                            float unit_charge100 = Float.parseFloat(uc100);
                            float unit_charge1t3 = Float.parseFloat(uc1t3);
                            float unit_charge3t5 = Float.parseFloat(uc3t5);

                            float unit_charge5 = Float.parseFloat(ec), fixed_charge5 = Float.parseFloat(fc), wheelingCharge = Float.parseFloat(wc); //For meter reading above 500

                            float reading_minused = (float) (reading-(reading_100+reading_200+reading_200));

                            float energy_charges = reading_100*unit_charge100+reading_200*unit_charge1t3+reading_200*unit_charge3t5+reading_minused*unit_charge5;
                            binding.energyChargesTxt.setText("Energy charges: ".concat(Float.toString(energy_charges)));
                            binding.fixedChargesTxt.setText("Fixed charges: ".concat(Float.toString(fixed_charge5)));
                            float Wheeling_charges = (float) (reading*wheelingCharge);
                            binding.wheelingChargesTxt.setText("Wheeling charges: ".concat(Float.toString(Wheeling_charges)));
                            float consumerCharge_sum = (float) (energy_charges+fixed_charge5+reading*wheelingCharge);
                            binding.consumerCharges.setText("Consumer charges: ".concat(Float.toString(consumerCharge_sum)));
                            float GovtED = govt_ED*consumerCharge_sum/100;
                            float Govt_Tax = (float) (reading*govtTax);
                            binding.govtTaxTxt.setText("Govt tax: ".concat(Float.toString(Govt_Tax)));
                            binding.govtEdChrgsTxt.setText("Govt ED charges: ".concat(Float.toString(GovtED)));
                            float total_amount = consumerCharge_sum+Govt_Tax+GovtED;
                            binding.totalTxt.setText("Total Amount: ".concat(Float.toString(total_amount)));
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        // Hide after some seconds
        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                    binding.energyChargesTxt.setVisibility(View.VISIBLE);
                    binding.consumerCharges.setVisibility(View.VISIBLE);
                    binding.fixedChargesTxt.setVisibility(View.VISIBLE);
                    binding.wheelingChargesTxt.setVisibility(View.VISIBLE);
                    binding.govtEdChrgsTxt.setVisibility(View.VISIBLE);
                    binding.govtTaxTxt.setVisibility(View.VISIBLE);
                    binding.totalTxt.setVisibility(View.VISIBLE);
                }
            }
        };

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 2000);
    }
}