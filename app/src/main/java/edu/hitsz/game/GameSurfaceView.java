package edu.hitsz.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.application.music.MusicManager;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.factory.AircraftFactory;
import edu.hitsz.factory.BossEnemyFactory;
import edu.hitsz.factory.EliteEnemyFactory;
import edu.hitsz.factory.MobEnemyFactory;

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final long FRAME_DELAY_MS = 16L;
    private static final int TIME_INTERVAL = 20;
    private static final int HERO_RESPAWN_HP = 100;
    private static final int BOSS_SCORE_THRESHOLD = 120;
    private static final int BOSS_HP = 360;
    private static final float HUD_TEXT_SIZE_SP = 24f;
    private static final float HUD_PADDING_DP = 16f;
    private static final float HUD_LINE_GAP_DP = 8f;

    private final SurfaceHolder surfaceHolder;
    private final Paint hudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect backgroundDestRect = new Rect();
    private final Rect backgroundDestRect2 = new Rect();
    private final GameDifficulty difficulty;

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
    private int nextBossScore = BOSS_SCORE_THRESHOLD;
    private float density;
    private int statusBarInsetTop;
    private float hudPadding;
    private float hudLineGap;
    private float hudTextBaseline;
    private float hudSecondLineBaseline;
    private float hudPanelBottom;

    public GameSurfaceView(Context context, GameDifficulty difficulty) {
        super(context);
        this.difficulty = difficulty;
        ImageManager.initialize(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);
        setKeepScreenOn(true);
        density = getResources().getDisplayMetrics().density;
        statusBarInsetTop = resolveStatusBarInsetTop();
        hudPadding = dpToPx(HUD_PADDING_DP);
        hudLineGap = dpToPx(HUD_LINE_GAP_DP);

        hudPaint.setColor(Color.WHITE);
        hudPaint.setTextSize(spToPx(HUD_TEXT_SIZE_SP));
        hudPaint.setFakeBoldText(true);
        overlayPaint.setColor(Color.argb(110, 0, 0, 0));
        updateHudMetrics();
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
        updateHudMetrics();
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
        nextBossScore = BOSS_SCORE_THRESHOLD;

        heroAircraft = HeroAircraft.getHeroAircraft();
        heroAircraft.revive();
        heroAircraft.setLocation(Main.WINDOW_WIDTH / 2f, Main.WINDOW_HEIGHT * 0.82f);
        int heal = HERO_RESPAWN_HP - heroAircraft.getHp();
        if (heal > 0) {
            heroAircraft.decreaseHp(-heal);
        }
        MusicManager.playBgm();
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
            MusicManager.playOverBgm();
            initializeGameState();
            return;
        }
        syncBackgroundMusic();
    }

    private boolean timeCountAndNewCycleJudge() {
        cycleTime += TIME_INTERVAL;
        if (cycleTime >= difficulty.getCycleDuration()) {
            cycleTime %= difficulty.getCycleDuration();
            return true;
        }
        return false;
    }

    private void createEnemies() {
        if (score >= nextBossScore && !hasBossEnemy()) {
            spawnBossEnemy();
            nextBossScore += BOSS_SCORE_THRESHOLD;
        }
        while (enemyAircrafts.size() < difficulty.getEnemyMaxNumber()) {
            AircraftFactory factory = Math.random() < difficulty.getEliteProbability()
                    ? new EliteEnemyFactory() : new MobEnemyFactory();
            int x = randomInt(80, Math.max(81, Main.WINDOW_WIDTH - 80));
            int y = randomInt(60, Math.max(61, Main.WINDOW_HEIGHT / 6));
            int speedX = randomInt(-3, 4);
            int speedY = randomInt(difficulty.getMinEnemySpeed(), difficulty.getMaxEnemySpeed() + 1);
            int hp = factory instanceof EliteEnemyFactory ? difficulty.getEliteEnemyHp() : difficulty.getMobEnemyHp();
            enemyAircrafts.add(factory.createAircraft(x, y, speedX, speedY, hp));
        }
    }

    private void shootAction() {
        heroBullets.addAll(heroAircraft.shoot());
        MusicManager.playBullet();
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            heroBullets.removeIf(BaseBullet::notValid);
            if (enemyAircraft instanceof BossEnemy || enemyAircraft instanceof EliteEnemy || Math.random() < 0.15) {
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
                        MusicManager.playHitEnemyPlayer();
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
                MusicManager.playHitEnemyPlayer();
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
        backgroundDestRect.set(0, -backgroundTop, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT - backgroundTop);
        backgroundDestRect2.set(0, Main.WINDOW_HEIGHT - backgroundTop, Main.WINDOW_WIDTH,
                Main.WINDOW_HEIGHT * 2 - backgroundTop);
        canvas.drawBitmap(background, null, backgroundDestRect, null);
        canvas.drawBitmap(background, null, backgroundDestRect2, null);
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
        canvas.drawRect(0, 0, Main.WINDOW_WIDTH, hudPanelBottom, overlayPaint);
        canvas.drawText("Mode: " + difficulty.getLabel(), hudPadding, hudTextBaseline, hudPaint);
        canvas.drawText("Score: " + score, hudPadding + dpToPx(180), hudTextBaseline, hudPaint);
        canvas.drawText("HP: " + heroAircraft.getHp(), hudPadding, hudSecondLineBaseline, hudPaint);
    }

    private void spawnBossEnemy() {
        AircraftFactory bossFactory = new BossEnemyFactory();
        enemyAircrafts.add(bossFactory.createAircraft(
                Main.WINDOW_WIDTH / 2,
                Math.max(120, Main.WINDOW_HEIGHT / 10),
                5,
                4,
                BOSS_HP
        ));
        MusicManager.playBossBgm();
    }

    private boolean hasBossEnemy() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (!enemyAircraft.notValid() && enemyAircraft instanceof BossEnemy) {
                return true;
            }
        }
        return false;
    }

    private void syncBackgroundMusic() {
        if (hasBossEnemy()) {
            MusicManager.playBossBgm();
        } else {
            MusicManager.playBgm();
        }
    }

    private static int randomInt(int minInclusive, int maxExclusive) {
        return minInclusive + (int) (Math.random() * Math.max(1, maxExclusive - minInclusive));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void updateHudMetrics() {
        Paint.FontMetrics fontMetrics = hudPaint.getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top;
        hudTextBaseline = statusBarInsetTop + hudPadding - fontMetrics.top;
        hudSecondLineBaseline = hudTextBaseline + textHeight + hudLineGap;
        hudPanelBottom = hudSecondLineBaseline + fontMetrics.bottom + hudPadding;
    }

    private int resolveStatusBarInsetTop() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId == 0) {
            return Math.round(24 * density);
        }
        return getResources().getDimensionPixelSize(resourceId);
    }

    private float dpToPx(float dp) {
        return dp * density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
