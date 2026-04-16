package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.strategy.StraightShootStrategy;


public class EliteEnemyFactory implements AircraftFactory{
    @Override
    public AbstractAircraft createAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        AbstractAircraft eliteEnemy = new EliteEnemy(
                locationX,
                locationY,
                speedX,
                speedY,
                hp
        );
        eliteEnemy.setStrategy(new StraightShootStrategy());
        return eliteEnemy;
    }
}