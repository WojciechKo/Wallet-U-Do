package com.walletudo.ui.statistics;

import android.os.Bundle;

import com.walletudo.R;
import com.walletudo.ui.BaseActivity;

public class StatisticActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }

        setContentView(R.layout.activity_drawer);

        if (null == savedInstanceState) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new StatisticFragment())
                    .commit();
        }

        overridePendingTransition(0, 0);
    }

    @Override
    protected DrawerItemType getSelfNavDrawerItem() {
        return DrawerItemType.STATISTIC;
    }
}