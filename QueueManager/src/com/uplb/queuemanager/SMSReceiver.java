package com.uplb.queuemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver
{
	private static final String LOG = "com.uplb.queuemanager";
	private DatabaseAdapter databaseAdapter;
	
    @Override
    public void onReceive(Context context, Intent intent) 
    {
    	//get the SMS message passed in
        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        String messageReceived = "";            
        if (bundle != null)
        {
            //retrieve the SMS message received
           Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];            
            for (int i=0; i<msgs.length; i++)

            {
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                messageReceived += msgs[i].getMessageBody().toString();
                messageReceived += "\n";        
            }

            //Get the Sender Phone Number
           String senderPhoneNumber=msgs[0].getOriginatingAddress();  

          
           databaseAdapter = new DatabaseAdapter(context);
           databaseAdapter.open();
           
           //REGEX (Fname Lname ENQUEUE or Fname Lname INQUIRE)
           Pattern pattern = Pattern.compile("([a-zA-Z]+\\s)+(ENQUEUE|INQUIRE|enqueue|inquire)\n");
			
		   String name = messageReceived.substring(0, messageReceived.lastIndexOf(" "));
		   String operation = messageReceived.substring(messageReceived.lastIndexOf(" ") + 1);
			
		   Matcher matcher = pattern.matcher(messageReceived);

		   boolean match = false;
           while (matcher.find()) {
                   match = true;
           }
           
           operation = operation.toLowerCase();
           if(match&&operation.equals("enqueue\n")){
                
           //get name from messageReceived
        	   
           //waiting time computation
           //kung average dapat ang iccount lang ay yung mga may service time -> ISQUEUED =0
           int average_service_time = 0;
		   int service_time_sum = databaseAdapter.getServiceTimeSum();
		   int custDone = databaseAdapter.retrieveCustomerDone();
           int queue_length = databaseAdapter.retrieveCustomerNumber();
           if(custDone!=0)
        	   average_service_time=service_time_sum/custDone;
           else
        	   average_service_time=10; //initial value: 10mins (how to determine)
           
           int expected_waiting_time = average_service_time*queue_length; 
           
           //store info to DB (check for duplicates, PK: phone_number)
           Calendar d = Calendar.getInstance();
   		   String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
	       time = time + "." + Integer.toString(d.get(Calendar.MILLISECOND));   
	        
		   databaseAdapter.insertCustomer(senderPhoneNumber,
											queue_length+1,
											time,
											expected_waiting_time,
											0,
											name,//name
											1); //queue_id
           
           //update table virtual queue (after successful insert)
           //QUEUE TABLE (di pa sure kung kailangan)
		   databaseAdapter.updateQueueLength(queue_length, queue_length+1);
           String mgt = databaseAdapter.getUserName();
           
           //customer number
           String total_customer_num = Integer.toString(queue_length);
           String estimated_waiting_time = Integer.toString(expected_waiting_time);
           
           //send confirmation message
           String confirmation_message = "Good day! You are now in the line. Currently, there are "+ total_customer_num +" customers to be served before you. Your estimated waiting time is " + estimated_waiting_time +" minutes. Thank you! -" +mgt+ " Management";
           
           //prompt regarding sent confirmation message
           try{
        	   SmsManager sms = SmsManager.getDefault(); 
        	   sms.sendTextMessage(senderPhoneNumber, null, confirmation_message, null, null);
        	   
        	   // Show the toast  like in above screen shot
        	   Toast.makeText(context, "Confirmation SMS Sent to " + senderPhoneNumber, Toast.LENGTH_LONG).show();
           }catch (Exception e) {
        	   Toast.makeText(context,"SMS failed, please try again.",Toast.LENGTH_LONG).show();
        	   e.printStackTrace();
           }
           
           
           
           abortBroadcast();
           }//end if
           else if(match&&operation.equals("inquire\n")){
        	   
        	   //waiting time computation
              // DatabaseAdapter databaseAdapter = new DatabaseAdapter(context);
              // databaseAdapter.open();
               //kung average dapat ang iccount lang ay yung mga may service time -> ISQUEUED =0
               int average_service_time = 0;
    		   int service_time_sum = databaseAdapter.getServiceTimeSum(); //assumption: service time/customer > 1 min
    		   int custDone = databaseAdapter.retrieveCustomerDone();
               int queue_length = databaseAdapter.retrieveCustomerNumber();
               if(custDone!=0)
            	   average_service_time=service_time_sum/custDone;
               else
            	   average_service_time=10; //initial value: 10mins (how to determine)
               
               int expected_waiting_time = average_service_time*queue_length; 
               String mgt = databaseAdapter.getUserName();
               //databaseAdapter.close();
               
               //customer number
               String total_customer_num = Integer.toString(queue_length);
               String estimated_waiting_time = Integer.toString(expected_waiting_time);
               //databaseAdapter.updateWaitTime(senderPhoneNumber, expected_waiting_time);
               //send confirmation message
               String info_message = "Good day! Currently, there are "+ total_customer_num +" customers on the line. Estimated waiting time is " + estimated_waiting_time +" minutes. If you want to be enqueued just reply with this format: FIRST_NAME [space] LAST _NAME [space] ENQUEUE. Thank you! -" +mgt+ " Management";
               
        	   //prompt regarding sent information message
               try{
            	   SmsManager sms = SmsManager.getDefault(); 
            	   sms.sendTextMessage(senderPhoneNumber, null, info_message, null, null);
            	   
            	   // Show the toast  like in above screen shot
            	   Toast.makeText(context, "Information SMS Sent to " + senderPhoneNumber + "with waiting time " + expected_waiting_time, Toast.LENGTH_LONG).show();
            	   //Toast.makeText(context, info_message, Toast.LENGTH_LONG).show();
               }catch (Exception e) {
            	   Toast.makeText(context,"SMS failed, please try again.",Toast.LENGTH_LONG).show();
            	   e.printStackTrace();
               }
               abortBroadcast();
           }
           else
        	   Log.i(LOG,"Wrong SMS format!");
           
           databaseAdapter.close();
           
           //Intent i = getIntent();
           //finish();
           //startActivity(i);
       }                         
    }
}
