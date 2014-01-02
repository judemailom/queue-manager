package com.uplb.queuemanager;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
//import android.widget.Toast;

public class Settings extends Activity {
	private EditText password, name, contact, address;
	private DatabaseAdapter databaseAdapter;
	private String comp_name,comp_pass,comp_contact,comp_add;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		password = (EditText)this.findViewById(R.id.profile_password);
		name = (EditText)this.findViewById(R.id.profile_name);
		contact = (EditText)this.findViewById(R.id.profile_phone);
		address = (EditText)this.findViewById(R.id.profile_address);
		
		databaseAdapter = new DatabaseAdapter(getApplicationContext());
		databaseAdapter.open();
		comp_name = databaseAdapter.getUserName();
		comp_pass = databaseAdapter.getPassword();
		comp_contact = databaseAdapter.getContact();
		comp_add = databaseAdapter.getAddress();
		databaseAdapter.close();
		
		if(comp_name!=""){
			name.setText(comp_name);
			password.setText(comp_pass);
			contact.setText(comp_contact);
			address.setText(comp_add);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	public void save_method(View view){
		
		databaseAdapter = new DatabaseAdapter(getApplicationContext());
		databaseAdapter.open();
		if(comp_name=="")
			databaseAdapter.insertUser(address.getText().toString(), password.getText().toString(), name.getText().toString(), contact.getText().toString());
		else
			databaseAdapter.updateUser(comp_name, name.getText().toString(), password.getText().toString(), address.getText().toString(), contact.getText().toString());//update
		
		databaseAdapter.close();
		 
		finish();
		
		//re-instantiate MainActivity
		Intent i=new Intent(Settings.this, MainActivity.class);
        startActivity(i);
	        
        
	}
}
