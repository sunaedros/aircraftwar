package edu.hitsz.prop;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.music.MusicManager;


public class BombProp extends AbstractProp{

    public BombProp(int locationX, int locationY, int speedX, int speedY){
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void effect(AbstractAircraft aircraft) {
        MusicManager.playBomb();
        System.out.println("BombSupply active!");
    };

}