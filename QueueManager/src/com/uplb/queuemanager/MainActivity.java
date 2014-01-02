package com.uplb.queuemanager;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
//import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private EditText password;
	private ArrayList<String> userList = new ArrayList<String>();
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	/*Context context=this.getApplicationContext();
    	SharedPreferences settings=context.getSharedPreferences("PREFERENCES", 0);
    	boolean isLogged=settings.getBoolean("isLogged", false);
    	*/
    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        password = (EditText)this.findViewById(R.id.password);
    	
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(getApplicationContext());
 		databaseAdapter.open();
 		
 		userList=databaseAdapter.getAllUsers();
 		if(userList==null){
 			AlertDialog.Builder welcome = new AlertDialog.Builder(this);
 			welcome.setMessage("To start using Queue Manager, you have to update your profile first.");
 			welcome.setTitle("Welcome!");
 			welcome.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog, int which) {
 			    	Intent intent = new Intent();
 			    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			        intent.setClass(getApplicationContext(), Settings.class);
 			        startActivity(intent);
 			        dialog.cancel();
 			        return;
 			    } 
 			});
 			welcome.show();
 		}
 		databaseAdapter.close();
		
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void login_method(View view){
    	//database
    	int flag=0;
    	if(userList!=null){
    	for(String user: userList){//user must be decrypted? (md5)
	        if(password.getText().toString().equals(user)){//pano kng pareho ng password? T_T
	    		
	        	//init queue here (lipat to sa queue pag may session na)
	        	DatabaseAdapter databaseAdapter = new DatabaseAdapter(getApplicationContext());
	     		databaseAdapter.open();
	     		//databaseAdapter.insertVirtualQueue(0,0,databaseAdapter.getUserID(user));
	     		databaseAdapter.close();
	     		
	        	//move to next page
	    		Intent i=new Intent(MainActivity.this, HomeActivity.class);
	    		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			    startActivity(i);
	            flag=1;
	            
	            /*Context context=this.getApplicationContext();
	            SharedPreferences settings=context.getSharedPreferences("PREFERENCES", 0);
	            SharedPreferences.Editor editor = settings.edit();
	            editor.putBoolean("isLogged", true);
	            editor.commit();
	            */
	            break;
	    	}
    	}
    	if(flag!=1)
    		Toast.makeText(getApplicationContext(), "Incorrect Password.", Toast.LENGTH_LONG).show();
    	}
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.about:
        	startActivity(new Intent(this, About.class));
        	return true;
        case R.id.help:
        	startActivity(new Intent(this, Help.class));
        	return true;
        case R.id.action_settings:
        	startActivity(new Intent(this, Settings.class));
        	return true;
        default:
        return super.onOptionsItemSelected(item);
       }
	}
    
}
