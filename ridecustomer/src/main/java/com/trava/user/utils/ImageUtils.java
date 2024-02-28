package com.trava.user.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.trava.user.AppVersionConstants;
import com.trava.user.BuildConfig;
import com.trava.user.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageUtils
{
    public static final int REQ_CODE_CAMERA_PICTURE = 1;
    public static final int REQ_CODE_GALLERY_PICTURE = 2;
    private static final String TAG = ImageUtils.class.getSimpleName();
    private static File imageFile;

    public static void displayImagePicker(final Object parentContext)
    {
        Context context = null;
        if (parentContext instanceof Fragment)
        {
            context = ((Fragment) parentContext).getContext();
        } else if (parentContext instanceof Activity)
            context = (Activity) parentContext;

        if (context != null)
        {
            String[] pickerItems = {
                    context.getString(R.string.dialog_camera),
                    context.getString(R.string.dialog_gallery),
                    context.getString(android.R.string.cancel)};

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.dialog_select_your_choice));
            builder.setItems(pickerItems, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which)
                    {
                        case 0:
                            openCamera(parentContext);
                            break;

                        case 1:
                            openGallery(parentContext);
                            break;
                    }
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    /**
     * Open the device camera using the ACTION_IMAGE_CAPTURE intent
     *
     * @param uiReference Reference of the current ui.
     *                    Use either android.support.v7.app.AppCompatActivity or android.support
     *                    .v4.app.Fragment
     *                    //     * @param imageFile   Destination image file
     *
     */
    private static void openCamera(@NonNull Object uiReference)
    {
        Context context = null;
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try
        {
            if (uiReference instanceof Fragment)
                context = ((Fragment) uiReference).getContext();
            else if (uiReference instanceof AppCompatActivity)
                context = (AppCompatActivity) uiReference;
            if (context != null)
            {
                if(context.getExternalCacheDir()!=null)
                    imageFile = ImageUtils.createImageFile(context.getExternalCacheDir().getPath());
                // Put the uri of the image file as intent extra
                Uri imageUri = FileProvider.getUriForFile(context,
                        AppVersionConstants.Companion.getAPPLICATION_ID(),
                        imageFile);

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                // Get a list of all the camera apps
                List<ResolveInfo> resolvedIntentActivities =
                        context.getPackageManager()
                                .queryIntentActivities(cameraIntent, PackageManager
                                        .MATCH_DEFAULT_ONLY);

                // Grant uri read/write permissions to the camera apps
                for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities)
                {
                    String packageName = resolvedIntentInfo.activityInfo.packageName;

                    context.grantUriPermission(packageName, imageUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                if (uiReference instanceof Fragment)
                    ((Fragment) uiReference).startActivityForResult(cameraIntent,
                            REQ_CODE_CAMERA_PICTURE);
                else
                    ((AppCompatActivity) uiReference).startActivityForResult(cameraIntent,
                            REQ_CODE_CAMERA_PICTURE);
            }
        } catch (NullPointerException | IOException e)
        {
            e.printStackTrace();
        }
    }


    private static void openGallery(Object uiReference)
    {
        Intent galleryIntent =  new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (uiReference instanceof AppCompatActivity)
            ((AppCompatActivity) uiReference).startActivityForResult(galleryIntent,
                    REQ_CODE_GALLERY_PICTURE);
        else if (uiReference instanceof Fragment)
            ((Fragment) uiReference).startActivityForResult(galleryIntent,
                    REQ_CODE_GALLERY_PICTURE);
    }

    public static File getImagePathFromGallery(Context context, @NonNull Uri imageUri)
    {
        String imagePath = null;
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(imageUri,
                filePathColumn, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imagePath = cursor.getString(columnIndex);
            cursor.close();
        }

        return saveImageToExternalStorage(context,getFile(imagePath));
    }

    private static File saveImageToExternalStorage(Context context,Bitmap finalBitmap) {
        File file2;
        File mediaStorageDir =new File(context.getExternalCacheDir().getPath());
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        file2= new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        try {
            FileOutputStream out;
            out = new FileOutputStream(file2);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG,100, out);
            out.flush();
            out.close();
            return file2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file2;
    }

    private static Bitmap getFile(String imgPath) {
        Bitmap bMapRotate=null;
        try {

            if (imgPath != null) {
                ExifInterface exif = new ExifInterface(imgPath);

                int mOrientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 1);

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imgPath, options);
                options.inSampleSize = calculateInSampleSize(options,400,400);
                options.inJustDecodeBounds = false;

                bMapRotate = BitmapFactory.decodeFile(imgPath, options);
                if (mOrientation == 6) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bMapRotate = Bitmap.createBitmap(bMapRotate, 0, 0,
                            bMapRotate.getWidth(), bMapRotate.getHeight(),
                            matrix, true);
                } else if (mOrientation == 8) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(270);
                    bMapRotate = Bitmap.createBitmap(bMapRotate, 0, 0,
                            bMapRotate.getWidth(), bMapRotate.getHeight(),
                            matrix, true);
                } else if (mOrientation == 3) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(180);
                    bMapRotate = Bitmap.createBitmap(bMapRotate, 0, 0,
                            bMapRotate.getWidth(), bMapRotate.getHeight(),
                            matrix, true);
                }
            }

        } catch (OutOfMemoryError e) {
            bMapRotate = null;
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            bMapRotate = null;
            e.printStackTrace();
        }
        return bMapRotate;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

  /*  @NonNull
    public static String getFacebookProfileImage(@NonNull String profileId)
    {
        return "https://graph.facebook.com/" + profileId + "/picture?type=large";
    }*/

    /*private void cropAndRotateImage(Fragment fragment, File sourceFile, File destinationFile)
    {
        Context context = fragment.getContext();
        if (sourceFile != null && destinationFile != null)
        {
            Uri sourceFileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID +
            ".provider",
                    sourceFile);

            Uri destinationFileUri = FileProvider.getUriForFile(context, BuildConfig
            .APPLICATION_ID + ".provider",
                    destinationFile);

            CropImage.activity(sourceFileUri)
                    .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setAspectRatio(1, 1)
                    .setMinCropResultSize(700, 700)
                    .setRequestedSize(2000, 2000)
                    .setOutputCompressQuality(90)
                    .setOutputUri(destinationFileUri)
                    .start(context, fragment);
        } else
            Log.e(TAG, "Source or destination file is null");
    }*/

    @Nullable
    private static File createImageFile(@NonNull String directory) throws IOException
    {
        File imageFile = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState()))
        {
            File storageDir = new File(directory);
            if (!storageDir.mkdirs())
            {
                if (!storageDir.exists())
                {
                    Log.d(TAG, "Failed to create directory");
                    return null;
                }
            }
            String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";

            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        }
        return imageFile;
    }

    /**
     * Revoke access permission for the content URI to the specified package otherwise the
     * permission won't be
     * revoked until the device restarts.
     */
   /* public static void revokeUriPermission(Context context, File file)
    {
        Log.d(TAG, "Uri permission revoked");
        context.revokeUriPermission(FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider", file),
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }*/

    public static File getFile(Context context)
    {
        if (imageFile != null)
            return saveImageToExternalStorage(context,getFile(imageFile.getPath()));
        else
            return null;
    }
}
