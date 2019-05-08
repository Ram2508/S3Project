
package com.amazonaws.project;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class CreateActivity extends ListActivity {

    private final String TAG = "CreateActivity";

    private EditText courseTitle;
    private EditText courseNumber;
    private EditText instructorName;
    private EditText projectNumber;
    private EditText projectDescription;
    private EditText dueDate;

    static TransferUtility transferUtility;
    static List<TransferObserver> observers;

    static Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        util = new Util();
        transferUtility = util.getTransferUtility(this);
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (observers != null && !observers.isEmpty()) {
            for (TransferObserver observer : observers) {
                observer.cleanTransferListener();
            }
        }
    }


    private void initUI(){

        courseTitle = findViewById(R.id.course_title);
        courseNumber = findViewById(R.id.course_number);
        instructorName = findViewById(R.id.instructor_name);
        projectNumber = findViewById(R.id.project_number);
        projectDescription = findViewById(R.id.project_description);
        dueDate = findViewById(R.id.due_date);
        Button btnCreateUpload = findViewById(R.id.buttonCreateUpload);

        btnCreateUpload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = "Course Title        : "+courseTitle.getText()+"\n"+
                        "Course Number       : "+courseNumber.getText()+"\n"+
                        "Instructor Name     : "+instructorName.getText()+"\n"+
                        "Project Number      : "+projectNumber.getText()+"\n"+
                        "Project Description : "+projectDescription.getText()+"\n"+
                        "Due Date            : "+dueDate.getText();

                File file = new File(Environment.getExternalStorageDirectory()+File.separator+courseTitle.getText()+"_"+projectNumber.getText()+".txt");
                try {
                    if(file.createNewFile())
                    {
                        Log.d(TAG, "File created successfully");
                    }
                if(file.exists())
                {
                    OutputStream out = new FileOutputStream(file);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
                    outputStreamWriter.write(str);
                    outputStreamWriter.close();
                }else{
                    Toast.makeText(getApplicationContext(), "Failed to create and upload",Toast.LENGTH_LONG).show();
                }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                transferUtility.upload(
                        file.getName(),
                        file
                );
                Toast.makeText(getApplicationContext(), "Successfully uploaded file to cloud", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(CreateActivity.this, DownloadFileActivity.class);
                startActivity(intent);
            }
        });

    }

}
