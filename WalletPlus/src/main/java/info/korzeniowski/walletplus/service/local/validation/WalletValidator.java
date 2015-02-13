package info.korzeniowski.walletplus.service.local.validation;

import com.google.common.base.Objects;

import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import info.korzeniowski.walletplus.service.exception.WalletTypeCannotBeChangedException;

import static com.google.common.base.Preconditions.checkNotNull;

public class WalletValidator implements Validator<Wallet>{
    private final WalletService walletService;

    public WalletValidator(WalletService walletService) {
        this.walletService = walletService;
    }

    @Override
    public void validateInsert(Wallet wallet) {
        checkNotNull(wallet);
        validateIfInitialAmountIsNotNull(wallet);
        wallet.setCurrentAmount(wallet.getInitialAmount());
    }

    private void validateIfInitialAmountIsNotNull(Wallet wallet) {
        if (wallet.getInitialAmount() == null) {
            throw new EntityPropertyCannotBeNullOrEmptyException(wallet.getClass().getSimpleName(), "InitialAmount");
        }
    }

    @Override
    public void validateUpdate(Wallet newWallet) {
        Wallet oldWallet = walletService.findById(newWallet.getId());
    }

    @Override
    public void validateDelete(Long id) {

    }
}
