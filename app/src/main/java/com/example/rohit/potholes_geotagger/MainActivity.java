package com.example.rohit.potholes_geotagger;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rtugeek.android.colorseekbar.ColorSeekBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //locaton var
    private String TAG1="mainact";
    private Button but1;
    private boolean but1t=false;
    private FusedLocationProviderClient mFusedLocationClient;
    private int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private TextView textView1;
    private Integer counter=0;
    private String s;

    ArrayList<String> log = new ArrayList<>();
    ArrayList<String> lat = new ArrayList<>();

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 101;

    //camera vars
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private Button cambut;
    private Button write;

    //firebase vars
    private StorageReference mStorageRef;

    //slider
    private ColorSeekBar colorSeekBar;
    private Integer priority=0;

    //photo
    private Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //location part vars
//        but1 = (Button) findViewById(R.id.but1);
//        textView1 =(TextView) findViewById(R.id.tv);

        //camera part vars
        imageView = (ImageView) findViewById(R.id.imgview);

        cambut = (Button) findViewById(R.id.but1);

 //       write = (Button) findViewById(R.id.write);

        //firebase vars
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //slider
        colorSeekBar =(ColorSeekBar)findViewById(R.id.colorSlider);
        colorSeekBar.setMaxPosition(100);
        colorSeekBar.setColorSeeds(R.array.material_colors); // material_colors is defalut included in res/color,just use it.
        colorSeekBar.setColorBarPosition(10); //0 - maxValue
        colorSeekBar.setAlphaBarPosition(10); //0 - 25
        //     colorSeekBar.setShowAlphaBar(true);
        colorSeekBar.setBarHeight(5); //5dpi
        colorSeekBar.setThumbHeight(30); //30dpi
        colorSeekBar.setBarMargin(10);

        //location part
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(100); //use a value fo about 10 to 15s for a real app
        locationRequest.setFastestInterval(100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);

        }


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    //Update UI with location data
                    if (location != null) {
                        log.add(String.valueOf(location.getLongitude()));
                        lat.add(String.valueOf(location.getLatitude()));
                        Log.d("loc", "onLocationResult: "+log.size());
                    }
                }
            }
        };


/*        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!but1t){
                    startLocationUpdates();
                    Log.d("start", "onClick: ");
                    but1t=!but1t;
                }else{
                    stopLocationUpdates();
                    Log.d("stop", "onClick: ");
                    but1t=!but1t;
                }

            }
        });



        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

 */

        //camera part

        cambut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},
                            MY_CAMERA_PERMISSION_CODE);

                }
                else
                {
                    startLocationUpdates();
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });



        //slider bar
        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color) {
                priority = colorBarPosition;
            }
        });



    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if(resultCode == RESULT_OK) {
                if(log.size()>0) {

                    if(priority>75) {
                        s = "c-" + counter + "_meme-worthy_-" + priority + " " + log.get(log.size() - 1) + "__lat__" + lat.get(lat.size() - 1);
                    }else {
                        s = "c-" + counter + "_p-" + priority + " " + log.get(log.size() - 1) + "__lat__" + lat.get(lat.size() - 1);
                    }

                    photo = (Bitmap) data.getExtras().get("data");
                    imageView.setImageBitmap(photo);

                    OutputStream fOut = null;
                    File file = new File(MainActivity.this.getFilesDir(), "img_log__" + s + ".jpg");// the File to save , append increasing numeric counter to prevent files from getting overwritten.

                    try {
                        fOut = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    photo.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                    try {
                        fOut.flush(); // Not really required
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        fOut.close(); // do not forget to close the stream
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Uri file1 = Uri.fromFile(new File(MainActivity.this.getFilesDir(), "img_log__" + s + ".jpg"));
                    uploadfiles(file1, "images/" + "img_log__" + s + ".jpg");

                    counter++;
                    stopLocationUpdates();

                }

            }
        }

    }


    //outside the activity

    private void uploadfiles(Uri file, String s){

        StorageReference riversRef = mStorageRef.child(s);
        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.d("upload", "onSuccess: "+"done");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
            }
        }

    }


    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSION_REQUEST_FINE_LOCATION:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted do nothing and carry on

                } else {
                    Toast.makeText(getApplicationContext(), "This app requires location permissions to be granted", Toast.LENGTH_SHORT).show();
                    finish();
                }

                break;
            case MY_CAMERA_PERMISSION_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission was granted
                }else{
                    Toast.makeText(getApplicationContext(), "This app requires Camera permissions to be granted", Toast.LENGTH_SHORT).show();
                    finish();
                }


        }





    }

}
