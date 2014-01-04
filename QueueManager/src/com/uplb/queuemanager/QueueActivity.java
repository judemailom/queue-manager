package com.uplb.queuemanager;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.telephony.SmsManager;
//import android.graphics.Color;
//import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.view.ViewGroup;
//import android.view.View.OnClickListener;
import android.widget.Button;
//import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class QueueActivity extends Activity {
	
	//private static final String LOG = "com.uplb.queuemanager";
	private TableLayout table, serving_table;
	private ArrayList<String> customerList;
	private DatabaseAdapter databaseAdapter;
	
	private TextView tv_pass ,tv2_pass; 
	private Button btn_pass;
	private TableRow tr_pass;

	//get end time | update all other customers | send SMS
	View.OnClickListener click2(final String customer, final TableLayout table, final ArrayList<String> customerList)  {
	    return new View.OnClickListener() {
	        public void onClick(View v) {
	        	
	        		Calendar d = Calendar.getInstance();
	        		String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
			        time = time + "." + Integer.toString(d.get(Calendar.MILLISECOND));   
			        databaseAdapter = new DatabaseAdapter(getApplicationContext());
			        databaseAdapter.open();
			        databaseAdapter.updateEndTime(customer, time);
			        
			        //hide the row
			        table.removeView(table.getChildAt(1));
			        //DELETE THIS CUSTOMER
			        int queue_index = databaseAdapter.getQueuePosn(customer);	
			        databaseAdapter.updateStatus(customer);
			        databaseAdapter.updateQueuePosition(customer,0);
			        //compute for the waiting time of other customers
			        String st = databaseAdapter.getStartTime(customer);
			        String et = databaseAdapter.getEndTime(customer);
			        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
			        
			        try {
						Date start = df.parse(st);
						Date end = df.parse(et);
						long diff_mil = end.getTime()-start.getTime();
						long diff_min = diff_mil/60000; 
						databaseAdapter.updateServiceTime(customer, (int)diff_min);
					} catch (ParseException e) {
						e.printStackTrace();
					}
			        
			        //REF Queue.java -> line 73-75
			        //compute | update estimated waiting time | formula
			        int sts = databaseAdapter.getServiceTimeSum(); 
			        int custDone = databaseAdapter.retrieveCustomerDone();	
			        int awt = sts/custDone; //average waiting (service) time
			        //databaseAdapter.updateAverageServiceTime(0, awt); //better yet use the date & fkey (or any ID of the current queue)
			        //UPDATE NEW WAITING TIME -> consider queue table
            		//do i really need queue table T_T
			        
			        //UPDATE QUEUE POSITION OF OTHER CUSTOMERS
			        if(databaseAdapter.getAllCustomers()!=null){
			        
			        for(String phone: databaseAdapter.getAllCustomers()){
				        int posn=databaseAdapter.getQueuePosn(phone);
	            		int ewt = awt*(posn-2);
				        
	            		//SEND UPDATE MESSAGE TO ALL WHOS NOT DONE BELOW THE ONE BEING SERVED 
				        if(posn>queue_index && (posn-1)>0){
				        	databaseAdapter.updateQueuePosition(phone,posn-1);
		            		String confirmation_message = "Good day! Currently, there are only "+ (posn-1) +" customers to be served before you. Your estimated waiting time is " + ewt +" minutes. Thank you! -Management";
		                    //prompt regarding sent confirmation message
		                    try{
		                 	   SmsManager sms = SmsManager.getDefault(); 
		                 	   sms.sendTextMessage(phone, null, confirmation_message, null, null);
		                 	   databaseAdapter.updateWaitTime(phone, ewt);
		                 	   Toast.makeText(getApplicationContext(), "Update SMS Sent to " + phone, Toast.LENGTH_LONG).show();
		                    }catch (Exception e) {
		                 	   //Toast.makeText(getApplicationContext(),"SMS failed, please try again.",Toast.LENGTH_LONG).show();
		                 	   e.printStackTrace();
		                    }}//end SMS
	            		}//end if
			        }//end - for
			        
			        
			        databaseAdapter.close();
			        
			        
			      
	        }
	    };
	}
	
	//layout
	View.OnClickListener click(final String customer, final String time, final TableRow row, final TableLayout serving_table, final TextView tv, final TextView tv2, final Button btn, final TableRow tr, final TableLayout queue_table, final ArrayList<String> customerList)  {
	    return new View.OnClickListener() {
	        public void onClick(View v) {
	        	
	        	databaseAdapter = new DatabaseAdapter(getApplicationContext());
		        
	        	if(serving_table.getChildAt(1)==null){//SQSSP
	        	row.setVisibility(View.GONE);
	       	 	
	        	//update color of other customers (may mali)
		        for(int i=1;i<table.getChildCount();i++){
		        	if((i+1)%2==0)
		        		queue_table.getChildAt(i).setBackgroundResource(R.color.gray);
		        	else
		        		queue_table.getChildAt(i).setBackgroundResource(R.color.white);
		       	}
		        
		        databaseAdapter.open();
	       	 	tv.setText(databaseAdapter.getName(customer));
		        tv2.setText("Waiting time: "+time+" mins.");
		        btn.setText("End");
		        databaseAdapter.close();
		        
		        btn.setOnClickListener(click2(customer, serving_table, customerList));
		        
		        tv.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Small);
		        tv2.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Small);
		        tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		        tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		        btn.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		        	
		        tv.setGravity(Gravity.CENTER);
		        tv2.setGravity(Gravity.CENTER);
		        tv.setPadding(10,0,0,0);
		        tv2.setPadding(15,0,0,0);
		        btn.setGravity(Gravity.CENTER);
		        //find why they are not synchronized when it comes to layout (when in fact they are the same)
		        //or hard code the padding of button
		        //google: how to move button to the left of the column
		        
		        tr.removeAllViews(); //row to be passed and added to the serving_table
		        tr.addView(tv);
		        tr.addView(tv2);
		        tr.addView(btn);
		        tr.setPadding(5,5,5,5);
		        tr.setBackgroundResource(R.color.white);
		        
		        serving_table.addView(tr,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) );
		        
		        //record start time
		        Calendar d = Calendar.getInstance();
		        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
		        time = time + "." + Integer.toString(d.get(Calendar.MILLISECOND));   
		        
		        databaseAdapter.open();
		        databaseAdapter.updateStartTime(customer, time);
		        int queue_index = databaseAdapter.getQueuePosn(customer);	
		        databaseAdapter.close();
		        
		        if(queue_index!=1){//may nalampasan siya sa queue (yung mga wala pa)
		        	//prompt those who are not yet there
		        	for(int j=1;j<queue_index;j++){
		        		databaseAdapter.open();
		        		String phone2 = databaseAdapter.getCustomerPhone(j);
		        		databaseAdapter.close();
		        		
		        		String prompt_message = "Other customers has been served since you are not yet here. You will be priority once you arrived. Thank you! -Management";
	                    //prompt regarding sent confirmation message
	                    try{
	                 	   SmsManager sms = SmsManager.getDefault(); 
	                 	   sms.sendTextMessage(phone2, null, prompt_message, null, null);
	                 	   Toast.makeText(getApplicationContext(), "Prompt SMS Sent to " + phone2, Toast.LENGTH_LONG).show();
	                    }catch (Exception e) {
	                 	   e.printStackTrace();
	                    }}//end SMS
		        		
		        	}
		        }
		        
	        		/*else if(serving_table.getChildAt(1)==null && swap.getText()=="Swap UP"){
	        		//not sure if condition is correct (kailangan ba di siya nagsserve para magswap?)
	        		//how will you display the change?
	        		
	        		//View row1 = queue_table.getChildAt(i);
	        		//View row2 = queue_table.getChildAt(i-1);
	        		//queue_table.removeViewAt(i);
	        		//queue_table.removeViewAt(i-1);
	        		//queue_table.addView(row2, i);
	        		//queue_table.addView(row1, i-1);
	        		int i = queue_table.indexOfChild(row);
	        		View row2 = queue_table.getChildAt(i);
	        		View row1 = queue_table.getChildAt(i-1);
	        		queue_table.removeViewAt(i);
	        		queue_table.removeViewAt(i-1);
	        		queue_table.addView(row2, i-1);
	        		queue_table.addView(row1, i);
	        		
	        		//update details of row
	        		//how to access button above swap T_T
	        		//if(i==2)
	        		//	swap.setText("Serve");
	        		
	        		//swap queue_position in database
	        		databaseAdapter.open();
	        		int cust_posn1 = databaseAdapter.getQueuePosn(customer);
	        		String customer2 = databaseAdapter.getCustomerPhone(cust_posn1+1);
	        		databaseAdapter.updateQueuePosition(customer, cust_posn1+1);
	        		databaseAdapter.updateQueuePosition(customer2, cust_posn1);
	        		databaseAdapter.close();
	 		        
	        	}*/
	        	else
	        		 Toast.makeText(getApplicationContext(), "You are still serving a customer." , Toast.LENGTH_LONG).show();
	        }
	    };
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_queue);
			
			//project initial data from db
			table = (TableLayout)this.findViewById(R.id.table_current_queue);
			serving_table = (TableLayout)this.findViewById(R.id.table_now_serving);
			customerList = new ArrayList<String>();
			
			databaseAdapter = new DatabaseAdapter(getApplicationContext());
	        databaseAdapter.open();
	        customerList=databaseAdapter.getAllCustomers();
	        databaseAdapter.close();
			
	        tv_pass = new TextView(this);
	    	tv2_pass = new TextView(this);
	    	btn_pass = new Button(this);
	    	tr_pass = new TableRow(this);
	    	
	        
	        int i=0;
	        if(customerList!=null){
	        for(final String customer: customerList){
				TableRow row = new TableRow(this);
				TextView tv = new TextView(this);
				TextView tv2 = new TextView(this);
				Button img = new Button(this);
				
				//get waiting_time wrt phone_number
				databaseAdapter.open();
		        final String time = databaseAdapter.getWaitingTime(customer);
		        
		        tv.setText(databaseAdapter.getName(customer));
		        tv2.setText("Waiting time: "+time+" mins.");
		        img.setText("Serve");
		        
		        databaseAdapter.close();
		        
		        img.setOnClickListener(click(customer,time,row,serving_table,tv_pass,tv2_pass,btn_pass,tr_pass,table,customerList));
		        
		        tv.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Small);
		        tv2.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Small);
		        tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		        tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		        img.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		        	
		      
		        tv.setGravity(Gravity.CENTER);
		        tv2.setGravity(Gravity.CENTER);
		        
		        row.addView(tv);
		        row.addView(tv2);
		        row.addView(img);
		        row.setPadding(5,5,5,5);
		        tv.setPadding(10,0,0,0);
		        
		        if(i++%2==0)
		        	row.setBackgroundResource(R.color.white);
		        
		       table.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) );
			}
	        }
	        else 
	        	Toast.makeText(getApplicationContext(), "There are no customers yet." , Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.queue, menu);
		return true;
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
