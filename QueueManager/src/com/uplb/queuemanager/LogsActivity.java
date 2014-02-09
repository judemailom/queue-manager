package com.uplb.queuemanager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

public class LogsActivity extends Activity {
	
	private TableLayout table;
	private TextView date;
	private ArrayList<String> customerList;
	private DatabaseAdapter databaseAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logs);
		
		date = (TextView)this.findViewById(R.id.log_date);
		table = (TableLayout)this.findViewById(R.id.table_log);
		customerList = new ArrayList<String>();
		
		final Calendar c = Calendar.getInstance();
	    int yy = c.get(Calendar.YEAR);
	    int mm = c.get(Calendar.MONTH);
	    int dd = c.get(Calendar.DAY_OF_MONTH);

	    // set current date into textview
	    //date.setText(new StringBuilder()
	    //			.append(mm+1).append("-")
	    //			.append(dd).append("-").append(yy));
	    
	    String date_now = ""+(mm+1)+"-"+dd+"-"+yy;
		date.setText("Transaction logs for "+date_now); 
		
		databaseAdapter = new DatabaseAdapter(getApplicationContext());
        databaseAdapter.open();
        customerList=databaseAdapter.getAllCustomersByDate(date_now); //date_now"2-3-2014"
        //customerList=databaseAdapter.getAllCustomers();
        databaseAdapter.close();
        
        if(customerList!=null){
	        for(final String customer: customerList){
				TableRow row = new TableRow(this);
				TextView name = new TextView(this);
				TextView number = new TextView(this);
				TextView start = new TextView(this);
				TextView end = new TextView(this);
				TextView remarks = new TextView(this);
				
				//get waiting_time wrt phone_number
				databaseAdapter.open();
		        
		        name.setText(databaseAdapter.getName(customer));
		        number.setText(customer);
		        start.setText(databaseAdapter.getStartTime(customer));
		        end.setText(databaseAdapter.getEndTime(customer));
		        int remark = databaseAdapter.getRemarks(customer);
		        databaseAdapter.close();
		        
		        String remark_val = "";
		        if(remark == 0)
		        	remark_val = "SERVED";
		        else if(remark==-1)
		        	remark_val = "CANCELLED";
		        
		        remarks.setText(remark_val);
		        
		        name.setTextSize(TypedValue.COMPLEX_UNIT_PX,14);
		        number.setTextSize(TypedValue.COMPLEX_UNIT_PX,14);
		        start.setTextSize(TypedValue.COMPLEX_UNIT_PX,14);
		        end.setTextSize(TypedValue.COMPLEX_UNIT_PX,14);
		        remarks.setTextSize(TypedValue.COMPLEX_UNIT_PX,14);
		        
		        name.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		        number.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		        start.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		        end.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		        remarks.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		        	
		      
		        name.setGravity(Gravity.CENTER);
		        number.setGravity(Gravity.CENTER);
		        start.setGravity(Gravity.CENTER);
		        end.setGravity(Gravity.CENTER);
		        remarks.setGravity(Gravity.CENTER);
		        
		        name.setPadding(15,0,25,0);
		        number.setPadding(0,0,25,0);
		        start.setPadding(0,0,25,0);
		        end.setPadding(0,0,25,0);
		        remarks.setPadding(0,0,15,0);
		        
		        row.addView(name);
		        row.addView(number);
		        row.addView(start);
		        row.addView(end);
		        row.addView(remarks);
		        row.setPadding(5,5,5,5);
		        
		        
		       table.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) );
				}
	        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.logs, menu);
		return true;
	}
	
	public void export_method(View view) throws IOException{
		
		String FILENAME = "hello_file";
		String string = "hello world!";

		FileOutputStream fos;
		fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
		fos.write(string.getBytes());
		fos.close();

		
	}
	

}
