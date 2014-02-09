package com.uplb.queuemanager;

import java.util.ArrayList;
import java.util.Calendar;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The Class HomeActivity serves as the back end of the home activity.
 */
public class HomeActivity extends Activity {
	
	private TextView date,qstatus;
	private Button close_queue,queue;
	private DatabaseAdapter databaseAdapter;
	private ArrayList<String> customerList;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		date=(TextView)this.findViewById(R.id.date);
		qstatus=(TextView)this.findViewById(R.id.queue_count);
		queue=(Button)this.findViewById(R.id.close_queue);
		close_queue=(Button)this.findViewById(R.id.pause_queue);
		
		final Calendar c = Calendar.getInstance();
	    int yy = c.get(Calendar.YEAR);
	    int mm = c.get(Calendar.MONTH);
	    int dd = c.get(Calendar.DAY_OF_MONTH);

	    // set current date into textview
	    date.setText(new StringBuilder()
	    			.append(mm+1).append("-")
	    			.append(dd).append("-").append(yy));
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	/**
	 * queue_method called by onClick attribute of login button. 
	 *
	 * @param current view of the button
	 */
	public void queue_method(View view){
		databaseAdapter = new DatabaseAdapter(getApplicationContext());
        databaseAdapter.open();
        String text = Integer.toString(databaseAdapter.retrieveCustomerNumber());
		databaseAdapter.close();
		
		qstatus.setText(text+" people in queue");
		queue.setText("QUEUE");
		
		close_queue.setVisibility(View.VISIBLE);
		
		Intent i=new Intent(HomeActivity.this, QueueActivity.class);
        startActivity(i);
		
	}
	
	public void close_method(View view){
		
		databaseAdapter = new DatabaseAdapter(getApplicationContext());
        
		//prompt user ("Are you sure you want to close queue? Closing the queue would delete all currently enqueued customers")
		AlertDialog.Builder welcome = new AlertDialog.Builder(this);
			welcome.setMessage("Closing the queue would delete all currently enqueued customers.");
			welcome.setTitle("Close Queue?");
			welcome.setPositiveButton("Close", new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which) { //if yes
					
			    	databaseAdapter.open();
			    	customerList=databaseAdapter.getAllCustomers();	//get enqueued customers
			    	for(final String customer: customerList){
			    		
			    		//prompt all customers ("The management decided to close the bank, you will now be deleted from the queue. You can register again when banking operations resume tomorrow (8AM) Thank  you!")
						try{
			    			   String prompt_message = "The management decided to close the bank, you will now be deleted from the queue. You can register again when banking operations resume tomorrow (8AM) Thank  you!";
		                 	   SmsManager sms = SmsManager.getDefault(); 
		                 	   sms.sendTextMessage(customer, null, prompt_message, null, null);
		                 	   //prompt regarding sent confirmation message
			                   Toast.makeText(getApplicationContext(), "Prompt SMS Sent to " + customer, Toast.LENGTH_LONG).show();
		                    }catch (Exception e) {
		                 	   e.printStackTrace();
		                    }//end SMS
						
						//delete all customers isqueued=-1 -1 means cancelled trans
			    		databaseAdapter.updateStatus2(customer);
			    		databaseAdapter.updateQueuePosition(customer,-1);
			    	}
			        databaseAdapter.close();
					
					
			    	//go to the LOGS page
					Intent intent = new Intent();
			    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			        intent.setClass(getApplicationContext(), LogsActivity.class);
			        startActivity(intent);
			        dialog.cancel();
			        return;
			    } 
			});
			welcome.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which) {//else if no walang gagawin :)
			    	
			    	//Intent intent = new Intent();
			    	//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			        //intent.setClass(getApplicationContext(), Settings.class);
			        //startActivity(intent);
			        //dialog.cancel();
			        //return;
			    } 
			});
		welcome.show();
		
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
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
        case R.id.logs:
        	startActivity(new Intent(this, LogsActivity.class));
        	return true;
        default:
        return super.onOptionsItemSelected(item);
       }
	}
	
}
