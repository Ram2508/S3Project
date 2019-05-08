
package com.amazonaws.project;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DownloadFileActivity extends ListActivity {

    private static final String TAG = "DownloadFileActivity";
    private static final int REQUEST_WRITE_STORAGE = 112;

    private AmazonS3Client s3;
    static TransferUtility transferUtility;
    static Util util;
    private String bucket;
    static SimpleAdapter simpleAdapter;
    private ArrayList<HashMap<String, Object>> transferRecordMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        util = new Util();
        transferUtility = util.getTransferUtility(this);
        bucket = new AWSConfiguration(this).optJsonObject("S3TransferUtility").optString("Bucket");
        initData();
        initUI();

    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetFileListTask().execute();
    }

    private void initData()
    {
        s3 = util.getS3Client(DownloadFileActivity.this);
        transferRecordMaps = new ArrayList<HashMap<String, Object>>();
    }

    private class GetFileListTask extends AsyncTask<Void, Void, Void> {
        private List<S3ObjectSummary> s3ObjList;
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(DownloadFileActivity.this,
                    getString(R.string.refreshing),
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... inputs) {
            s3ObjList = s3.listObjects(bucket).getObjectSummaries();
            transferRecordMaps.clear();
            for (S3ObjectSummary summary : s3ObjList) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("key", summary.getKey());
                transferRecordMaps.add(map);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            simpleAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        initUI();
    }

    private void initUI() {

        simpleAdapter = new SimpleAdapter(DownloadFileActivity.this, transferRecordMaps,
                R.layout.bucket_item, new String[] {
                "key"
        },
                new int[] {
                        R.id.key
                });
        simpleAdapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                switch (view.getId()) {
                    case R.id.key:
                        TextView fileName = (TextView) view;
                        fileName.setText((String) data);
                        return true;
                }
                return false;
            }
        });
        setListAdapter(simpleAdapter);

        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                String key = (String) transferRecordMaps.get(pos).get("key");
                beginDownload(key);
            }
        });
        requestWriteExternalStoragePermission();
    }

    private void  requestWriteExternalStoragePermission() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to store data in external storage is not granted");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission to access the External Storage is required for this application to store the downloaded data from Amazon S3")
                        .setTitle("Permission required");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "Clicked");
                        makeRequest();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                makeRequest();
            }
        }
        else {
            Log.i(TAG, "Permission to store data in external storage is granted.");
        }

    }
    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);
    }

    private void beginDownload(String key) {
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + key);
        TransferObserver observer = transferUtility.download(key, file);
        Toast.makeText(DownloadFileActivity.this,"Successfully Downloaded", Toast.LENGTH_LONG).show();
    }
}
