package edu.hitsz.aircraft;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.factory.*;
import edu.hitsz.prop.AbstractProp;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * 精英敌机
 * 可射击
 *
 * @author hitsz
 */
public class BossEnemy extends AbstractAircraft {

    /**攻击方式 */

    /**
     * 子弹一次发射数量
     */
    private int shootNum = 20;

    /**
     * 子弹伤害
     */
    private int power = 30;

    /**
     * 子弹射击方向 (向上发射：1，向下发射：-1)
     */
    private int direction = 1;


    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
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
        // 随机决定掉落道具的数量，比如0到3个
        Random rand = new Random();
        int numOfProps = rand.nextInt(4); // 0,1,2,3

        for (int i = 0; i < numOfProps; i++) {
            // 随机选择道具类型
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
            // 为了分散道具的位置，可以在x方向上随机偏移
            int offsetX = rand.nextInt(41) - 20; // -20到20的随机偏移
            int x = this.locationX + offsetX;
            int y = this.locationY;
            AbstractProp prop = propFactory.createProp(x, y, 0, 10);
            props.add(prop);
        }

        return props;
    }
}