
package dev.ukanth.iconmgr;

import androidx.annotation.Nullable;

/**
 * Created by ukanth on 15/8/17.
 */

public interface DetailListener {
    void onHomeDataUpdated(@Nullable Detail detail);
    void onHomeIntroInit();
}
