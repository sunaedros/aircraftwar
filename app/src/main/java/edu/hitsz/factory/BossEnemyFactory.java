package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.strategy.CircleShootStrategy;


public class BossEnemyFactory implements AircraftFactory{
    @Override
    public AbstractAircraft createAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        AbstractAircraft bossEnemy = new BossEnemy(
                locationX,
                locationY,
                speedX,
                speedY,
                hp
        );
        bossEnemy.setStrategy(new CircleShootStrategy());
        return bossEnemy;
    }
}