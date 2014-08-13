package info.korzeniowski.walletplus.drawermenu.wallet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Strings;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.text.MessageFormat;
import java.text.NumberFormat;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.WalletNameAndTypeMustBeUniqueException;

@EFragment(R.layout.wallet_details)
@OptionsMenu({R.menu.action_delete, R.menu.action_save})
public class WalletDetailsFragment extends Fragment {
    private enum DetailsType {ADD, EDIT}

    static final String WALLET_ID = "WALLET_ID";

    @ViewById
    TextView walletName;

    @ViewById
    TextView walletInitialAmount;

    @Inject @Named("local")
    WalletService localWalletService;

    @Inject @Named("local")
    CashFlowService localCashFlowService;

    @Inject @Named("amount")
    NumberFormat amountFormat;

    private DetailsType type;
    private Wallet.Builder walletBuilder;
    private String originalName;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Long walletId = getArguments().getLong(WALLET_ID);
        type = walletId == 0L ? DetailsType.ADD : DetailsType.EDIT;
        Wallet wallet = null;
        if (type == DetailsType.EDIT) {
            wallet = localWalletService.findById(walletId);
            originalName = wallet.getName();
        }
        walletBuilder = new Wallet.Builder(wallet);
        return null;
    }

    @AfterViews
    void setupViews() {
        setupAdapters();
        setupListeners();
        fillViewsWithData();
    }

    private void setupAdapters() {

    }

    private void setupListeners() {
        walletName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                walletName.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateIfEmpty(walletName, getString(R.string.walletNameCantBeEmpty));
                validateIfNameIsUnique();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        walletInitialAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                walletInitialAmount.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateIfEmpty(walletInitialAmount, getString(R.string.walletInitialAmountCantBeEmpty));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void fillViewsWithData() {
        if (type == DetailsType.EDIT) {
            walletName.setText(walletBuilder.getName());
            walletInitialAmount.setText(amountFormat.format(walletBuilder.getInitialAmount()));
        }
    }

    private void getDataFromViews() {
        walletBuilder.setName(walletName.getText().toString());
        Double initialAmount = Double.parseDouble(walletInitialAmount.getText().toString());
        if (type == DetailsType.ADD) {
            walletBuilder.setCurrentAmount(walletBuilder.getInitialAmount());
        } else if (type == DetailsType.EDIT) {
            walletBuilder.setCurrentAmount(walletBuilder.getCurrentAmount() + initialAmount - walletBuilder.getInitialAmount());
        }
        walletBuilder.setInitialAmount(initialAmount);
    }

    @OptionsItem(R.id.menu_save)
    void actionSave() {
        if (validateIfEmptyFields()) {
            getDataFromViews();
            walletBuilder.setType(Wallet.Type.MY_WALLET);
            try {
                if (type == DetailsType.ADD) {
                    localWalletService.insert(walletBuilder.build());
                } else if (type == DetailsType.EDIT) {
                    localWalletService.update(walletBuilder.build());
                }
                getActivity().getSupportFragmentManager().popBackStack();
            } catch (WalletNameAndTypeMustBeUniqueException e) {
                walletName.setError(getString(R.string.walletNameHaveToBeUnique));
            }
        }
    }

    @OptionsItem(R.id.menu_delete)
    void actionDelete() {
        showConfirmationAlert();
    }

    private void showConfirmationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(getConfirmationMessage())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tryDelete(walletBuilder.getId());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    private void tryDelete(Long id) {
        localWalletService.deleteById(id);
    }

    private String getConfirmationMessage() {
        int count = (int) getNumberOfCashFlowsConnectedWithWallet(walletBuilder.getId());
        return MessageFormat.format(getActivity().getString(R.string.walletDeleteConfirmation), count);
    }

    private long getNumberOfCashFlowsConnectedWithWallet(Long id) {
        return localCashFlowService.countAssignedToWallet(id);
    }

    private boolean validateIfEmptyFields() {
        validateIfEmpty(walletName, getString(R.string.walletNameCantBeEmpty));
        validateIfEmpty(walletInitialAmount, getString(R.string.walletInitialAmountCantBeEmpty));
        return !isAnyErrorsAppear();
    }

    private void validateIfEmpty(TextView textView, String errorMsg) {
        if (Strings.isNullOrEmpty(textView.getText().toString())) {
            textView.setError(errorMsg);
        }
    }

    private void validateIfNameIsUnique() {
        Wallet found = localWalletService.findByNameAndType(walletName.getText().toString(), Wallet.Type.MY_WALLET);
        if (found != null && !found.getName().equals(originalName)) {
            walletName.setError(getString(R.string.walletNameHaveToBeUnique));
        }
    }

    private boolean isAnyErrorsAppear() {
        if (walletName.getError() != null) {
            return true;
        } else if (walletInitialAmount.getError() != null) {
            return true;
        }
        return false;
    }
}
