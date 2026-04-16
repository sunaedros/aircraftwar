package edu.hitsz.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.factory.AircraftFactory;
import edu.hitsz.factory.EliteEnemyFactory;
import edu.hitsz.factory.MobEnemyFactory;

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final long FRAME_DELAY_MS = 16L;
    private static final int TIME_INTERVAL = 20;
    private static final int CYCLE_DURATION = 600;
    private static final int ENEMY_MAX_NUMBER = 5;
    private static final int HERO_RESPAWN_HP = 1000;

    private final SurfaceHolder surfaceHolder;
    private final Paint hudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<AbstractAircraft> enemyAircrafts = new LinkedList<>();
    private final List<BaseBullet> heroBullets = new LinkedList<>();
    private final List<BaseBullet> enemyBullets = new LinkedList<>();

    private Thread renderThread;
    private volatile boolean running;
    private volatile boolean surfaceReady;

    private HeroAircraft heroAircraft;
    private int backgroundTop = 0;
    private int cycleTime = 0;
    private int score = 0;

    public GameSurfaceView(Context context) {
        super(context);
        ImageManager.initialize(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);

        hudPaint.setColor(Color.WHITE);
        hudPaint.setTextSize(42f);
        overlayPaint.setColor(Color.argb(110, 0, 0, 0));
    }

    @Override
    public void run() {
        while (running) {
            long frameStart = System.currentTimeMillis();
            updateGame();
            drawGame();
            long elapsed = System.currentTimeMillis() - frameStart;
            long sleepTime = FRAME_DELAY_MS - elapsed;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        surfaceReady = true;
        startLoopIfPossible();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Main.WINDOW_WIDTH = width;
        Main.WINDOW_HEIGHT = height;
        initializeGameState();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        surfaceReady = false;
        stopLoop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (heroAircraft == null) {
            return true;
        }
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            float halfWidth = heroAircraft.getWidth() / 2f;
            float halfHeight = heroAircraft.getHeight() / 2f;
            float x = clamp(event.getX(), halfWidth, Main.WINDOW_WIDTH - halfWidth);
            float y = clamp(event.getY(), halfHeight, Main.WINDOW_HEIGHT - halfHeight);
            heroAircraft.setLocation(x, y);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void pause() {
        stopLoop();
    }

    public void resume() {
        startLoopIfPossible();
    }

    private void startLoopIfPossible() {
        if (!surfaceReady || Main.WINDOW_WIDTH <= 0 || Main.WINDOW_HEIGHT <= 0 || running) {
            return;
        }
        if (heroAircraft == null) {
            initializeGameState();
        }
        running = true;
        renderThread = new Thread(this, "aircraft-war-loop");
        renderThread.start();
    }

    private void stopLoop() {
        running = false;
        if (renderThread == null) {
            return;
        }
        try {
            renderThread.join(500);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            renderThread = null;
        }
    }

    private void initializeGameState() {
        enemyAircrafts.clear();
        heroBullets.clear();
        enemyBullets.clear();
        cycleTime = 0;
        score = 0;
        backgroundTop = 0;

        heroAircraft = HeroAircraft.getHeroAircraft();
        heroAircraft.setLocation(Main.WINDOW_WIDTH / 2f, Main.WINDOW_HEIGHT * 0.82f);
        int heal = HERO_RESPAWN_HP - heroAircraft.getHp();
        if (heal > 0) {
            heroAircraft.decreaseHp(-heal);
        }
    }

    private void updateGame() {
        if (!surfaceReady || heroAircraft == null) {
            return;
        }
        backgroundTop = (backgroundTop + 6) % Main.WINDOW_HEIGHT;
        if (timeCountAndNewCycleJudge()) {
            createEnemies();
            shootAction();
        }
        bulletsMoveAction();
        aircraftsMoveAction();
        crashCheckAction();
        postProcessAction();
        if (heroAircraft.getHp() <= 0) {
            initializeGameState();
        }
    }

    private boolean timeCountAndNewCycleJudge() {
        cycleTime += TIME_INTERVAL;
        if (cycleTime >= CYCLE_DURATION) {
            cycleTime %= CYCLE_DURATION;
            return true;
        }
        return false;
    }

    private void createEnemies() {
        while (enemyAircrafts.size() < ENEMY_MAX_NUMBER) {
            AircraftFactory factory = Math.random() < 0.35 ? new EliteEnemyFactory() : new MobEnemyFactory();
            int x = randomInt(80, Math.max(81, Main.WINDOW_WIDTH - 80));
            int y = randomInt(60, Math.max(61, Main.WINDOW_HEIGHT / 6));
            int speedX = randomInt(-3, 4);
            int speedY = randomInt(8, 14);
            int hp = factory instanceof EliteEnemyFactory ? 90 : 30;
            enemyAircrafts.add(factory.createAircraft(x, y, speedX, speedY, hp));
        }
    }

    private void shootAction() {
        heroBullets.addAll(heroAircraft.shoot());
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            heroBullets.removeIf(BaseBullet::notValid);
            if (enemyAircraft instanceof EliteEnemy || Math.random() < 0.15) {
                enemyBullets.addAll(enemyAircraft.shoot());
            }
        }
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }

    private void crashCheckAction() {
        for (BaseBullet heroBullet : heroBullets) {
            if (heroBullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    continue;
                }
                if (enemyAircraft.crash(heroBullet)) {
                    enemyAircraft.decreaseHp(heroBullet.getPower());
                    heroBullet.vanish();
                    if (enemyAircraft.notValid()) {
                        score += 10;
                    }
                    break;
                }
            }
        }

        for (BaseBullet enemyBullet : enemyBullets) {
            if (enemyBullet.notValid()) {
                continue;
            }
            if (heroAircraft.crash(enemyBullet)) {
                heroAircraft.decreaseHp(enemyBullet.getPower());
                enemyBullet.vanish();
            }
        }

        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft.notValid()) {
                continue;
            }
            if (heroAircraft.crash(enemyAircraft)) {
                heroAircraft.decreaseHp(80);
                enemyAircraft.vanish();
            }
        }
    }

    private void postProcessAction() {
        removeInvalid(heroBullets);
        removeInvalid(enemyBullets);
        removeInvalid(enemyAircrafts);
    }

    private <T extends AbstractFlyingObject> void removeInvalid(List<T> objects) {
        Iterator<T> iterator = objects.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().notValid()) {
                iterator.remove();
            }
        }
    }

    private void drawGame() {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        try {
            drawBackground(canvas);
            drawFlyingObject(canvas, heroAircraft);
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                drawFlyingObject(canvas, enemyAircraft);
            }
            for (BaseBullet bullet : heroBullets) {
                drawFlyingObject(canvas, bullet);
            }
            for (BaseBullet bullet : enemyBullets) {
                drawFlyingObject(canvas, bullet);
            }
            drawHud(canvas);
        } finally {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        Bitmap background = ImageManager.BACKGROUND_IMAGE;
        if (background == null) {
            canvas.drawColor(Color.BLACK);
            return;
        }
        canvas.drawBitmap(background, null,
                new android.graphics.Rect(0, -backgroundTop, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT - backgroundTop), null);
        canvas.drawBitmap(background, null,
                new android.graphics.Rect(0, Main.WINDOW_HEIGHT - backgroundTop, Main.WINDOW_WIDTH,
                        Main.WINDOW_HEIGHT * 2 - backgroundTop), null);
    }

    private void drawFlyingObject(Canvas canvas, AbstractFlyingObject flyingObject) {
        if (flyingObject == null || flyingObject.notValid()) {
            return;
        }
        Bitmap bitmap = flyingObject.getImage();
        if (bitmap == null) {
            return;
        }
        float left = flyingObject.getLocationX() - bitmap.getWidth() / 2f;
        float top = flyingObject.getLocationY() - bitmap.getHeight() / 2f;
        canvas.drawBitmap(bitmap, left, top, null);
    }

    private void drawHud(Canvas canvas) {
        canvas.drawRect(0, 0, Main.WINDOW_WIDTH, 120, overlayPaint);
        canvas.drawText("Score: " + score, 32, 52, hudPaint);
        canvas.drawText("HP: " + heroAircraft.getHp(), 32, 102, hudPaint);
    }

    private static int randomInt(int minInclusive, int maxExclusive) {
        return minInclusive + (int) (Math.random() * Math.max(1, maxExclusive - minInclusive));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
