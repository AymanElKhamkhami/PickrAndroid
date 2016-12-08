package dmcs.pickr.models;

import java.io.Serializable;

/**
 * Created by Ayman on 02/12/2016.
 */
public class Preferences  implements Serializable{

    public boolean smoking = false;
    public boolean music = false;
    public boolean pets = false;
    public int talking = 0;

    public Preferences(boolean smoking, boolean music, boolean pets, int talking) {
        this.smoking = smoking;
        this.music = music;
        this.pets = pets;
        this.talking = talking;
    }

    public Preferences() {
    }

    public boolean isSmoking() {
        return smoking;
    }

    public void setSmoking(boolean smoking) {
        this.smoking = smoking;
    }

    public boolean isMusic() {
        return music;
    }

    public void setMusic(boolean music) {
        this.music = music;
    }

    public boolean isPets() {
        return pets;
    }

    public void setPets(boolean pets) {
        this.pets = pets;
    }

    public int getTalking() {
        return talking;
    }

    public void setTalking(int talking) {
        this.talking = talking;
    }
}
