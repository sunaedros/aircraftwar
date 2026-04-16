package edu.hitsz.strategy;

import edu.hitsz.bullet.BaseBullet;
import java.util.List;

public interface ShootStrategy {
    List<BaseBullet> shootAction(int locationX, int locationY, int speedX, int speedY,int direction);
}