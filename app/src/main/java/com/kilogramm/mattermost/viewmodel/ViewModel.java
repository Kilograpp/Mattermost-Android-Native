package com.kilogramm.mattermost.viewmodel;

import android.os.Bundle;

/**
 * Created by Evgeny on 25.07.2016.
 * base viewModel interface
 */
public interface ViewModel {
    void destroy();
    void onSaveInstanceState(Bundle outState);
    void onRestoreInstanceState(Bundle savedInstanceState);
}
