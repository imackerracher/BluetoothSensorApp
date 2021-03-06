package com.example.groupfourtwo.bluetoothsensorapp.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.groupfourtwo.bluetoothsensorapp.R;
import com.example.groupfourtwo.bluetoothsensorapp.data.DbExportImport;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * User Interface to access and move data from and to the external storage.
 *
 * @author Stefan Erk
 */
public class StorageActivity extends AppCompatActivity {

    /* debugging only */
    private static final String LOG_TAG = StorageActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST_STORAGE = 1;
    private static final int EXPORT_FAILED = 2;
    private static final int RESTORING_FAILED = 3;
    public static final int IMPORT_FAILED = 4;

    Button exportButton;
    Button restoreButton;
    Button importButton;

    final StorageActivity context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        exportButton = (Button) findViewById(R.id.button_export);
        restoreButton = (Button) findViewById(R.id.button_restore);
        importButton = (Button) findViewById(R.id.button_import);

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkStoragePermission()) {
                    if (DbExportImport.exportDb(context)) {
                        showSuccessDialog();
                    } else {
                        showErrorDialog(EXPORT_FAILED);
                    }
                }
            }
        });

        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkStoragePermission()) {
                    if (DbExportImport.restoreDb(context)) {
                        showSuccessDialog();
                    } else {
                        showErrorDialog(RESTORING_FAILED);
                    }
                }
            }
        });

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkStoragePermission()) {
                    new ImportTask().execute();
                }
            }
        });
    }


    /**
     * Test whether the user granted the application the permission to read from and write to
     * the external storage, like an SD card.
     * <p>
     * If the application has no rights yet, let the user decide about granting them now.
     */
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.request_storage_title));
                builder.setMessage(getString(R.string.request_storage_message));
                builder.setPositiveButton(getString(R.string.ok), null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE},
                                    PERMISSION_REQUEST_STORAGE);
                        }
                    }
                });
                builder.show();
                return false;
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG_TAG, "External storage - Access granted.");
                builder.setTitle(getString(R.string.access_granted))
                        .setMessage(getString(R.string.storage_request_granted));
            } else {
                Log.d(LOG_TAG, "External storage - Access denied.");
                builder.setTitle(getString(R.string.attention))
                        .setMessage(getString(R.string.storage_request_denied));
            }

            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        } else {
            Log.d(LOG_TAG, "An unknown request code arrived.");
        }
    }


    private void showErrorDialog(int errorCode) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (errorCode) {
            case EXPORT_FAILED:
                builder.setTitle(getString(R.string.error_export_title));
                builder.setMessage(getString(R.string.error_export_desc));
                break;

            case RESTORING_FAILED:
            case IMPORT_FAILED:
                builder.setTitle(getString(R.string.error_import_title));
                builder.setMessage(getString(R.string.error_import_desc));
                break;

            default:
                Log.d(LOG_TAG, "Unknown error code from button click.");
        }
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        builder.show();
    }

    private void showSuccessDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Finished")
                .setMessage("Transfer completed successfully")
                .setPositiveButton(android.R.string.ok, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
                })
                .show();
    }


    private class ImportTask extends AsyncTask<Object, Integer, Boolean> {
        private AlertDialog waitDialog;

        @Override
        protected void onPreExecute() {
            waitDialog = new AlertDialog.Builder(context)
                    .setTitle("Loading")
                    .setMessage("Please wait while the data is being imported.")
                    .setCancelable(false)
                    .show();
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            return DbExportImport.importDb(context);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            waitDialog.dismiss();
            if (result) {
                showSuccessDialog();
            } else {
                showErrorDialog(IMPORT_FAILED);
            }
        }
    }

}
