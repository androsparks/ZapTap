package com.betterclever.zaptap.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.betterclever.zaptap.objects.TutorialOverlay;
import com.betterclever.zaptap.utility.Constants;
import com.betterclever.zaptap.utility.FontsUtilty;
import com.betterclever.zaptap.ZapTapGame;
import com.betterclever.zaptap.objects.Button;
import com.betterclever.zaptap.objects.CircleButton;
import com.betterclever.zaptap.objects.DevelopersOverlay;
import com.betterclever.zaptap.objects.EarnZapperButton;
import com.betterclever.zaptap.objects.Ripple;
import com.betterclever.zaptap.objects.SoundOnOffButton;
import com.betterclever.zaptap.objects.Zappers;

/**
 * Created by betterclever on 05/01/17.
 */

public class HomeScreen extends InputAdapter implements Screen {

    private static final String TAG = HomeScreen.class.getSimpleName();

    private ExtendViewport viewport;

    private Ripple playRippleButton;

    private DevelopersOverlay developersOverlay;
    private TutorialOverlay tutorialOverlay;

    private Color backgroundColor;
    private Color destColor;
    private ColorAction colorAction;
    private float counter;

    private SpriteBatch batch;
    private ShapeRenderer renderer;

    private Texture scoreTrophy;
    private Texture achievementMedal;
    private ZapTapGame game;

    private CircleButton leaderBoard;
    private CircleButton firstButton;

    private Button[] buttons;
    private Zappers zappers;
    private EarnZapperButton earnZapperButton;
    private SoundOnOffButton soundOnOffButton;

    private Rectangle infoButtonBounds;
    private Rectangle infoCloseBounds;

    private boolean developerScreenShown = false;

    private Preferences preferences;

    public HomeScreen(ZapTapGame zapTapGame) {
        game = zapTapGame;
    }

    boolean playTapped = false;
    boolean hardLocked,mediumLocked,insaneLocked;

    @Override
    public void show() {

        Gdx.input.setInputProcessor(this);
        Gdx.input.setCatchBackKey(true);

        infoButtonBounds = new Rectangle(0,0,100,100);
        infoCloseBounds = new Rectangle(Constants.WORLD_WIDTH-100,Constants.WORLD_HEIGHT-100,100,100);
        backgroundColor = new Color(Color.TEAL);
        destColor = new Color(Color.MAGENTA);
        colorAction = new ColorAction();
        colorAction.setColor(backgroundColor);
        colorAction.setDuration(2f);
        colorAction.setEndColor(destColor);

        scoreTrophy = new Texture("trophy.png");
        achievementMedal = new Texture("medal.png");

        renderer = new ShapeRenderer();
        playRippleButton = new Ripple(renderer,
                            new Vector2(Constants.WORLD_WIDTH/2,Constants.WORLD_HEIGHT/3),
                            60,
                            100);

        leaderBoard = new CircleButton(renderer,3*Constants.WORLD_WIDTH/4,Constants.WORLD_HEIGHT/3,60);
        firstButton = new CircleButton(renderer,Constants.WORLD_WIDTH/4,Constants.WORLD_HEIGHT/3,60);

        batch = new SpriteBatch();
        viewport = new ExtendViewport(Constants.WORLD_WIDTH,Constants.WORLD_HEIGHT);

        developersOverlay = new DevelopersOverlay(renderer,batch);
        tutorialOverlay = new TutorialOverlay(batch,renderer);

        preferences = Gdx.app.getPreferences(Constants.PREF_KEY);
        zappers = new Zappers(batch,renderer,preferences,
                new Vector2(10,Constants.WORLD_HEIGHT-70));
        earnZapperButton = new EarnZapperButton(batch,renderer);


        soundOnOffButton = new SoundOnOffButton(batch,preferences);

        initModeLockStates();

        buttons = new Button[4];
        buttons[0] = new Button(renderer,10,10,180,60,"Easy",false,
                        FontsUtilty.MODE_BUTTON_FONT,batch);
        buttons[1] = new Button(renderer,10 +200,10,180,60,"Medium",mediumLocked,
                        FontsUtilty.MODE_BUTTON_FONT,batch);
        buttons[2] = new Button(renderer,10 +400,10,180,60,"Hard",hardLocked,
                        FontsUtilty.MODE_BUTTON_FONT,batch);
        buttons[3] = new Button(renderer,10 +600,10,180,60,"Insane",insaneLocked,
                        FontsUtilty.MODE_BUTTON_FONT,batch);

    }

    private void initModeLockStates() {
        if(preferences.getBoolean(Constants.MEDIUM_LOCKED,true)){
            mediumLocked = true;
            preferences.putBoolean(Constants.MEDIUM_LOCKED,true).flush();
        }
        else {
            mediumLocked = false;
        }
        if(preferences.getBoolean(Constants.HARD_LOCKED,true)){
            hardLocked = true;
            preferences.putBoolean(Constants.HARD_LOCKED,true).flush();
        }
        else {
            hardLocked = false;
        }
        if(preferences.getBoolean(Constants.INSANE_LOCKED,true)){
            insaneLocked = true;
            preferences.putBoolean(Constants.INSANE_LOCKED,true).flush();
        }else {
            insaneLocked = false;
        }
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(backgroundColor.r,backgroundColor.g,backgroundColor.b,backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        renderer.setProjectionMatrix(viewport.getCamera().combined);

        playRippleButton.render(delta);

        firstButton.render(delta);
        leaderBoard.render(delta);

        chameleonizeTheBackground(delta);
        writeLogo();

        soundOnOffButton.render(delta);

        batch.begin();
        batch.draw(scoreTrophy,3*Constants.WORLD_WIDTH/4-30,Constants.WORLD_HEIGHT/4+10,60,60);
        batch.draw(achievementMedal,Constants.WORLD_WIDTH/4-30,Constants.WORLD_HEIGHT/4+10,60,60);
        batch.end();


        if(playTapped) {
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].render(delta);
            }
        }

        zappers.render(delta);

        if(developerScreenShown) {
            developersOverlay.render(delta);
        }
        if(!playTapped && !developerScreenShown) {
            batch.begin();
            batch.draw(Constants.INFORMATION_IMAGE, 20, 10, 50, 50);
            batch.end();
        }

    }

    private void writeLogo() {
        batch.begin();
        FontsUtilty.HOMESCREEN_LOGO_FONT.draw(batch,"Zap Tap",Constants.WORLD_WIDTH/4,5*Constants.WORLD_HEIGHT/7);
        if(playTapped) {
            FontsUtilty.PLAY_BUTTON_FONT.draw(batch, "MODE", Constants.WORLD_WIDTH / 2 - 58, Constants.WORLD_HEIGHT / 3 + 10);
        }
        else {
            FontsUtilty.PLAY_BUTTON_FONT.draw(batch, "PLAY", Constants.WORLD_WIDTH / 2 - 55, Constants.WORLD_HEIGHT / 3 + 10);
        }
        batch.end();
    }

    private void chameleonizeTheBackground(float delta) {
        counter+= delta;

        colorAction.act(delta);

        if(counter>2){
            counter-=2;
            destColor = destColor.set(MathUtils.random(0.3f,0.8f),MathUtils.random(0.3f,0.8f),MathUtils.random(0.3f,0.8f),1);
            colorAction.reset();
            colorAction.setColor(backgroundColor);
            colorAction.setDuration(2f);
            colorAction.setEndColor(destColor);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width,height,true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        boolean value = super.touchDown(screenX, screenY, pointer, button);

        Vector2 position = viewport.unproject(new Vector2(screenX,screenY));

        if(!playTapped){
            if(infoButtonBounds.contains(position)){
                developerScreenShown = true;
                return value;
            }
            if(developersOverlay.githubBounds.contains(position)){
                Gdx.net.openURI("https://github.com/betterclever");
            }
            if(developersOverlay.facebookBounds.contains(position)){
                Gdx.net.openURI("https://facebook.com/betterclever");
            }
            if(developersOverlay.googleplusBounds.contains(position)){
                Gdx.net.openURI("https://plus.google.com/u/0/104046348939694351090");
            }
        }

        if(developerScreenShown){
            if(infoCloseBounds.contains(position)){
                developerScreenShown = false;
            }
            return value;
        }

        if(playRippleButton.isTouched(position)){
            playTapped = !playTapped;
            initModeLockStates();
        }

        if(soundOnOffButton.isTouched(position)){
            soundOnOffButton.toggle();
        }

        if(firstButton.isTouched(position)){
            game.getPlatformHelper().showAchievement();
            game.getPlatformHelper().submitAllScores();
        }

        if(leaderBoard.isTouched(position)){
            game.getPlatformHelper().showScore();
        }

        if(playTapped){
            for (int i = 0; i < buttons.length; i++) {
                if(buttons[i].isTouched(position)){
                    playTapped = false;
                    game.startPlay(i);
                }
            }
        }

        return value;
    }

    @Override
    public boolean keyDown(int keycode) {
        boolean s =  super.keyDown(keycode);
        if(keycode == Input.Keys.BACK){
            game.exit();
        }
        return s;
    }
}
