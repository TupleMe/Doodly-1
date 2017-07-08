package tuple.me.dtools.qr;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arasthel.asyncjob.AsyncJob;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import timber.log.Timber;
import tuple.me.dtools.R;
import tuple.me.dtools.file.SystemFile;
import tuple.me.dtools.file.util.FilePicker;
import tuple.me.dtools.sugarmodel.qr.QRItem;
import tuple.me.lily.core.Callback;
import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.util.DialogUtil;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.util.async.SimpleJob;
import tuple.me.lily.util.async.doInBackground;
import tuple.me.lily.util.async.onComplete;
import tuple.me.lily.views.toasty.Toasty;

public class QRActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {
    private QRCodeReaderView qrReaderView;
    private boolean isTorchOn = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        qrReaderView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        qrReaderView.setOnQRCodeReadListener(this);
        qrReaderView.setQRDecodingEnabled(true);
        qrReaderView.setAutofocusInterval(2000L);
        qrReaderView.setTorchEnabled(false);
        qrReaderView.setBackCamera();
        FloatingActionButton history = (FloatingActionButton) findViewById(R.id.history);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
                    @Override
                    public void doOnBackground() {
                        Iterator<QRItem> qrs = QRItem.findAll(QRItem.class);
                        final ArrayList<String> qrsList = new ArrayList<>();
                        while (qrs.hasNext()) {
                            QRItem item = qrs.next();
                            if (!TextUtils.isEmpty(item.text))
                                qrsList.add(item.text);
                        }
                        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                            @Override
                            public void doInUIThread() {
                                if (qrsList.isEmpty()) {
                                    Toasty.empty(QRActivity.this, R.string.history_is_empty);
                                    return;
                                }
                                MaterialDialog.Builder builder = DialogUtil.getBlankDialogBuilder(QRActivity.this);
                                builder.title(R.string.history);
                                builder.negativeText(R.string.cancel);
                                builder.positiveText(R.string.clear_history);
                                builder.items(qrsList);
                                builder.itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        CommonUtil.copyToClipBoard("QR Code", qrsList.get(which));
                                        Toasty.success(QRActivity.this, R.string.success);
                                        finish();
                                    }
                                });
                                builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        QRItem.deleteAll(QRItem.class);
                                    }
                                });
                                builder.build().show();
                            }
                        });

                    }
                });
            }
        });
        FloatingActionButton torch = (FloatingActionButton) findViewById(R.id.torch);
        if (hasFlash()) {
            torch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((FloatingActionButton) v).setImageResource(isTorchOn ? R.drawable.ic_flash_off : R.drawable.ic_flash_on);
                    qrReaderView.setTorchEnabled(isTorchOn = (!isTorchOn));
                }
            });
            torch.setImageResource(isTorchOn ? R.drawable.ic_flash_off : R.drawable.ic_flash_on);
        } else {
            torch.setVisibility(View.INVISIBLE);
        }

        FloatingActionButton openfile = (FloatingActionButton) findViewById(R.id.open_file);
        openfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<String> files = new ArrayList<String>(2) {
                    {
                        add("jpg");
                        add("png");
                    }
                };
                new FilePicker(QRActivity.this).filter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() || files.contains(FileUtils.getFileExtension(file));
                    }
                }).onSelect(new Callback<SystemFile>() {
                    @Override
                    public void call(final SystemFile val) {
                        SimpleJob.simpleJob(new doInBackground<String, Void>() {
                            @Override
                            public String doInBackground(Void[] parms) throws Exception {
                                return scanViaFile(val.file);
                            }
                        }, new onComplete<String>() {
                            @Override
                            public boolean onSuccess(String result) {
                                onQRCodeRead(result, null);
                                return true;
                            }

                            @Override
                            public void onError(Exception exception) {
                                Toasty.error(R.string.error_occurred);
                            }
                        });
                    }
                }).show();
            }
        });
    }

    private String scanViaFile(File file) throws Exception {
        InputStream inputStream = new FileInputStream(file);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if (bitmap == null) {
            throw new Exception();
        }
        int width = bitmap.getWidth(), height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        bitmap.recycle();
        bitmap = null;
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
        MultiFormatReader reader = new MultiFormatReader();
        Result result = reader.decode(bBitmap);
        return result.getText();
    }

    @Override
    public void onQRCodeRead(final String text, PointF[] point) {
        Timber.d("Qr Scanner" + text);
        qrReaderView.stopCamera();
        MaterialDialog.Builder dialogBuilder = DialogUtil.getBasicDialog(QRActivity.this, new String[]{"Scan Success : ", text, "Copy to clip Board", "Rescan", null});
        dialogBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
                startActivity(new Intent(QRActivity.this, QRActivity.class));
                finish();
            }
        }).onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                CommonUtil.copyToClipBoard("QR Code", text);
                AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
                    @Override
                    public void doOnBackground() {
                        new QRItem(System.currentTimeMillis(), text).save();
                        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                            @Override
                            public void doInUIThread() {
                                Toasty.success(QRActivity.this, R.string.success);
                                finish();
                            }
                        });
                    }
                });
            }
        }).cancelable(false);
        MaterialDialog dialog = dialogBuilder.build();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrReaderView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrReaderView.stopCamera();
    }

    public boolean hasFlash() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
}
