package info.korzeniowski.walletplus.test.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.service.ormlite.AccountServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.ProfileServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.UserDatabaseHelper;
import info.korzeniowski.walletplus.test.service.cashflow.CashFlowServiceOrmLiteTest;
import info.korzeniowski.walletplus.test.service.tag.TagStatisticsTest;
import info.korzeniowski.walletplus.test.service.tag.LocalTagServiceTest;
import info.korzeniowski.walletplus.test.service.wallet.LocalWalletServiceTest;
import info.korzeniowski.walletplus.test.service.wallet.WalletValidatorTest;
import info.korzeniowski.walletplus.util.PrefUtils;

@Module(
        includes = DatabaseModule.class,
        injects = {
                CashFlowServiceOrmLiteTest.class,
                TagStatisticsTest.class,
                LocalTagServiceTest.class,
                LocalWalletServiceTest.class,
                WalletValidatorTest.class
        },
        overrides = true,
        complete = false
)
public class TestDatabaseModule {
    private Context context;

    public TestDatabaseModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    public UserDatabaseHelper provideUserDatabaseHelper(ProfileServiceOrmLite profileService, AccountServiceOrmLite accountService) {
        Profile profile = new Profile().setName("Test profile");
        profileService.insert(profile);
        PrefUtils.setActiveProfileId(context, profile.getId());
        return new UserDatabaseHelper(context, profile.getName());
    }
}
