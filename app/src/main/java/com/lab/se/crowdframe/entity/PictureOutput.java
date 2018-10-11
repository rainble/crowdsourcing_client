package com.lab.se.crowdframe.entity;

/**
 * Created by lwh on 2017/4/7.
 */

public class PictureOutput extends OutputParent {
    boolean isActive;

    public PictureOutput(){
        isActive = false;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
