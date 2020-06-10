package com.example.snehajain.count;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Home_Activity extends AppCompatActivity {
Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_);
        Button cam=findViewById(R.id.button2);
        cam.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(Home_Activity.this);
            }
        });

        Button clear = findViewById(R.id.button3);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(getIntent());
            }
        });

        Button count =findViewById(R.id.button4);
        count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText ipv4 = findViewById(R.id.editText);
                String ipv4addr = ipv4.getText().toString();
                EditText portNum = findViewById(R.id.editText2);
                String port = portNum.getText().toString();
                    String url = "http://" + ipv4addr + ":" + port + "/count";
                    String postBodyText="Hello";
                    MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");
                    RequestBody postBody = RequestBody.create(mediaType, postBodyText);
                    postRequest(url, postBody);
                    Toast.makeText(Home_Activity.this,"Counting Started!",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void connectServer(View v) {
        EditText ipv4 = findViewById(R.id.editText);
        String ipv4addr = ipv4.getText().toString();
        EditText portNum = findViewById(R.id.editText2);
        String port = portNum.getText().toString();
        if(bitmap!=null){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            String url = "http://" + ipv4addr + ":" + port + "/image";
            String postBodyText = "Hello";
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            RequestBody postBodyImage = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                    .build();
            TextView responseText = findViewById(R.id.textView5);
            responseText.setText("Please wait ...");
            postRequest(url, postBodyImage);

        }
        else{
            String url = "http://" + ipv4addr + ":" + port + "/";
            String postBodyText="Hello";
            MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");
            RequestBody postBody = RequestBody.create(mediaType, postBodyText);
            postRequest(url, postBody);
        }

    }

    void postRequest(String url, RequestBody postBody) {
        OkHttpClient client = new OkHttpClient()  .newBuilder()
                .connectTimeout(30,TimeUnit.SECONDS)
                .writeTimeout(30,TimeUnit.SECONDS)
                .readTimeout(30,TimeUnit.SECONDS)
                .build();


        Request request = new Request.Builder()
                .url(url)
                .post(postBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                call.cancel();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.textView5);
                        responseText.setText(e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.textView5);
                        try {
                            responseText.setText(response.body().string());

                        } catch (IOException e) {
                          Toast.makeText(Home_Activity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                          //  e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                ImageView img=findViewById(R.id.imageView);
                img.setImageURI(resultUri);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
