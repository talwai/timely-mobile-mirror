package dartmouth.timely;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapClickListener{

	// API for calendar
	final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/calendar";

	// Test API for now; to be replaced
	final String TIMELY_API_URL = "http://pure-retreat-6606.herokuapp.com/api/v1/locations";

	// Mapquest API
	final String MAPQUEST_API = "http://open.mapquestapi.com/nominatim/v1/reverse.php?format=json";
	
	// Google Maps API lat/lng for Hanover
	private GoogleMap map;
	static final LatLng DARTMOUTH_COORD = new LatLng(43.704446,-72.288697);
	static final int ZOOM_LEVEL = 17;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		AccountManager accountManager = AccountManager.get(MainActivity.this);    	
		Account[] accounts = accountManager.getAccountsByType("com.google");

		Account account = accounts[0];
		//Log.d("Timely","started");


		accountManager.invalidateAuthToken(account.type, accountManager.KEY_AUTHTOKEN);

		accountManager.getAuthToken(account,
				"oauth2:https://www.googleapis.com/auth/calendar", null,
				this,
				new AccountManagerCallback<Bundle>(){ 
			public void run(AccountManagerFuture<Bundle> future) {
				try{
					Bundle bundle = future.getResult();
					if(bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
						String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
						sendLocation (token);
					}else {

					}
				}
				catch(Exception e){
					e.printStackTrace();
				}  
			}
		}, null);        

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Google Maps API v2 dance
		int playStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		
		if (playStatus == ConnectionResult.SUCCESS){
			map = ((SupportMapFragment)  getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap(); // generate the map
			
			// set camera to Dartmouth
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(DARTMOUTH_COORD, ZOOM_LEVEL));
			map.setOnMapClickListener(this);
			
		} else {
			Toast.makeText(getApplicationContext(), "No Google Play found", Toast.LENGTH_LONG).show();
		}
	}

	public void sendLocation(String accessToken) {    	
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (location != null) {
			// LatLng object and Strings of coordinates
			String latitude = Double.toString(location.getLatitude());
			String longitude = Double.toString(location.getLongitude());
			
			// Post to API with latitude and longitude
			new NetworkPost().execute(TIMELY_API_URL, latitude,longitude);

			// GET request to MapQuest with latitude longitude
			String url = MAPQUEST_API+"&lat="+latitude+"&lon="+longitude;
			
//			LatLng user_coord = new LatLng(location.getLatitude(), location.getLongitude());
//			new NetworkGet().execute(url, user_coord);
			new NetworkGet().execute(url);

			// test send notification
			noteLatLong(latitude, longitude);
		}

		final LocationListener locationListener = new LocationListener() {
			// Once location has changed
			public void onLocationChanged(Location location) {
				String latitude = Double.toString(location.getLatitude());
				String longitude = Double.toString(location.getLongitude());

				// POST to API with latitude and longitude
				new NetworkPost().execute(TIMELY_API_URL, latitude, longitude);

				// GET request to MapQuest with latitude longitude
				String url = MAPQUEST_API+"&lat="+latitude+"&lon="+longitude;
				
				// creates a marker at current user location // 
//				LatLng user_coord = new LatLng(location.getLatitude(), location.getLongitude());
//				new NetworkGet().execute(url, user_coord);
				new NetworkGet().execute(url);

				// test send notification
				noteLatLong(latitude, longitude);
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub

			}
		};

		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);

		//new JsonFactory();

		//service.accessKey = "zwe7TX17stsEOnB7FeAqQN7E";
		//service.setApplicationName("Timely");

	}


	// POST request to the Timely API
	private class NetworkPost extends AsyncTask<String, Void, HttpResponse>  {
		@Override
		protected HttpResponse doInBackground(String... params) {
			String link = params[0];

			HttpPost httppost = new HttpPost(link);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("latitude", params[1]));
			nameValuePairs.add(new BasicNameValuePair("longitude", params[2]));

			try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}            

			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			try {
				return client.execute(httppost);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} finally {
				client.close();
			}
		}

		@Override
		protected void onPostExecute(HttpResponse result) {
			if (result != null) {
				String location;
				try {
					location = EntityUtils.toString(result.getEntity());
					System.out.println ("Location from server " + location);
					//	            	TextView view = (TextView) findViewById(R.id.text);
					//	            	view.setText(location);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	// wrapper class
	public class Wrapper {
		public HttpResponse result;
		public LatLng point;
	}
	
	// GET request for the Mapquest API
	// Wrapper class enables multiple type parameters
	private class NetworkGet extends AsyncTask<Object, Void, Wrapper>  {

		@Override
		// use Object type for different type parameters
		protected Wrapper doInBackground(Object... params) {
			Wrapper p = new Wrapper(); // Wrapper class is returned to OnPostExecute
			String url = (String) params[0];
			
			// Check if the user clicked the map
			// If so, a LatLng point object will be passed
			try {
				p.point = (LatLng) params[1];
			} catch (Exception e){
				// not a map click, continue
			}

			// Set up the GET request
			HttpClient client = new DefaultHttpClient();  
			String getURL = url;
			HttpGet get = new HttpGet(getURL);

			try {
				p.result = client.execute(get); 
				return p;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} 
		}


		@Override
		// after GET request finished
		protected void onPostExecute(Wrapper p) {
			HttpEntity resEntityGet = p.result.getEntity();  

			if (resEntityGet != null) {  
				String response;

				try {
					response = EntityUtils.toString(resEntityGet); // response JSON 

					// Set full JSON text (enable this and uncomment in main.xml to view full JSON)
					// Or just use curl on the MAPQUEST_API URL
//					TextView view = (TextView) findViewById(R.id.text);
//					view.setText(response);
					/////// end ///////

					// Parse JSON
					JSONObject jObject = new JSONObject(response);
					String display_name_obj = jObject.getString("display_name");
					
					String[] display_name_arr = display_name_obj.split(",");
					
					// Check if user clicked on map
					if (p.point != null){
						// Create marker at user's point
						Marker usermarker = map.addMarker(new MarkerOptions().position(p.point)
								.title(display_name_arr[0])
								.snippet("Snippet"));
						usermarker.showInfoWindow(); // display marker title automatically
	
						map.animateCamera(CameraUpdateFactory.newLatLng(p.point));
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}
		}
	}

	// Pops a notification for user
	private void noteLatLong(String latitude, String longitude){
		Intent notificationIntent = new Intent(this, NotificationReceiverActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this,
				0, notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		System.out.println("Notification portion reached");
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Notification.Builder builder = new Notification.Builder(this)
		.setContentTitle("Timely")
		.setContentText("Lat: "+latitude+", Long: "+longitude);

		builder.setContentIntent(contentIntent)
		.setSmallIcon(R.drawable.timely)
		.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.timely));
		//                    .setTicker(res.getString("Test ticker"))
		//                    .setWhen(System.currentTimeMillis())
		//                    .setAutoCancel(true)
		Notification n = builder.build();

		final int YOUR_NOTIF_ID = 0;
		nm.notify(YOUR_NOTIF_ID, n);
	}

	@Override
	/* 
	 * Called when user clicks on the Google Map
	 * Create a marker at that point, move camera
	 * @see com.google.android.gms.maps.GoogleMap.OnMapClickListener#onMapClick(com.google.android.gms.maps.model.LatLng)
	 */
	public void onMapClick(LatLng point) {
		String url = MAPQUEST_API+"&lat="+point.latitude+"&lon="+point.longitude;
		
		// Send lat/lng in parameters to draw a marker on the map with the title 
		new NetworkGet().execute(url, point); // reverse-geocode
	}
}