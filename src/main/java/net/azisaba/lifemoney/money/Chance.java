package net.azisaba.lifemoney.money;

public interface Chance {

    int chance();

    default int getChance() {
        return chance();
    }

    void setChance(int chance);
}
