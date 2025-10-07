// Stavite ovu klasu unutar ui.bosses paketa
package com.example.habittrackerrpg.logic;

import java.util.List;

public class PotentialRewardsInfo {
    private final String maxCoins;
    private final List<String> representativeItemIcons;

    public PotentialRewardsInfo(String maxCoins, List<String> representativeItemIcons) {
        this.maxCoins = maxCoins;
        this.representativeItemIcons = representativeItemIcons;
    }

    public String getMaxCoins() { return maxCoins; }
    public List<String> getRepresentativeItemIcons() { return representativeItemIcons; }
}