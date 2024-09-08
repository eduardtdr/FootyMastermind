package com.example.footymastermind;

import java.lang.String;

public class GameModel {
    String gameName;
    String gameCategory;
    String gameDescription;
    int image;

    public GameModel(String gameName, String gameCategory, String gameDescription, int image) {
        this.gameName = gameName;
        this.gameCategory = gameCategory;
        this.gameDescription = gameDescription;
        this.image = image;
    }


    public String getGameName() {
        return gameName;
    }

    public String getGameCategory() {
        return gameCategory;
    }

    public String getGameDescription() {
        return gameDescription;
    }

    public int getImage() {
        return image;
    }
}
