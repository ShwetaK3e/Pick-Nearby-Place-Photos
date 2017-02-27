package com.shwetak3e.nearby;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    int PLACE_PICKER_REQUEST=1;
    PlacePicker.IntentBuilder builder=new PlacePicker.IntentBuilder();
    GoogleApiClient mGoogleApiClient;

    Button setLoc;
    LinearLayout result;
    TextView placeName;
    TextView ratings;
    ImageView placeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient=new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this,this)
                .build();

        placeName=(TextView)findViewById(R.id.name);
        placeImage=(ImageView)findViewById(R.id.image) ;
        ratings=(TextView)findViewById(R.id.rating);

        result=(LinearLayout) findViewById(R.id.result);
        result.setVisibility(View.INVISIBLE);

        setLoc=(Button)findViewById(R.id.setLocation) ;
        setLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocation();
            }
        });
    }

    private void setLocation() {
        try {
            startActivityForResult(builder.build(this),PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==PLACE_PICKER_REQUEST ){
            if(resultCode==RESULT_OK){
                result.setVisibility(View.VISIBLE);
                Place place = PlacePicker.getPlace(this, data);
                String plceName = place.getName().toString();
                String plceRatings=String.valueOf(place.getRating()>0?place.getRating():0.0f);
                placeName.setText(plceName);
                ratings.setText(plceRatings);
                String placeId = place.getId();

                placePhotosAsync(placeId);

                Toast.makeText(this, "New Location Added", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Location Not Found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void placePhotosAsync(String placeId) {
        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, placeId)
                .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {


                    @Override
                    public void onResult(PlacePhotoMetadataResult photos) {
                        if (!photos.getStatus().isSuccess()) {
                            Log.i("TAG","failed1");
                            placeImage.setImageDrawable(getResources().getDrawable(R.drawable.no_image));
                            return;
                        }

                        PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();

                        if (photoMetadataBuffer.getCount() > 0) {
                            Log.i("TAG","success1");
                            // Display the first bitmap in an ImageView in the size of the view
                            photoMetadataBuffer.get(0)
                                    .getScaledPhoto(mGoogleApiClient,placeImage.getWidth(),placeImage.getWidth())
                                    .setResultCallback(mDisplayPhotoResultCallback);
                        }else{
                        }
                        photoMetadataBuffer.release();
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("Connection: ","Disconnected");
    }

    private ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback
            = new ResultCallback<PlacePhotoResult>() {
        @Override
        public void onResult(PlacePhotoResult placePhotoResult) {
            if (!placePhotoResult.getStatus().isSuccess()) {
                placeImage.setImageDrawable(getResources().getDrawable(R.drawable.no_image));
                return;
            }
            placeImage.setImageDrawable(new BitmapDrawable(getResources(),placePhotoResult.getBitmap()));
        }
    };
}
