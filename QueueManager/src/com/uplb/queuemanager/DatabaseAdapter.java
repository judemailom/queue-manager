package com.uplb.queuemanager;


import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;


public class DatabaseAdapter 
{
		
		private static final String LOG = "com.uplb.queuemanager";
		static final String DATABASE_NAME = "queuemanager.db";
        static final int DATABASE_VERSION = 2;
        public static final int NAME_COLUMN = 1;
        
        final Calendar c = Calendar.getInstance();
	    int yy = c.get(Calendar.YEAR);
	    int mm = c.get(Calendar.MONTH);
	    int dd = c.get(Calendar.DAY_OF_MONTH);

  	    //set current date into textview
  	  	String date = (new StringBuilder()
  	    			.append(mm+1).append("-")
  	    			.append(dd).append("-").append(yy)).toString();
    
        //COL (PHONE_NUMBER, QUEUE_POSTITION, ARRIVAL_TIME(of message), WAITING_TIME(estimated waiting time from message to being served), TOTAL_SERVICE_TIME(computed waiting time, after being served, used for computation of others' waiting time), CUSTOMER_NAME, START_SERVICE_TIME, END_SERVICE_TIME, DATE, FKEY_ID)
        static final String DATABASE_CREATE_CUSTOMER = "create table if not exists CUSTOMER"+
                                     "(_ID integer PRIMARY KEY autoincrement,"+ 
                                     "PHONE_NUMBER text not null,QUEUE_POSITION integer," +
                                     "ARRIVAL_TIME text, WAITING_TIME integer," +
                                     "TOTAL_SERVICE_TIME integer, CUSTOMER_NAME text," +
                                     "START_SERVICE_TIME text, END_SERVICE_TIME text,DATE integer,"+
                                     "ISQUEUED integer, QUEUE_ID INTEGER, FOREIGN KEY(QUEUE_ID) REFERENCES QUEUE(_ID)); ";
        
        //COL (QUEUE_LENGTH, AVERAGE_SERVICE_TIME, DATE, FKEY_ID)
        static final String DATABASE_CREATE_QUEUE = "create table if not exists QUEUE"+
				                "(_ID integer PRIMARY KEY autoincrement,"+ 
				                "QUEUE_LENGTH integer, AVERAGE_SERVICE_TIME integer, DATE integer,"+
				                "USER_ID INTEGER, FOREIGN KEY(USER_ID) REFERENCES USER(ID)); ";
		
		//COL (USER_NAME,PASSWORD,COMP_NAME)		                
		static final String DATABASE_CREATE_USER = "create table if not exists USER" +
								"(_ID integer PRIMARY KEY autoincrement,"+
								"PASSWORD text, COMP_NAME text, COMP_ADDRESS text, COMP_CONTACT integer not null);";
		
								
        
        public  SQLiteDatabase db; 			//Variable to hold the database instance
        private final Context context; 		//Context of the application using the database.
        private DatabaseHelper dbHelper;	//Database open/upgrade helper
        
        /**
         * Instantiates a new database adapter.
         *
         * @param _context the _context
         */
        public  DatabaseAdapter(Context _context) 
        {
        		this.context = _context;
                this.dbHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
          * Opens the database.
          *
          * @return the database adapter
          * @throws SQLException the sQL exception
          */
         public  DatabaseAdapter open() throws SQLException 
        {
        		db = this.dbHelper.getWritableDatabase();
                return this;
        }
    
        /**
         * Closes the database.
         */
        public void close() 
        {
        	dbHelper.close();
        }
 
        /**
         * Gets the database instance.
         *
         * @return the database instance
         */
        public  SQLiteDatabase getDatabaseInstance()
        {
                return db;
        }
        
        /**
         * Insert customer information to customer table.
         *
         * @param phone_number the phone number of the customer
         * @param queue_position the queue position of the customer 
         * @param arrival_time the arrival time of the customer
         * @param waiting_time the waiting time of the customer
         * @param total_service_time the total service time of the customer
         * @param name the name of the customer
         * @param queue_id the queue_id
         */
        public void insertCustomer(String phone_number,
									int queue_position, 
									String arrival_time, 
									int waiting_time, 
									int total_service_time, 
									String name, 
									int queue_id)
        {
        	Log.i(LOG,"INSERTING TO TABLE CUSTOMER");
            ContentValues newValues = new ContentValues();
            newValues.put("PHONE_NUMBER", phone_number);
            newValues.put("QUEUE_POSITION",queue_position);
            newValues.put("ARRIVAL_TIME", arrival_time);
            newValues.put("WAITING_TIME",waiting_time);
            newValues.put("TOTAL_SERVICE_TIME", total_service_time);
            newValues.put("CUSTOMER_NAME",name);
            newValues.put("START_SERVICE_TIME", "00:00:00.000");
            newValues.put("END_SERVICE_TIME","00:00:00.000");
            newValues.put("DATE",date);
            newValues.put("ISQUEUED",1);
            newValues.put("QUEUE_ID",queue_id);
           
           
            db.insert("CUSTOMER", null, newValues);
            Toast.makeText(context, "Customer Info Saved", Toast.LENGTH_LONG).show();
       
   
         }
        
        /**
         * Update status of customer 0 - not in the queue, 1 - enqueued
         *
         * @param phone the phone number of the customer
         */
        public void updateStatus(String phone){
        	Log.i(LOG,"UPDATING STATUS OF CUSTOMER = "+phone);
       	 	ContentValues updatedValues = new ContentValues();
            updatedValues.put("ISQUEUED", 0);
            
            String where="PHONE_NUMBER = ?";
            db.update("CUSTOMER",updatedValues, where, new String[]{phone});
        }
        
        /**
         * Update start time (transaction) of customer
         *
         * @param phone the phone number of the customer
         * @param start the start transaction time of the customer
         */
        public void updateStartTime(String phone, String start){
        	Log.i(LOG,"UPDATING TABLE CUSTOMER WITH START = "+start+" AND Phone = "+phone);
       	 	ContentValues updatedValues = new ContentValues();
            updatedValues.put("START_SERVICE_TIME", start);
            
            String where="PHONE_NUMBER = ?";
            db.update("CUSTOMER",updatedValues, where, new String[]{phone});
        }
        
        /**
         * Update end time (transaction) of customer
         *
         * @param phone the phone number of the customer
         * @param end the end transaction time of the customer
         */
        public void updateEndTime(String phone, String end){
        	Log.i(LOG,"UPDATING TABLE CUSTOMER WITH END = "+end+" AND Phone = "+phone);
       	 	ContentValues updatedValues = new ContentValues();
            updatedValues.put("END_SERVICE_TIME", end);
            
            String where="PHONE_NUMBER = ?";
            db.update("CUSTOMER",updatedValues, where, new String[]{phone});
        }
        
        /**
         * Update service time = end-start of customer
         *
         * @param phone the phone number of the customer
         * @param service_time the service time of the customer
         */
        public void updateServiceTime(String phone, int service_time)
        {
        	Log.i(LOG,"UPDATING TABLE CUSTOMER: WITH VALUE OF ST= " +service_time+" AND PHONE_NUMBER = "+phone);
       	    ContentValues updatedValues = new ContentValues();
            updatedValues.put("TOTAL_SERVICE_TIME", service_time);
            
            String where="PHONE_NUMBER = ?";
            db.update("CUSTOMER",updatedValues, where, new String[]{phone});
          
        } 
        
        /**
         * Updates estimated waiting time of customers who are not yet served
         *
         * @param phone the phone number of the customer
         * @param wait_time the wait time of the customer
         */
        public void updateWaitTime(String phone, int wait_time)
        {
        	Log.i(LOG,"UPDATING TABLE CUSTOMER: WITH VALUE OF ST= " +wait_time+" AND PHONE_NUMBER = "+phone);
       	    ContentValues updatedValues = new ContentValues();
            updatedValues.put("WAITING_TIME", wait_time);
            
            String where="PHONE_NUMBER = ?";
            db.update("CUSTOMER",updatedValues, where, new String[]{phone});
          
        }        
        
        
        /**
         * Gets the start (transaction) time of the customer.
         *
         * @param customer the phone number of the customer
         * @return the start time
         */
        public String getStartTime(String customer){
        	String st="";
        	Log.i(LOG,"GETTING START TABLE CUSTOMER WITH ID = "+customer);
       	 	String query="SELECT START_SERVICE_TIME FROM CUSTOMER WHERE PHONE_NUMBER='"+customer+"';";
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
    			st = cursor.getString(cursor.getColumnIndex("START_SERVICE_TIME"));
    		//db.update("QUEUE",updatedValues, where, new String[]{Integer.toString(prev_average_service_time)});
        	cursor.close();
        	
            return st;
        }
        
        /**
         * Gets the end (transaction) time of the customer.
         *
         * @param customer the phone number of the customer
         * @return the end time
         */
        public String getEndTime(String customer){
        	String st="";
        	Log.i(LOG,"GETTING END TABLE CUSTOMER WITH ID = "+customer);
       	 	String query="SELECT END_SERVICE_TIME FROM CUSTOMER WHERE PHONE_NUMBER='"+customer+"';";
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
    			st = cursor.getString(cursor.getColumnIndex("END_SERVICE_TIME"));
    		cursor.close();
        	
            return st;
        }
    	
        /**
         * Gets the customer phone number.
         *
         * @param id the queue position of the customer
         * @return the customer phone number
         */
        public String getCustomerPhone(int id){	
    		String phone="";
    		String query="SELECT PHONE_NUMBER FROM CUSTOMER WHERE QUEUE_POSITION="+id+";";
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
    			phone = cursor.getString(cursor.getColumnIndex("PHONE_NUMBER"));
    		cursor.close();
        	
            return phone;
        }
        
        /**
         * Gets all customers.
         *
         * @return all customers
         */
        public ArrayList<String> getAllCustomers() {
            ArrayList<String> yourStringValues = new ArrayList<String>();
            Cursor result = db.rawQuery("SELECT PHONE_NUMBER FROM CUSTOMER WHERE ISQUEUED=1;",null);

            if (result.moveToFirst()) {
                do {
                    yourStringValues.add(result.getString(result.getColumnIndex("PHONE_NUMBER")));
                } while (result.moveToNext());
            } else {
                return null;
            }
            return yourStringValues;
        }
        
        /**
         * Gets all customers with specific date of transaction.
         *
         * @param date is the date of transaction
         * @return all customers
         */
        public ArrayList<String> getAllCustomersByDate(String date) {
            ArrayList<String> yourStringValues = new ArrayList<String>();
            Cursor result = db.rawQuery("SELECT PHONE_NUMBER FROM CUSTOMER WHERE DATE='"+date+"';",null);

            if (result.moveToFirst()) {
                do {
                    yourStringValues.add(result.getString(result.getColumnIndex("PHONE_NUMBER")));
                } while (result.moveToNext());
            } else {
                return null;
            }
            return yourStringValues;
        }
        
        /**
         * Gets the waiting time.
         *
         * @param phone_number the phone number of the customer
         * @return the waiting time
         */
        public String getWaitingTime(String phone_number){
    		String time="";
    		
    		String query="SELECT WAITING_TIME FROM CUSTOMER WHERE PHONE_NUMBER='"+phone_number+"';";
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
    			time = cursor.getString(cursor.getColumnIndex("WAITING_TIME"));
    		cursor.close();
        	
            return time;
        }
        
        /**
         * Retrieve total number of customers enqueued.
         *
         * @return the total number of customers enqueued
         */
        public int retrieveCustomerNumber(){
        	int n=0;
        	
        	String query="SELECT COUNT(*) FROM CUSTOMER WHERE ISQUEUED=1;"; 
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
    			n = Integer.parseInt(cursor.getString(0));
        	cursor.close();
        	
        	return n;
        }
        
        /**
         * Retrieve total number of customers done.
         *
         * @return the number of customers done
         */
        public int retrieveCustomerDone(){
        	int n=0;
        	
        	String query="SELECT COUNT(*) FROM CUSTOMER WHERE ISQUEUED=0;"; 
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
    			n = Integer.parseInt(cursor.getString(0));
        	cursor.close();
        	
        	return n;
        }
           
        /**
         * Delete customer. (not yet used)
         *
         * @param phone_number the phone_number
         */
        public void deleteCustomer(String phone_number)
        {
           String where="PHONE_NUMBER=?";
           
           db.delete("CUSTOMER", where, new String[]{phone_number}) ;
           Toast.makeText(context, "Customer Transaction Ended", Toast.LENGTH_LONG).show();
      
        }
       
        /**
         * Updates queue position of the customers.
         *
         * @param phone the phone number of the customer
         * @param queue_position the queue position
         */
        public void updateQueuePosition(String phone, int queue_position)
        {
            ContentValues updatedValues = new ContentValues();
            updatedValues.put("QUEUE_POSITION", queue_position);
            
            String where="PHONE_NUMBER = ?";
            db.update("CUSTOMER",updatedValues, where, new String[]{phone});
          
        }
        
        /**
         * Gets the queue position.
         *
         * @param phone the phone number
         * @return the queue position
         */
        public int getQueuePosn(String phone){
    		int et=0;
    		
    		Log.i(LOG,"GETTING Q POSN TABLE CUSTOMER WITH phone = "+phone);
       	 	String query="SELECT QUEUE_POSITION FROM CUSTOMER WHERE PHONE_NUMBER='"+phone+"';";
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
    			et = Integer.parseInt(cursor.getString(cursor.getColumnIndex("QUEUE_POSITION")));
    		cursor.close();
        	
            return et;
        }
        
        /**
         * Gets the name of the customer.
         *
         * @param phone the phone number
         * @return the name
         */
        public String getName(String phone){
    		
        	String name="";
    		
    		String query="SELECT CUSTOMER_NAME FROM CUSTOMER WHERE PHONE_NUMBER='"+phone+"';";
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
    			name = cursor.getString(cursor.getColumnIndex("CUSTOMER_NAME"));
    		cursor.close();
        	
            return name;
        }


        
        /**
         * Gets the sum of all service time of customers done in the queue.
         *
         * @return the service time sum
         */
        public int getServiceTimeSum()
        {
        	int tst = 0;
            Cursor cursor=db.query("CUSTOMER", null, "ISQUEUED=0", null, null, null, "END_SERVICE_TIME DESC","2");
            if(cursor.getCount()<1)
                return tst;
            
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            	tst += cursor.getInt(cursor.getColumnIndex("TOTAL_SERVICE_TIME"));
            
            Log.i(LOG,"TOTAL SERVICE TIME = "+tst);
       	 
            return tst;
        }
        
        /**
         * Insert virtual queue. (not yet used)
         *
         * @param queue_length the queue_length
         * @param average_service_time the average_service_time
         * @param user_id the user_id
         */
        public void insertVirtualQueue(int queue_length, int average_service_time, int user_id)
        {
          
            ContentValues newValues = new ContentValues();
            newValues.put("QUEUE_LENGTH", queue_length);
            newValues.put("AVERAGE_SERVICE_TIME",average_service_time);
            newValues.put("DATE",date);
            newValues.put("USER_ID",user_id);

            db.insert("QUEUE", null, newValues);
              
        }
        
        /**
         * Update queue length. (not yet used)
         *
         * @param prev_queue_length the prev_queue_length
         * @param queue_length the queue_length
         */
        public void  updateQueueLength(int prev_queue_length, int queue_length)
        {
            ContentValues updatedValues = new ContentValues();
            updatedValues.put("QUEUE_LENGTH", queue_length);
            
            String where="QUEUE_LENGTH = ?";
            db.update("QUEUE",updatedValues, where, new String[]{Integer.toString(prev_queue_length)});
          
        }

        /**
         * Update average service time. (not yet used)
         *
         * @param prev_average_service_time the prev_average_service_time
         * @param average_service_time the average_service_time
         */
        public void updateAverageServiceTime(int prev_average_service_time, int average_service_time)
        {
        	 Log.i(LOG,"UPDATING TABLE QUEUE: AST");
        	 ContentValues updatedValues = new ContentValues();
             updatedValues.put("AVERAGE_SERVICE_TIME", average_service_time);
             
             String where="AVERAGE_SERVICE_TIME = ?";
             db.update("QUEUE",updatedValues, where, new String[]{Integer.toString(prev_average_service_time)});
          
        }
        
        /**
         * Retrieve average service time. (not yet used)
         *
         * @return the int
         */
        public int retrieveAverageServiceTime(){
        	Log.i(LOG,"RETRIEVING AST");
       	 	int ast=0;
        	String query="SELECT AVERAGE_SERVICE_TIME FROM QUEUE;";
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
    			ast = Integer.parseInt(cursor.getString(cursor.getColumnIndex("AVERAGE_SERVICE_TIME")));
    		
    		cursor.close();
        	
            return ast;
        }
        
        /**
         * Retrieve queue length. (not yet used)
         *
         * @return the int
         */
        public int retrieveQueueLength(){
        	
        	Log.i(LOG,"RETRIEVING QL");
       	 	int ql=0;
        	String query="SELECT QUEUE_LENGTH FROM QUEUE;";
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
    			ql = Integer.parseInt(cursor.getString(cursor.getColumnIndex("QUEUE_LENGTH")));
    		
    		cursor.close();
        	
            return ql;
        }
		
		/**
		 * Insert user information to database.
		 *
		 * @param comp_add the company address
		 * @param password the password
		 * @param comp_name the company name
		 * @param comp_contact the company contact number
		 */
		public void insertUser(String comp_add, String password, String comp_name, String comp_contact)
        {
          
            ContentValues newValues = new ContentValues();
            newValues.put("COMP_ADDRESS", comp_add);
            newValues.put("PASSWORD", password);
            newValues.put("COMP_NAME", comp_name);
            newValues.put("COMP_CONTACT", comp_contact);
			
            db.insert("USER", null, newValues);
        }
		
		/**
		 * Gets the all users.
		 *
		 * @return the all user password
		 */
		public ArrayList<String> getAllUsers() {
            ArrayList<String> yourStringValues = new ArrayList<String>();
            Cursor result = db.rawQuery("SELECT PASSWORD FROM USER;",null);

            if (result.moveToFirst()) {
                do {
                    yourStringValues.add(result.getString(result.getColumnIndex("PASSWORD")));
                } while (result.moveToNext());
            } else {
                return null;
            }
            return yourStringValues;
        }
		
		/**
		 * Gets the user id.
		 *
		 * @param user the user password
		 * @return the user id
		 */
		public int getUserID(String user){
    		
			int id=-1;
			
    		String query="SELECT _ID FROM USER WHERE PASSWORD=\""+user+"\";";
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
   			id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("_ID")));
    		cursor.close();
        	
            return id;
        }
		
		/**
		 * Gets the user company name.
		 *
		 * @return the user company name
		 */
		public String getUserName(){
			
			String name="";
			
			String query="SELECT COMP_NAME FROM USER";
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
   			name = cursor.getString(cursor.getColumnIndex("COMP_NAME"));
    		cursor.close();
        	
        	return name;
		}
		
		/**
		 * Gets the password.
		 *
		 * @return the password
		 */
		public String getPassword(){
			
			String pass="";
			
			String query="SELECT PASSWORD FROM USER";
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
   			pass = cursor.getString(cursor.getColumnIndex("PASSWORD"));
    		cursor.close();
        	
        	return pass;
		}
		
		/**
		 * Gets the company address.
		 *
		 * @return the company address
		 */
		public String getAddress(){
			
			String add="";
			
			String query="SELECT COMP_ADDRESS FROM USER";
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
    		add = cursor.getString(cursor.getColumnIndex("COMP_ADDRESS"));
    		cursor.close();
        	
        	return add;
		}
		
		/**
		 * Gets the company contact number.
		 *
		 * @return the company contact number
		 */
		public String getContact(){
			
			String contact="";
			
			String query="SELECT COMP_CONTACT FROM USER";
    		Cursor cursor = db.rawQuery(query,null);
    		if(cursor.moveToFirst())
    		contact = cursor.getString(cursor.getColumnIndex("COMP_CONTACT"));
    		cursor.close();
        	
        	return contact;
		}

		/**
		 * Update user.
		 *
		 * @param prev_name the previous name
		 * @param curr_name the new name
		 * @param pass the new password
		 * @param add the new address
		 * @param contact the new contact number
		 */
		public void updateUser(String prev_name, String curr_name, String pass, String add, String contact){
        	ContentValues updatedValues = new ContentValues();
            updatedValues.put("COMP_NAME", curr_name);
            updatedValues.put("PASSWORD", pass);
            updatedValues.put("COMP_ADDRESS", add);
            updatedValues.put("COMP_CONTACT", contact);
            
            String where="COMP_NAME = ?";
            db.update("USER",updatedValues, where, new String[]{prev_name});
        }
        
}
