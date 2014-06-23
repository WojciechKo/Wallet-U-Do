package info.korzeniowski.walletplus.test.robolectric.datamanager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import info.korzeniowski.walletplus.datamanager.AccountDataManager;
import info.korzeniowski.walletplus.datamanager.local.LocalAccountDataManager;
import info.korzeniowski.walletplus.model.Account;

import static org.fest.assertions.api.Assertions.assertThat;

@Ignore
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalAccountDataManagerTest {

    private AccountDataManager accountDataManager;

    @Before
    public void setUp() {
        accountDataManager = new LocalAccountDataManager(null);
    }

    @Ignore
    @Test
    public void shouldCreateNewAccount() {

        Account account = new Account();
        account.setName("MyAccount");
        account.setPasswordHash("q1w2e3r4");

        accountDataManager.insert(account);

        Account read = accountDataManager.findById(account.getId());
        assertThat(accountDataManager.count()).isEqualTo(1);
        assertThat(read).isEqualTo(account);
        assertThat(read.getId()).isEqualTo(account.getId());
        assertThat(read.getName()).isEqualTo(account.getName());
        assertThat(read.getPasswordHash()).isEqualTo(account.getPasswordHash());
    }

}
