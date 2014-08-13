package info.korzeniowski.walletplus.drawermenu.cashflow;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.drawermenu.category.CategoryExpandableListAdapter;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.widget.OnContentClickListener;

@EFragment(R.layout.cashflow_details_fragment)
@OptionsMenu(R.menu.action_save)
public class CashFlowDetailsFragment extends Fragment {

    static final public String CASH_FLOW_ID = "CASH_FLOW_ID";

    @ViewById
    Spinner fromWallet;

    @ViewById
    Spinner toWallet;

    @ViewById
    EditText amount;

    @ViewById
    Button category;

    @ViewById
    EditText comment;

    @ViewById
    Button datePicker;

    @ViewById
    Button timePicker;

    @ViewById
    Switch recordType;

    @Inject @Named("local")
    CashFlowService localCashFlowService;

    @Inject @Named("local")
    WalletService localWalletService;

    @Inject @Named("local")
    CategoryService localCategoryService;

    @Inject @Named("amount")
    NumberFormat amountFormat;

    private DetailsType type;
    private CashFlow.Builder cashFlowBuilder;

    private Calendar calendar;
    private Category previousCategory;

    private List<Wallet> fromWalletList;
    private List<Wallet> toWalletList;
    private List<Category> categoryList;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @AfterViews
    void setupViews() {
        initFields();
        setupAdapters();
        setupListeners();
        fillViewsWithData();
    }

    private void initFields() {
        Long cashFlowId = getArguments().getLong(CASH_FLOW_ID);
        type = cashFlowId == 0L ? DetailsType.ADD : DetailsType.EDIT;
        calendar = Calendar.getInstance();
        cashFlowBuilder = new CashFlow.Builder(localCashFlowService.findById(cashFlowId));
        if (type.equals(DetailsType.EDIT)) {
            calendar.setTime(cashFlowBuilder.getDateTime());
        }
        fromWalletList = Lists.newArrayList();
        toWalletList = Lists.newArrayList();
        categoryList = Lists.newArrayList();
    }

    private void setupAdapters() {
        toWallet.setAdapter(new WalletAdapter(getActivity(), toWalletList));
        fromWallet.setAdapter(new WalletAdapter(getActivity(), fromWalletList));
    }

    private void setupListeners() {
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isDecimal(s)) {
                    int numberOfDigitsToDelete = getNumberOfDigitsToDelete(s);
                    s.delete(s.length() - numberOfDigitsToDelete, s.length());
                }
            }

            private int getNumberOfDigitsToDelete(Editable s) {
                int allowedNumberOfDigitsAfterComa = 2;
                int indexOfComa = s.toString().indexOf('.');
                if (indexOfComa < s.length() - 1 - allowedNumberOfDigitsAfterComa) {
                    return s.length() - indexOfComa - 1 - allowedNumberOfDigitsAfterComa;
                }
                return 0;
            }

            private boolean isDecimal(Editable s) {
                return s.toString().contains(".");
            }
        });
    }

    private void fillViewsWithData() {
        resetDatePicker();
        resetTimePicker();
        if (cashFlowBuilder.getAmount() != null) {
            amount.setText(amountFormat.format(cashFlowBuilder.getAmount()));
        }
        comment.setText(cashFlowBuilder.getComment());
        recordType.setChecked(cashFlowBuilder.build().isExpanse());
        refillLists();
        fromWallet.setSelection(fromWalletList.indexOf(cashFlowBuilder.getFromWallet()));
        toWallet.setSelection(toWalletList.indexOf(cashFlowBuilder.getToWallet()));
        notifyWalletAdapters();
        if (cashFlowBuilder.getCategory() != null) {
            category.setText(cashFlowBuilder.getCategory().getName());
        }
    }

    @CheckedChange
    void recordTypeCheckedChanged() {
        handleChangeCategory();
        notifyWalletAdapters();
    }

    private void handleChangeCategory() {
        swapCategoryWithPrevious();
        category.setText(getCategoryText(cashFlowBuilder.getCategory()));

        int selectedFromWalletPosition = fromWallet.getSelectedItemPosition();
        int selectedToWalletPosition = toWallet.getSelectedItemPosition();

        refillLists();

        toWallet.setSelection(selectedFromWalletPosition);
        fromWallet.setSelection(selectedToWalletPosition);
    }

    private void swapCategoryWithPrevious() {
        Category temp = previousCategory;
        previousCategory = cashFlowBuilder.getCategory();
        cashFlowBuilder.setCategory(temp);
    }

    private String getCategoryText(Category category) {
        if (category == null) {
            return getString(R.string.cashflowCategoryHint);
        }
        return category.getName();
    }

    private void refillLists() {
        fromWalletList.clear();
        toWalletList.clear();
        categoryList.clear();
        fillWalletLists();
        fillCategoryList();
    }

    private void fillWalletLists() {
        if (isExpanseType()) {
            fromWalletList.addAll(localWalletService.getMyWallets());
            toWalletList.addAll(localWalletService.getContractors());
        } else {
            fromWalletList.addAll(localWalletService.getContractors());
            toWalletList.addAll(localWalletService.getMyWallets());
        }
    }

    private void fillCategoryList() {
        if (isExpanseType()) {
            categoryList.addAll(localCategoryService.getMainExpenseTypeCategories());
        } else {
            categoryList.addAll(localCategoryService.getMainIncomeTypeCategories());
        }
    }

    public boolean isExpanseType() {
        return recordType.isChecked();
    }

    private void notifyWalletAdapters() {
        ((WalletAdapter) fromWallet.getAdapter()).notifyDataSetChanged();
        ((WalletAdapter) toWallet.getAdapter()).notifyDataSetChanged();
    }

    @Click
    void categoryClicked() {
        ExpandableListView expandableListView = new ExpandableListView(getActivity());

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.cashflowCategoryChooseAlertTitle))
                .setView(expandableListView)
                .create();

        expandableListView.setAdapter(new CategoryExpandableListAdapter(getActivity(), categoryList, new OnContentClickListener() {
            @Override
            public void onContentClick(Long id) {
                cashFlowBuilder.setCategory(localCategoryService.findById(id));
                category.setText(cashFlowBuilder.getCategory().getName());
                alertDialog.dismiss();
            }
        }));

        alertDialog.show();
    }

    private void resetDatePicker() {
        datePicker.setText(DateFormat.getDateFormat(getActivity()).format(calendar.getTime()));
    }

    private void resetTimePicker() {
        timePicker.setText(DateFormat.getTimeFormat(getActivity()).format(calendar.getTime()));
    }

    @Click
    void datePickerClicked() {
        new DatePickerDialog(
                getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        resetDatePicker();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    @Click
    void timePickerClicked() {
        new TimePickerDialog(
                getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        resetTimePicker();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(getActivity())
        ).show();
    }

    @OptionsItem(R.id.menu_save)
    void actionSave() {
        if (preValidations()) {
            getDataFromViews();
            boolean success = false;
            if (DetailsType.ADD.equals(type)) {
                success = tryInsert();
            } else if (DetailsType.EDIT.equals(type)) {
                success = tryUpdate();
            }
            if (success) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    public void getDataFromViews() {
        cashFlowBuilder.setAmount(Float.parseFloat(amount.getText().toString()));
        cashFlowBuilder.setDateTime(calendar.getTime());
        cashFlowBuilder.setFromWallet((Wallet) fromWallet.getSelectedItem());
        cashFlowBuilder.setToWallet((Wallet) toWallet.getSelectedItem());
        cashFlowBuilder.setComment(comment.getText().toString());
    }

    private boolean tryInsert() {
        localCashFlowService.insert(cashFlowBuilder.build());
        return true;
    }

    private boolean tryUpdate() {
        localCashFlowService.update(cashFlowBuilder.build());
        return true;
    }

    private boolean preValidations() {
        return validateAmount();
    }

    private boolean validateAmount() {
        if (Strings.isNullOrEmpty(amount.getText().toString())) {
            amount.setError("Amount can't be empty.");
            return false;
        }
        return true;
    }

    private enum DetailsType {ADD, EDIT}

    private class WalletAdapter extends BaseAdapter {
        List<Wallet> wallets;
        Context context;

        private WalletAdapter(Context context, List<Wallet> list) {
            super();
            this.context = context;
            wallets = list;
        }

        @Override
        public int getCount() {
            return wallets.size();
        }

        @Override
        public Wallet getItem(int position) {
            return wallets.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = new TextView(context);
                textView.setTextSize(getResources().getDimension(R.dimen.smallFontSize));
            } else {
                textView = (TextView) convertView;
            }
            textView.setText(getItem(position).getName());
            return textView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }

}
