package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.strategy.ScatterShootStrategy;


public class ElitePlusEnemyFactory implements AircraftFactory{
    @Override
    public AbstractAircraft createAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        AbstractAircraft elitePlusEnemy = new ElitePlusEnemy(
                locationX,
                locationY,
                speedX,
                speedY,
                hp
        );
        elitePlusEnemy.setStrategy(new ScatterShootStrategy());
        return elitePlusEnemy;
    }
}