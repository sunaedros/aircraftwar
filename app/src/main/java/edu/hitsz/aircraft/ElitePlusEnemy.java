package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.factory.*;
import edu.hitsz.prop.AbstractProp;

import java.util.LinkedList;
import java.util.List;

/**
 * 精英敌机
 * 可射击
 *
 * @author hitsz
 */
public class ElitePlusEnemy extends AbstractAircraft {

    /**攻击方式 */

    /**
     * 子弹一次发射数量
     */
    private int shootNum = 3;

    /**
     * 子弹伤害
     */
    private int power = 30;

    /**
     * 子弹射击方向 (向上发射：1，向下发射：-1)
     */
    private int direction = 1;


    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT ) {
            vanish();
        }
    }

    @Override
    public List<BaseBullet> shoot() {
        return executeStrategy(locationX, locationY, speedX, speedY, direction);
    }

    public List<AbstractProp> dropProps() {
        List<AbstractProp> props = new LinkedList<>();

        double random = Math.random();
        PropFactory propFactory;
        if (random < 0.25) {
            propFactory = new BloodPropFactory();
        } else if (random < 0.5) {
            propFactory = new BombPropFactory();
        } else if (random < 0.75){
            propFactory = new BulletPropFactory();
        } else {
            propFactory = new SuperBulletPropFactory();
        }
        AbstractProp prop = propFactory.createProp(this.locationX, this.locationY, 0, 10);
        props.add(prop);

        return props;
    }
}