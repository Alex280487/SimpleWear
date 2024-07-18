package android.os;

import androidx.annotation.DeprecatedSinceApi;
import androidx.annotation.RequiresApi;

public interface IPowerManager extends IInterface {

    @DeprecatedSinceApi(api = Build.VERSION_CODES.M)
    void wakeUp(long time);

    @RequiresApi(api = Build.VERSION_CODES.M)
    void wakeUp(long time, String reason, String opPackageName);

    void goToSleep(long time, int reason, int flags);

    abstract class Stub extends Binder implements IPowerManager {
        public static IPowerManager asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }
    }
}