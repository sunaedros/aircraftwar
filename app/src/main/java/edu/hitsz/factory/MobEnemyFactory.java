package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.strategy.NoneShootStrategy;

public class MobEnemyFactory implements AircraftFactory{
    @Override
    public AbstractAircraft createAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        AbstractAircraft mobEnemy = new MobEnemy(
                locationX,
                locationY,
                speedX,
                speedY,
                hp
        );
        mobEnemy.setStrategy(new NoneShootStrategy());
        return mobEnemy;
    }
}