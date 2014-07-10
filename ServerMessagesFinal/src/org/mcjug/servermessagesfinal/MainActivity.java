package org.mcjug.servermessagesfinal;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MainActivity extends Activity {

	static final String TAG = "MainActivity";
	private DownloadServerMessage downloadService = null;
	private BroadcastReceiver receiver;	
	// Intent intent = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.v(TAG, "onCreate setContentView");
	}

	@Override
	  protected void onResume() {
	    super.onResume();
	    Intent intent= new Intent(this, DownloadServerMessage.class);
	    bindService(intent, downloadServiceConnection, Context.BIND_AUTO_CREATE);
	    Log.v(TAG, "onResume bindService");
	  }

	  @Override
	  protected void onPause() {
	    super.onPause();
	    unbindService(downloadServiceConnection);
	    handler.removeCallbacks(runnableTicker);
	    Log.v(TAG, "onPause unbindService");
	  }

	  private ServiceConnection downloadServiceConnection = new ServiceConnection() {

		  public void onServiceConnected(ComponentName className, IBinder binder) {
			  DownloadServerMessage.ServiceBinder bndr = (DownloadServerMessage.ServiceBinder) binder;
			  downloadService = bndr.getService();
			  Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
			  Log.v(TAG, "onServiceConnected");
		  }

		  public void onServiceDisconnected(ComponentName className) {
			  downloadService = null;
			  Log.v(TAG, "onServiceDisconnected");
		  }
	  };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onButtonStartClick(View v) {
        if (receiver != null) {
        	Log.v(TAG, "Button Start Clicked while receiver is initialized");
        	addNote("Already Started");
        }
        else {
        	Log.v(TAG, "Button Start Clicked, initializing receiver");
        	registerReceiver(receiver, new IntentFilter(DownloadServerMessage.NOTIFICATION));
        	Toast.makeText(getApplicationContext(), "STARTING", Toast.LENGTH_SHORT).show();
        }
        
        handler.postDelayed (runnableTicker, 1000);
	}

		// 	First we need a Handler that starts the Runnable after 100ms

		private Handler handler = new Handler();
		
		// And we also need the Runnable for the Handler
		private Runnable runnableTicker = new Runnable() {
		   @Override
		   public void run() {
		      /* do what you need to do */
			   updateTicker();
		      /* and here comes the "trick" */
		      handler.postDelayed(this, 10000);
		   }
		};

		private int tickerNumber = 0;

		private void updateTicker () {
			tickerNumber++;
			TextView mText = (TextView) findViewById(R.id.textViewTicker);
			mText.setText(tickerNumber + ". Updated\n");
		}

	
	public void onButtonStopClick(View v) {
        if (receiver != null) {
            Toast.makeText(getApplicationContext(), "STOP", Toast.LENGTH_SHORT).show();
            Log.v(TAG, "Button Stop Clicked, unregister receiver");
            unregisterReceiver(receiver);  
            receiver = null;
        }
        else {
        	Log.v(TAG, "Button Stop Clicked while receiver is not initialized");
        	addNote("Already Stopped");
        }
        
        handler.removeCallbacks(runnableTicker);
	}


	public void onButtonDownloadClick(View v) {
		Log.v(TAG, "Button Download clicked");

		if (downloadService != null) {
			int resultCode = downloadService.getResult();
			if (resultCode  == RESULT_OK) {
				String stringJson = downloadService.getLoadedString();
				if (stringJson !="") {
					processServerMessage(stringJson);
					if (receivedServerMessage != null) {
						Log.v(TAG, "ServerMessage created from gson.fromJson: " + receivedServerMessage.toString());
						addNote("Result: " + receivedServerMessage.toString());
					}
					else
						addNote("ServerMessage is empty");
				}
				else
					addNote("JSON is empty");	
			}
			else 
				addNote("Result not OK");
		}
		else
			addNote("Service is disconnected");
	}
	
	static final String jsonSource = "{\"meta\": {\"limit\": 20, \"next\": null, \"offset\": 0, \"previous\": null, \"total_count\": 1}, \"objects\": [{\"created_date\": \"2014-05-31T10:28:58.224990\", \"id\": 2, \"is_active\": true, \"message\": \"ACTIVE MESSAGE 2\", \"resource_uri\": \"/meetingfinder/api/v1/server_message/2\", \"short_message\": \"active message 2\", \"updated_date\": \"2014-05-31T10:28:58.225022\"}, {\"created_date\": \"2014-06-01T10:28:58.224990\", \"id\": 3, \"is_active\": true, \"message\": \"ACTIVE 3\", \"resource_uri\": \"/meetingfinder/api/v1/server_message/3\", \"short_message\": \"active 3\", \"updated_date\": \"2014-06-011T10:28:58.225022\"}]}";
	ServerMessage receivedServerMessage = null;

	public void onButtonInfoClick(View v) {
        Log.v(TAG, "Button Info Clicked");
        processServerMessage(jsonSource);
		if (receivedServerMessage != null) {
			Log.v(TAG, "ServerMessage created from gson.fromJson: " + receivedServerMessage.toString());
			addNote("Result: " + receivedServerMessage.toString());
		}
	}
	
	private int noteNumber = 0;

	public void addNote (String note) {
		noteNumber++;
		TextView mText = (TextView) findViewById(R.id.textViewNote);
		mText.setText(noteNumber + ". " + note + "\n");
		
		/*
		int maxLines = R.integer.TextMaxLines;
		if (noteNumber % maxLines == (maxLines-1)) {
			mText.setText("");
		}
		mText.append(noteNumber + ". " + note + "\n");
		*/
	}
	
	public void processServerMessage (String messageJson) {
		if (messageJson.length() > 0) {
			// Log.v(TAG, "processing " + messageJson);
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			Gson gson = gsonBuilder.create();
			receivedServerMessage = gson.fromJson(messageJson, ServerMessage.class);
			return;
		}
		receivedServerMessage = null;
	}
	
		
}
