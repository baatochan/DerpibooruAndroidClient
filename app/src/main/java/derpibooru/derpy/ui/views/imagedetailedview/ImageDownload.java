package derpibooru.derpy.ui.views.imagedetailedview;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

import derpibooru.derpy.R;

/**
 * A UI for an image download implemented with {@link DownloadManager}.
 * <p>
 * The files are saved to <strong>"{@link Environment#DIRECTORY_PICTURES}/Derpibooru"</strong> folder, which
 * allows them to be scanned by the media scanner.
 */
class ImageDownload {
    private final Context mContext;
    private final String mDownloadTitle;
    private final String mDownloadDescription;
    private final String mDownloadFilePath;
    private final Uri mUri;

    ImageDownload(Context context, int imageId, String imageTagNames, String imageUrl) {
        mContext = context;
        mUri = Uri.parse(imageUrl);
        mDownloadTitle = getDownloadTitle(imageId, imageTagNames);
        mDownloadDescription = getDownloadDescription();
        mDownloadFilePath = getPathToFile(mUri);
    }

    void start() {
        Thread thread = new Thread(new DownloaderRunnable());
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    boolean isDownloaded() {
        String path = getAbsolutePathToFile(mUri);
        return new File(path).exists();
    }

    private String getDownloadTitle(int imageId, String imageTagNames) {
        return String.format(mContext.getString(R.string.download_image_notification_title), imageId, imageTagNames);
    }

    private String getDownloadDescription() {
        return mContext.getString(R.string.download_image_notification_description);
    }

    private String getPathToFile(Uri contentUri) {
        String file = contentUri.getLastPathSegment();
        return String.format("Derpibooru/%s", file);
    }

    private String getAbsolutePathToFile(Uri contentUri) {
        String file = contentUri.getLastPathSegment();
        return String.format("%s/Derpibooru/%s", Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(), file);
    }

    private void mkdirs() {
        new File(getAbsolutePathToFile(mUri)).getParentFile().mkdirs();
    }

    private class DownloaderRunnable implements Runnable {
        @Override
        public void run() {
            mkdirs();
            DownloadManager manager = (DownloadManager)
                    mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(mUri);
            request.setTitle(mDownloadTitle)
                    .setDescription(mDownloadDescription)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, mDownloadFilePath)
                    .allowScanningByMediaScanner();
            manager.enqueue(request);
        }
    }
}
