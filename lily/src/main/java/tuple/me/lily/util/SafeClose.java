package tuple.me.lily.util;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.io.Closeable;
import java.net.DatagramSocket;
import java.net.ServerSocket;

/**
 * Created by gokul-4192 on 0025 25-Feb-17.
 */
public class SafeClose {
    public static void safeClose(@Nullable ServerSocket serverSocket){
        if(serverSocket!=null){
            try {
                serverSocket.close();
            }catch (Exception ignored){
            }
        }
    }
    public static void safeClose(@Nullable DatagramSocket datagramSocket){
        if(datagramSocket!=null){
            try {
                datagramSocket.close();
            }catch (Exception ignored){

            }
        }
    }

    public static void safeClose(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void safeClose(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Exception ignored) {
            }
        }
    }
}
