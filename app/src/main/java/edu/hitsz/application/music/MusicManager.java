package edu.hitsz.application.music;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

import edu.hitsz.R;

public final class MusicManager {

    private static final int MAX_STREAMS = 4;

    private static Context appContext;
    private static MediaPlayer bgmPlayer;
    private static MediaPlayer bossPlayer;
    private static SoundPool soundPool;
    private static boolean initialized = false;
    private static boolean audioEnabled = true;
    private static boolean bossMode = false;
    private static boolean lifecyclePaused = false;
    private static int bulletSoundId;
    private static int hitSoundId;
    private static int supplySoundId;
    private static int bombSoundId;
    private static int gameOverSoundId;

    private MusicManager() {
    }

    public static synchronized void initialize(Context context) {
        if (initialized) {
            return;
        }
        appContext = context.getApplicationContext();
        bgmPlayer = createLoopPlayer(R.raw.bgm);
        bossPlayer = createLoopPlayer(R.raw.bgm_boss);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(MAX_STREAMS)
                .build();
        bulletSoundId = soundPool.load(appContext, R.raw.bullet, 1);
        hitSoundId = soundPool.load(appContext, R.raw.bullet_hit, 1);
        supplySoundId = soundPool.load(appContext, R.raw.get_supply, 1);
        bombSoundId = soundPool.load(appContext, R.raw.bomb_explosion, 1);
        gameOverSoundId = soundPool.load(appContext, R.raw.game_over, 1);
        initialized = true;
    }

    public static synchronized void setAudioEnabled(boolean enabled) {
        audioEnabled = enabled;
        if (!initialized) {
            return;
        }
        if (!enabled) {
            pauseAllLoops();
        } else if (!lifecyclePaused) {
            syncLoopPlayback();
        }
    }

    public static boolean isAudioEnabled() {
        return audioEnabled;
    }

    public static void onHostResume() {
        lifecyclePaused = false;
        syncLoopPlayback();
    }

    public static void onHostPause() {
        lifecyclePaused = true;
        pauseAllLoops();
    }

    public static synchronized void release() {
        pauseAllLoops();
        if (bgmPlayer != null) {
            bgmPlayer.release();
            bgmPlayer = null;
        }
        if (bossPlayer != null) {
            bossPlayer.release();
            bossPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        initialized = false;
        appContext = null;
    }

    public static void playBgm() {
        bossMode = false;
        syncLoopPlayback();
    }

    public static void playBossBgm() {
        bossMode = true;
        syncLoopPlayback();
    }

    public static void playHitEnemyPlayer() {
        playEffect(hitSoundId);
    }

    public static void playSupply() {
        playEffect(supplySoundId);
    }

    public static void playBomb() {
        playEffect(bombSoundId);
    }

    public static void playBullet() {
        playEffect(bulletSoundId);
    }

    public static void playOverBgm() {
        playEffect(gameOverSoundId);
    }

    public static void overBgm() {
        pausePlayer(bgmPlayer);
    }

    public static void overBossBgm() {
        pausePlayer(bossPlayer);
    }

    private static void syncLoopPlayback() {
        if (!initialized || !audioEnabled || lifecyclePaused) {
            return;
        }
        MediaPlayer activePlayer = bossMode ? bossPlayer : bgmPlayer;
        MediaPlayer inactivePlayer = bossMode ? bgmPlayer : bossPlayer;
        pausePlayer(inactivePlayer);
        startPlayer(activePlayer);
    }

    private static void pauseAllLoops() {
        pausePlayer(bgmPlayer);
        pausePlayer(bossPlayer);
    }

    private static MediaPlayer createLoopPlayer(int resId) {
        MediaPlayer player = MediaPlayer.create(appContext, resId);
        if (player != null) {
            player.setLooping(true);
        }
        return player;
    }

    private static void startPlayer(MediaPlayer player) {
        if (player == null || player.isPlaying()) {
            return;
        }
        player.start();
    }

    private static void pausePlayer(MediaPlayer player) {
        if (player == null || !player.isPlaying()) {
            return;
        }
        player.pause();
        player.seekTo(0);
    }

    private static void playEffect(int soundId) {
        if (!initialized || !audioEnabled || soundPool == null || soundId == 0) {
            return;
        }
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
    }
}
