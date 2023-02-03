package com.pardeep.foxy_sdk;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.play.core.splitcompat.SplitCompat;
import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;
import com.google.android.play.core.splitinstall.SplitInstallSessionState;
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener;
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

public class FoxySdk
{
 private SplitInstallManager splitInstallManager = null;
 private ProgressDialog progressDialog = null;

    public void initSdk(Context context) {
        splitInstallManager = SplitInstallManagerFactory.create(context);

        isDynamicModuleAlreadyInstalled(context);
    }


    private void isDynamicModuleAlreadyInstalled(Context context) {
        boolean isModuleAlreadyInstalled = splitInstallManager.getInstalledModules().contains("dynamicfeature");
        if(!isModuleAlreadyInstalled) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Downloading host app dynamically");
            progressDialog.show();
            getAppBundle(context);
        } else {
            Toast.makeText(context, "Starting Activity", Toast.LENGTH_SHORT).show();
        }
    }


    public void getAppBundle(Context context){
        SplitInstallRequest request = SplitInstallRequest.newBuilder()
                .addModule("dynamicfeature")
                .build();


        splitInstallManager.startInstall(request).addOnCompleteListener(new OnCompleteListener<Integer>() {
            @Override
            public void onComplete(@NonNull Task<Integer> task) {

                Log.e("onComplete", "Completed = "+task.isComplete() + ", Success = "+task.isSuccessful());
            }
        }).addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {


            }
        });


        splitInstallManager.registerListener(new SplitInstallStateUpdatedListener() {
            @Override
            public void onStateUpdate(@NonNull SplitInstallSessionState state) {
                switch (state.status()){
                    case SplitInstallSessionStatus.DOWNLOADING:
                        long totalKiloBytes = state.totalBytesToDownload() / 1024;
                        long kiloBytesDownloaded = state.bytesDownloaded() / 1024;
                        long progressPercentage = kiloBytesDownloaded * 100 / totalKiloBytes;
                        // Update progress bar.
                        String progressMessage = String.format("Total size: %dKB, Progress: %d%%", totalKiloBytes, progressPercentage);
                        System.out.println("totalKiloBytes: " + totalKiloBytes + ", kiloBytesDownloaded: " + kiloBytesDownloaded);
                        System.out.println(progressMessage);
                        progressDialog.setMessage(progressMessage);
                        break;

                    case SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION:
                        // Displays a confirmation for the user to confirm the request.

                        break;

                    case SplitInstallSessionStatus.INSTALLED:
                        // After a module is installed, you can start accessing its content or
                        // fire an intent to start an activity in the installed module.
                        // For other use cases, see access code and resources from installed modules.

                        // If the request is an on demand module for an Android Instant App
                        // running on Android 8.0 (API level 26) or higher, you need to
                        // update the app context using the SplitInstallHelper API.
                        progressDialog.setMessage("Installed");
                        progressDialog.dismiss();
                        SplitCompat.installActivity(context);
                        break;
                    case SplitInstallSessionStatus.DOWNLOADED:
                        System.out.println("Download successful");
                        progressDialog.setMessage("Download successful");

                        break;
                    case SplitInstallSessionStatus.FAILED:
                        System.out.println("Failed to download host app, error code:  " + state.errorCode());
                        progressDialog.setMessage("Failed to download host app, error code: " + state.errorCode());
                        break;

                    case SplitInstallSessionStatus.PENDING:
                        System.out.println("Waiting for the download to begin");
                        progressDialog.setMessage("Waiting for the download to begin");
                        break;

                    case SplitInstallSessionStatus.INSTALLING:
                        System.out.println("Installing host app...");
                        progressDialog.setMessage("Installing host app...");
                        break;
                }

            }
        });
    }



    private void navigateToModuleActivity(Context context){
        Intent intent = new Intent();
        intent.setClassName("com.pardeep.foxy_sdk", "com.pardeep.foxydynamicmodule.ModuleMainActivity");
        context.startActivity(intent);
    }
}
