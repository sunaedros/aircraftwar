package edu.hitsz.prop;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.music.MusicManager;
import edu.hitsz.strategy.CircleShootStrategy;
import edu.hitsz.strategy.StraightShootStrategy;


public class SuperBulletProp extends AbstractProp{

    private boolean effectCalled = false;
    public SuperBulletProp(int locationX, int locationY, int speedX, int speedY){
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public synchronized void effect(AbstractAircraft aircraft){
        MusicManager.playBullet();
        System.out.println("SuperBulletSupply active!");
        if(!effectCalled){
            effectCalled = true;
            //定义新线程
            Runnable r = ()->{
                aircraft.setStrategy(new CircleShootStrategy());

                //System.out.println("Bullet change!");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                aircraft.setStrategy(new StraightShootStrategy());

            };

            //启动新线程
            new Thread(r).start();
        }
    }
}