package com.elbcalculator;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
        binding.facCharges.setVisibility(View.GONE);
        binding.fixedChargesTxt.setVisibility(View.GONE);
        binding.wheelingChargesTxt.setVisibility(View.GONE);
        binding.govtEdChrgsTxt.setVisibility(View.GONE);
        binding.govtTaxTxt.setVisibility(View.GONE);
        binding.totalTxt.setVisibility(View.GONE);

        int maxLength = 8;
        binding.meterReading.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});

        binding.calBtn.setOnClickListener(v -> {

                if (binding.meterReading.length() > 0) {

                    float reading = Float.valueOf(binding.meterReading.getText().toString().trim());
                    dialog.show();
                    TotalBillAmount(reading);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.textView8.getWindowToken(), 0);

                } else {
                    Toast.makeText(MainActivity.this, "Please enter meter reading", Toast.LENGTH_SHORT).show();
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

        double govtTax = (float) 0.2604, govt_ED = (float) 16;

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot snapshot1: snapshot.getChildren()) {
                    if (snapshot.exists()) {

                        if(reading<=100) {

                            String ec = snapshot.child("Charge 0-100/energy_charges").getValue(String.class);
                            String fc = snapshot.child("Charge 0-100/fixed_charges").getValue(String.class);
                            String wc = snapshot.child("Charge 0-100/wheeling_charges").getValue(String.class);
                            String FAC1 = snapshot.child("Charge 0-100/fac_charges").getValue(String.class);

                            double unit_charge100 = Float.parseFloat(ec), fixed_charge100 = Float.parseFloat(fc), wheelingCharge = Float.parseFloat(wc);

                            double fac1 = Double.parseDouble(FAC1);

                            binding.energyChargesTxt.setText("Energy charges: ".concat(String.format("%.2f", reading*unit_charge100)));
                            binding.fixedChargesTxt.setText("Fixed charges: ".concat(String.format("%.2f", fixed_charge100)));
                            double Wheeling_charges = (reading*wheelingCharge);
                            binding.wheelingChargesTxt.setText("Wheeling charges: ".concat(String.format("%.2f", Wheeling_charges)));

                            double FAC = reading*fac1;
                            binding.facCharges.setText("FAC charges: ".concat(String.format("%.2f", FAC)));

                            double consumerCharge_sum = (reading*unit_charge100+fixed_charge100+reading*wheelingCharge)+FAC;
                            binding.consumerCharges.setText("Consumer charges: ".concat(String.format("%.2f", consumerCharge_sum)));
                            double GovtED = govt_ED*consumerCharge_sum/100;
                            double Govt_Tax = (reading*govtTax);
                            double total_amount = consumerCharge_sum+Govt_Tax+GovtED;
                            binding.govtTaxTxt.setText("Govt tax: ".concat(String.format("%.2f", Govt_Tax)));
                            binding.govtEdChrgsTxt.setText("Govt ED charges: ".concat(String.format("%.2f", GovtED)));
                            binding.totalTxt.setText("Total Amount: ".concat(String.format("%.2f", total_amount)));

                        } else if(reading>100 && reading<=300) {

                            String ec = snapshot.child("Charge 100-300/energy_charges").getValue(String.class);
                            String fc = snapshot.child("Charge 100-300/fixed_charges").getValue(String.class);
                            String wc = snapshot.child("Charge 100-300/wheeling_charges").getValue(String.class);
                            String uc100 = snapshot.child("Charge 0-100/energy_charges").getValue(String.class);
                            String FAC1 = snapshot.child("Charge 0-100/fac_charges").getValue(String.class);
                            String FAC2 = snapshot.child("Charge 100-300/fac_charges").getValue(String.class);

                            double unit_charge100 = Double.parseDouble(uc100);
                            double unit_charge1t3 = Double.parseDouble(ec), fixed_charge1t3 = Float.parseFloat(fc), wheelingCharge = Float.parseFloat(wc); //For meter reading 100 to 300

                            double fac1 = Double.parseDouble(FAC1);
                            double fac2 = Double.parseDouble(FAC2);

                            double reading_minused = (reading-reading_100);

                            double energy_charges = reading_100*unit_charge100+reading_minused*unit_charge1t3;
                            binding.energyChargesTxt.setText("Energy charges: ".concat(String.format("%.2f", energy_charges)));
                            binding.fixedChargesTxt.setText("Fixed charges: ".concat(String.format("%.2f", fixed_charge1t3)));
                            double Wheeling_charges = (reading*wheelingCharge);
                            binding.wheelingChargesTxt.setText("Wheeling charges: ".concat(String.format("%.2f", Wheeling_charges)));

                            double FAC = (reading_100*fac1) + (reading_minused*fac2);
                            binding.facCharges.setText("FAC charges: ".concat(String.format("%.2f", FAC)));

                            double consumerCharge_sum = (energy_charges+fixed_charge1t3+reading*wheelingCharge)+FAC;
                            binding.consumerCharges.setText("Consumer charges: ".concat(String.format("%.2f", consumerCharge_sum)));
                            double GovtED = govt_ED*consumerCharge_sum/100;
                            double Govt_Tax = (reading*govtTax);
                            binding.govtTaxTxt.setText("Govt tax: ".concat(String.format("%.2f", Govt_Tax)));
                            binding.govtEdChrgsTxt.setText("Govt ED charges: ".concat(String.format("%.2f", GovtED)));
                            double total_amount = consumerCharge_sum+Govt_Tax+GovtED;
                            binding.totalTxt.setText("Total Amount: ".concat(String.format("%.2f", total_amount)));

                        } else if(reading>300 && reading<=500) {

                            String ec = snapshot.child("Charge 300-500/energy_charges").getValue(String.class);
                            String fc = snapshot.child("Charge 300-500/fixed_charges").getValue(String.class);
                            String wc = snapshot.child("Charge 300-500/wheeling_charges").getValue(String.class);
                            String uc100 = snapshot.child("Charge 0-100/energy_charges").getValue(String.class);
                            String uc1t3 = snapshot.child("Charge 100-300/energy_charges").getValue(String.class);
                            String FAC1 = snapshot.child("Charge 0-100/fac_charges").getValue(String.class);
                            String FAC2 = snapshot.child("Charge 100-300/fac_charges").getValue(String.class);
                            String FAC3 = snapshot.child("Charge 300-500/fac_charges").getValue(String.class);

                            double unit_charge100 = Float.parseFloat(uc100);
                            double unit_charge1t3 = Float.parseFloat(uc1t3);

                            double fac1 = Double.parseDouble(FAC1);
                            double fac2 = Double.parseDouble(FAC2);
                            double fac3 = Double.parseDouble(FAC3);

                            double unit_charge3t5 = Float.parseFloat(ec), fixed_charge3t5 = Float.parseFloat(fc), wheelingCharge = Float.parseFloat(wc); //For meter reading 300 to 500

                            double reading_minused = (reading-(reading_100+reading_200));

                            double energy_charges = reading_100*unit_charge100+reading_200*unit_charge1t3+reading_minused*unit_charge3t5;
                            binding.energyChargesTxt.setText("Energy charges: ".concat(String.format("%.2f", energy_charges)));
                            binding.fixedChargesTxt.setText("Fixed charges: ".concat(String.format("%.2f", fixed_charge3t5)));
                            double Wheeling_charges = (reading*wheelingCharge);
                            binding.wheelingChargesTxt.setText("Wheeling charges: ".concat(String.format("%.2f", Wheeling_charges)));

                            double FAC = (reading_100*fac1) + (reading_200*fac2) + (reading_minused*fac3);
                            binding.facCharges.setText("FAC charges: ".concat(String.format("%.2f", FAC)));

                            double consumerCharge_sum = (energy_charges+fixed_charge3t5+reading*wheelingCharge)+FAC;
                            binding.consumerCharges.setText("Consumer charges: ".concat(String.format("%.2f", consumerCharge_sum)));
                            double GovtED = govt_ED*consumerCharge_sum/100;
                            double Govt_Tax = (reading*govtTax);
                            binding.govtTaxTxt.setText("Govt tax: ".concat(String.format("%.2f", Govt_Tax)));
                            binding.govtEdChrgsTxt.setText("Govt ED charges: ".concat(String.format("%.2f", GovtED)));
                            double total_amount = consumerCharge_sum+Govt_Tax+GovtED;
                            binding.totalTxt.setText("Total Amount: ".concat(String.format("%.2f", total_amount)));

                        } else if(reading>500) {

                            String ec = snapshot.child("Charge 500>/energy_charges").getValue(String.class);
                            String fc = snapshot.child("Charge 500>/fixed_charges").getValue(String.class);
                            String wc = snapshot.child("Charge 500>/wheeling_charges").getValue(String.class);
                            String uc100 = snapshot.child("Charge 0-100/energy_charges").getValue(String.class);
                            String uc1t3 = snapshot.child("Charge 100-300/energy_charges").getValue(String.class);
                            String uc3t5 = snapshot.child("Charge 100-300/energy_charges").getValue(String.class);
                            String FAC1 = snapshot.child("Charge 0-100/fac_charges").getValue(String.class);
                            String FAC2 = snapshot.child("Charge 100-300/fac_charges").getValue(String.class);
                            String FAC3 = snapshot.child("Charge 300-500/fac_charges").getValue(String.class);
                            String FAC4 = snapshot.child("Charge 500>/fac_charges").getValue(String.class);

                            double unit_charge100 = Float.parseFloat(uc100);
                            double unit_charge1t3 = Float.parseFloat(uc1t3);
                            double unit_charge3t5 = Float.parseFloat(uc3t5);

                            double fac1 = Double.parseDouble(FAC1);
                            double fac2 = Double.parseDouble(FAC2);
                            double fac3 = Double.parseDouble(FAC3);
                            double fac4 = Double.parseDouble(FAC4);

                            double unit_charge5 = Float.parseFloat(ec), fixed_charge5 = Float.parseFloat(fc), wheelingCharge = Float.parseFloat(wc); //For meter reading above 500

                            double reading_minused = (reading-(reading_100+reading_200+reading_200));

                            double energy_charges = reading_100*unit_charge100+reading_200*unit_charge1t3+reading_200*unit_charge3t5+reading_minused*unit_charge5;
                            binding.energyChargesTxt.setText("Energy charges: ".concat(String.format("%.2f", energy_charges)));
                            binding.fixedChargesTxt.setText("Fixed charges: ".concat(String.format("%.2f", fixed_charge5)));
                            double Wheeling_charges = (reading*wheelingCharge);
                            binding.wheelingChargesTxt.setText("Wheeling charges: ".concat(String.format("%.2f", Wheeling_charges)));

                            double FAC = (reading_100*fac1) + (reading_200*fac2) + (reading_200*fac3) + (reading_minused*fac4);
                            binding.facCharges.setText("FAC charges: ".concat(String.format("%.2f", FAC)));

                            double consumerCharge_sum = (energy_charges+fixed_charge5+reading*wheelingCharge)+FAC;
                            binding.consumerCharges.setText("Consumer charges: ".concat(String.format("%.2f", consumerCharge_sum)));
                            double GovtED = govt_ED*consumerCharge_sum/100;
                            double Govt_Tax = (reading*govtTax);
                            binding.govtTaxTxt.setText("Govt tax: ".concat(String.format("%.2f", Govt_Tax)));
                            binding.govtEdChrgsTxt.setText("Govt ED charges: ".concat(String.format("%.2f", GovtED)));
                            double total_amount = consumerCharge_sum+Govt_Tax+GovtED;

                            binding.totalTxt.setText("Total Amount: ".concat(String.format("%.2f", total_amount)));
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
                    binding.facCharges.setVisibility(View.VISIBLE);
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