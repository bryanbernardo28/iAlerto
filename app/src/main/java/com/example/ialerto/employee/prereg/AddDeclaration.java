package com.example.ialerto.employee.prereg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.ialerto.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class AddDeclaration extends AppCompatActivity {
    private static TextInputEditText et_firstname,et_middlename,et_lastname,et_relationship,et_birthdate;
    private TextInputLayout etl_firstname,etl_middlename,etl_lastname,etl_relationship,etl_birthdate;
    static String birthdate = "";
    String declaration;
    boolean has_declaration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_declaration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        has_declaration = getIntent().getBooleanExtra("has_declaration",false);
        Log.d("check","Has Declaration: " + has_declaration);

        et_firstname = findViewById(R.id.et_firstname_id);
        et_middlename = findViewById(R.id.et_middlename_id);
        et_lastname = findViewById(R.id.et_lastname_id);
        et_relationship = findViewById(R.id.et_relationship_id);


        etl_firstname = findViewById(R.id.etl_firstname_id);
        etl_middlename = findViewById(R.id.etl_middlename_id);
        etl_lastname = findViewById(R.id.etl_lastname_id);
        etl_relationship = findViewById(R.id.etl_relationship_id);
        etl_birthdate = findViewById(R.id.etl_birthdate_id);
        et_birthdate = findViewById(R.id.et_birthdate_id);
        et_birthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });


    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(year, month, day, 0, 0, 0);
            Date chosenDate = cal.getTime();
            birthdate = String.valueOf(cal.getTimeInMillis());

//            DateFormat df_long = DateFormat.getDateInstance(DateFormat.LONG);
//            String df_long_str = df_long.format(chosenDate);

            SimpleDateFormat format = new SimpleDateFormat("LLLL dd,yyyy");
            String strDate = format.format(chosenDate);

            et_birthdate.setText(strDate);
        }
    }

    public void submit(View v){
        String firstname = et_firstname.getText().toString();
        String middlename = et_middlename.getText().toString();
        String lastname = et_lastname.getText().toString();
        String relationship = et_relationship.getText().toString();
        boolean[] errors = new boolean[4];

        if (firstname.trim().isEmpty()){
            etl_firstname.setError("First Name field is required.");
            errors[0] = true;
        }
        else{
            etl_firstname.setError(null);
            errors[0] = false;
        }

        if (lastname.trim().isEmpty()){
            etl_lastname.setError("Last Name field is required.");
            errors[1] = true;
        }
        else{
            etl_lastname.setError(null);
            errors[1] = false;
        }

        if (relationship.trim().isEmpty()){
            etl_relationship.setError("Relationship field is required.");
            errors[2] = true;
        }
        else{
            etl_relationship.setError(null);
            errors[2] = false;
        }

        if (et_birthdate.getText().toString().trim().isEmpty() || et_birthdate.getText().toString().trim() == ""){
            etl_birthdate.setError("Birth Date field is required.");
            errors[3] = true;
        }
        else{
            etl_birthdate.setError(null);
            errors[3] = false;
        }


        if (!errors[0] && !errors[1] && !errors[2] && !errors[3]){
            try {
                JSONObject declaration_fields = new JSONObject();
                JSONArray array_declarations = new JSONArray();

                if (has_declaration){
                    declaration = getIntent().getStringExtra("declarations");
                    array_declarations = new JSONArray(declaration);
                }

                declaration_fields.put("firstname",firstname);
                declaration_fields.put("middlename",middlename);
                declaration_fields.put("lastname",lastname);
                declaration_fields.put("relationship",relationship);
                declaration_fields.put("birthdate",birthdate);

                array_declarations.put(declaration_fields);
                Intent i = new Intent();
                i.putExtra("has_declaration",true);
                i.putExtra("declarations", String.valueOf(array_declarations));
                setResult(Activity.RESULT_OK, i);
                finish();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        declaration = getIntent().getStringExtra("declarations");
        has_declaration = getIntent().getBooleanExtra("has_declaration",false);
        Intent i = new Intent();
        i.putExtra("has_declaration",has_declaration);
        i.putExtra("declarations", declaration);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}