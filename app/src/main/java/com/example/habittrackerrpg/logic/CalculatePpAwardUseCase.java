package com.example.habittrackerrpg.logic;

public class CalculatePpAwardUseCase {
    // Ova metoda računa NAGRADU za dostizanje određenog nivoa
    public long execute(int levelReached) {
        if (levelReached <= 1) {
            return 0;
        }
        if (levelReached == 2) {
            return 40; // Početna nagrada za prelazak sa nivoa 1 na 2
        }

        // Računamo nagradu za prethodni nivo da bismo izračunali trenutnu
        // Formula: PP nagrada prethodnog + 3/4 * PP nagrada prethodnog
        long previousAward = execute(levelReached - 1);
        return (long) (previousAward + (previousAward * 0.75));
    }
}