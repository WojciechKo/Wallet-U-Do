package info.korzeniowski.walletplus.model;

import com.j256.ormlite.field.DatabaseField;

import java.util.List;

public class Wallet {
    public enum Type {MY_WALLET, CONTRACTOR}

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(uniqueIndex = true, canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private Type type;

    @DatabaseField(canBeNull = false)
    private Double initialAmount;

    @DatabaseField(canBeNull = false)
    private Double currentAmount;

    public Wallet() {

    }

    public Wallet(String name, Type type, Double initialAmount, Double currentAmount) {
        this.name = name;
        this.type = type;
        this.initialAmount = initialAmount;
        this.currentAmount = currentAmount;
    }

    public Long getId() {
        return id;
    }

    public Wallet setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Wallet setName(String name) {
        this.name = name;
        return this;
    }

    public Double getInitialAmount() {
        return initialAmount;
    }

    public Wallet setInitialAmount(Double initialAmount) {
        this.initialAmount = initialAmount;
        return this;
    }

    public Double getCurrentAmount() {
        return currentAmount;
    }

    public Wallet setCurrentAmount(Double currentAmount) {
        this.currentAmount = currentAmount;
        return this;
    }

    public Type getType() {
        return type;
    }

    public Wallet setType(Type type) {
        this.type = type;
        return this;
    }

    public static Wallet findById(List<Wallet> wallets, Long id) {
        for (Wallet wallet : wallets) {
            if (id.equals(wallet.getId())) {
                return wallet;
            }
        }
        return null;
    }
}
