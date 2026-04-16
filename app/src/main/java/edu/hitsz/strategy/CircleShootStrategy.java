package edu.hitsz.strategy;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

public class CircleShootStrategy implements ShootStrategy{
    @Override
    public List<BaseBullet> shootAction(int locationX, int locationY, int speedX, int speedY, int direction) {
        int shootNum = 12; // 环形子弹数量
        int power = (direction > 0) ? 20 : 30; // 根据方向设置子弹威力 (敌机: 20, 英雄机: 30)
        List<BaseBullet> res = new LinkedList<>();
        int bulletSpeed = 5; // 子弹基础速度，决定子弹向外扩散的速度
        int initialOffset = 30; // 子弹初始发射偏移量，使其从飞机周围发射

        BaseBullet bullet;

        for(int i = 0; i < shootNum; i++) {
            // 计算每个子弹的角度（均匀分布在一个完整的圆周上）
            double angle = 2 * Math.PI * i / shootNum;

            // 计算子弹的初始位置，相对于飞机中心进行偏移
            // 这样子弹会从飞机周围的一个圆上发射
            int bulletX = locationX + (int)(initialOffset * Math.cos(angle));
            int bulletY = locationY + (int)(initialOffset * Math.sin(angle));

            // 计算子弹的速度分量
            // 子弹自身的速度方向是向外扩散的，并叠加飞机的速度
            // 移除 Math.signum(direction)，让子弹的垂直速度也能自由扩散
            int bulletSpeedX = (int)(bulletSpeed * Math.cos(angle)) + speedX;
            int bulletSpeedY = (int)(bulletSpeed * Math.sin(angle)) + speedY; // 修正：不再乘以 Math.signum(direction)

            if (direction > 0) { // 敌机
                bullet = new EnemyBullet(bulletX, bulletY, bulletSpeedX, bulletSpeedY, power);
            } else { // 英雄机
                bullet = new HeroBullet(bulletX, bulletY, bulletSpeedX, bulletSpeedY, power);
            }
            res.add(bullet);
        }
        return res;
    }
}