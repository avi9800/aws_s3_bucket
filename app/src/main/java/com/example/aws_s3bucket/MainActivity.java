package com.example.aws_s3bucket;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private Button upload,download;
    private ImageView imageView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView=findViewById(R.id.image);
      

        upload=findViewById(R.id.upload);
        download=findViewById(R.id.download);
        progressBar=findViewById(R.id.progress_bar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload_file();
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download_file();
            }
        });
    }

    private void upload_file() {
        AWSCredentials credentials=new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return "AWS_accessKey";
            }

            @Override
            public String getAWSSecretKey() {
                return "AWS_SecretKey";
            }
        };

        CognitoCachingCredentialsProvider mCredentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),    /* pega o Context da aplicação */
                "ID_Pool",    /* ID da Identity Pool */
                Regions.Region_name           /* Região para a sua identity pool --US_EAST_1 or EU_WEST_1*/
        );
       // AmazonS3 s3 = new AmazonS3Client(mCredentialsProvider);

        AmazonS3Client s3Client=new AmazonS3Client(credentials);
        s3Client.setRegion(Region.getRegion(Regions.Region_Name));

        String bucketName = "Bucket_name";


        try {
            File outputdir=getCacheDir();
            final File tempfile=File.createTempFile("book"+"1",".jpg",outputdir);
            FileOutputStream outputStream=new FileOutputStream(tempfile);
            Bitmap bp= BitmapFactory.decodeResource(getResources(),R.drawable.book);
            bp.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            outputStream.flush();
            outputStream.close();
        //TransferUtility transferUtility=new TransferUtility(s3Client,this);
        TransferUtility transferUtility=TransferUtility.builder().context(this).s3Client(s3Client).defaultBucket(bucketName).build();
        TransferObserver observer=transferUtility.upload(bucketName,"pic_location"+"1",tempfile, CannedAccessControlList.PublicRead);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Toast.makeText(MainActivity.this, "Upload done", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int id, Exception ex) {
                    Log.e("uploaderror",ex.toString());
            }
        });

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            Toast.makeText(this, "Done uploading", Toast.LENGTH_SHORT).show();
        }

    }

    private void download_file() {
        AWSCredentials credentials=new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return "AWS_Access_Key";
            }

            @Override
            public String getAWSSecretKey() {
                return "AWS_Secret_Key";
            }
        };

        AmazonS3Client s3Client=new AmazonS3Client(credentials,Region.getRegion(Regions.Region_name));

        String bucketName = "Bucket_name";

        TransferUtility transferUtility=TransferUtility.builder().s3Client(s3Client).context(this).defaultBucket(bucketName).build();

        File outputdir=getCacheDir();
        try {
            final File tempfile=File.createTempFile("filename","filetype",outputdir);
            TransferObserver observer= transferUtility.download("file_location",tempfile);
            observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if(TransferState.IN_PROGRESS==state){
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setIndeterminate(true);
                    }
                   else if(TransferState.COMPLETED==state){
                        Toast.makeText(MainActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
                        progressBar.setIndeterminate(false);
                        progressBar.setVisibility(View.INVISIBLE);
                        if(tempfile!=null){
                            Toast.makeText(MainActivity.this, "File not empty", Toast.LENGTH_SHORT).show();
                            Bitmap bp=BitmapFactory.decodeFile(tempfile.getAbsolutePath());
                            imageView.setImageBitmap(bp);
                        }
                        else{
                            Toast.makeText(MainActivity.this, "File null", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                }

                @Override
                public void onError(int id, Exception ex) {

                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}