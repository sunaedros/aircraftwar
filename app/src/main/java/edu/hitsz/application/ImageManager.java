package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;
import java.util.Map;

import edu.hitsz.R;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.prop.BloodProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.BulletProp;
import edu.hitsz.prop.SuperBulletProp;

public final class ImageManager {

    private static final float ENTITY_SCALE = 2.0f;
    private static final Map<String, Bitmap> CLASSNAME_IMAGE_MAP = new HashMap<>();

    public static Bitmap BACKGROUND_IMAGE;
    public static Bitmap HERO_IMAGE;
    public static Bitmap HERO_BULLET_IMAGE;
    public static Bitmap ENEMY_BULLET_IMAGE;
    public static Bitmap MOB_ENEMY_IMAGE;
    public static Bitmap ELITE_ENEMY_IMAGE;
    public static Bitmap BOSS_ENEMY_IMAGE;
    public static Bitmap PROP_BLOOD_IMAGE;
    public static Bitmap PROP_BOMB_IMAGE;
    public static Bitmap PROP_BULLET_IMAGE;
    public static Bitmap PROP_BULLETPLUS_IMAGE;
    public static Bitmap ELITEPLUS_ENEMY_IMAGE;

    private static boolean initialized = false;

    private ImageManager() {
    }

    public static synchronized void initialize(Context context) {
        if (initialized) {
            return;
        }
        BACKGROUND_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg);
        HERO_IMAGE = loadScaledBitmap(context, R.drawable.hero);
        HERO_BULLET_IMAGE = loadScaledBitmap(context, R.drawable.bullet_hero);
        ENEMY_BULLET_IMAGE = loadScaledBitmap(context, R.drawable.bullet_enemy);
        MOB_ENEMY_IMAGE = loadScaledBitmap(context, R.drawable.mob);
        ELITE_ENEMY_IMAGE = loadScaledBitmap(context, R.drawable.elite);
        ELITEPLUS_ENEMY_IMAGE = loadScaledBitmap(context, R.drawable.elite_plus);
        BOSS_ENEMY_IMAGE = loadScaledBitmap(context, R.drawable.boss);
        PROP_BLOOD_IMAGE = loadScaledBitmap(context, R.drawable.prop_blood);
        PROP_BOMB_IMAGE = loadScaledBitmap(context, R.drawable.prop_bomb);
        PROP_BULLET_IMAGE = loadScaledBitmap(context, R.drawable.prop_bullet);
        PROP_BULLETPLUS_IMAGE = loadScaledBitmap(context, R.drawable.prop_bullet_plus);

        CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(), HERO_IMAGE);
        CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(), MOB_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EliteEnemy.class.getName(), ELITE_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(ElitePlusEnemy.class.getName(), ELITEPLUS_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BossEnemy.class.getName(), BOSS_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(), HERO_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(), ENEMY_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BloodProp.class.getName(), PROP_BLOOD_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BombProp.class.getName(), PROP_BOMB_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BulletProp.class.getName(), PROP_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(SuperBulletProp.class.getName(), PROP_BULLETPLUS_IMAGE);
        initialized = true;
    }

    public static Bitmap get(String className) {
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static Bitmap get(Object obj) {
        if (obj == null) {
            return null;
        }
        return get(obj.getClass().getName());
    }

    private static Bitmap loadScaledBitmap(Context context, int drawableResId) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableResId);
        int scaledWidth = Math.max(1, Math.round(bitmap.getWidth() * ENTITY_SCALE));
        int scaledHeight = Math.max(1, Math.round(bitmap.getHeight() * ENTITY_SCALE));
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
    }
}
