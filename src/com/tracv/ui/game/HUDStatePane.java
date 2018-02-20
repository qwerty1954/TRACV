package com.tracv.ui.game;

import com.tracv.model.GameState;
import com.tracv.observerpattern.Observable;
import com.tracv.observerpattern.Observer;
import com.tracv.swing.Label;
import com.tracv.swing.Pane;
import com.tracv.util.Comp;
import com.tracv.util.Constants;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Holds details fo the game state ie. Gold/Time
 *
 */
public class HUDStatePane extends Pane implements Observer {

    private HUDPane hudPane;
    private GameState gs;

    private Label lGold;
    private Label lTime;
    private Label lLevel;
    private Label lWave;


    public HUDStatePane(HUDPane hudPane, GameState gs) {
        this.hudPane = hudPane;
        this.gs = gs;

        lGold = new Label("Gold ", Label.MEDIUM, Label.LEFT, Label.INVISIBLE);
        lTime = new Label("Time ", Label.MEDIUM, Label.LEFT, Label.INVISIBLE);

        lLevel = new Label("Level ", Label.MEDIUM, Label.LEFT, Label.INVISIBLE);
        lWave = new Label("Wave ", Label.MEDIUM, Label.LEFT, Label.INVISIBLE);

        Comp.add(lGold, this, 0, 0, 1, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER,
                Constants.INSETS_BETWEEN, Constants.INSETS_OUTSIDE, Constants.INSETS_BETWEEN, Constants.INSETS_OUTSIDE);
        Comp.add(lTime, this, 0, 1, 1, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER,
                Constants.INSETS_BETWEEN, Constants.INSETS_OUTSIDE, Constants.INSETS_BETWEEN, Constants.INSETS_OUTSIDE);
        Comp.add(lLevel, this, 0, 2, 1, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER,
                Constants.INSETS_BETWEEN, Constants.INSETS_OUTSIDE, Constants.INSETS_BETWEEN, Constants.INSETS_OUTSIDE);
        Comp.add(lWave, this, 0, 3, 1, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER,
                Constants.INSETS_BETWEEN, Constants.INSETS_OUTSIDE, Constants.INSETS_BETWEEN, Constants.INSETS_OUTSIDE);

        this.setBackground(Color.WHITE);
        this.setBorder(new LineBorder(Color.YELLOW, 1));
    }

    @Override
    public void update(Observable o, String msg) {
        if(o == gs){
            if(msg.equals(Constants.OBSERVER_GOLD_CHANGED)){
                updateGold();
            }else if(msg.equals(Constants.OBSERVER_NEW_GAME)){
                updateGold();
                updateTime();
                updateWave();
                updateLevel();
            }else if(msg.equals(Constants.OBSERVER_TIME_MODIFIED)){
                updateTime();
            }else if(msg.equals(Constants.OBSERVER_WAVE_SPAWNED)){
                updateWave();
            }
        }
    }

    private void updateTime(){
        int time = gs.getTime()/1000; //gs time is in milliseconds.
        int minutes = time/60;
        int seconds = time % 60;
        String sMin, sSec;
        if(minutes < 10){
            sMin = "0" + String.valueOf(minutes);
        }else{
            sMin = String.valueOf(minutes);
        }
        if(seconds < 10){
            sSec = "0" + String.valueOf(seconds);
        }else{
            sSec = String.valueOf(seconds);
        }

        SwingUtilities.invokeLater(()->lTime.setText("Time " + sMin + ":" + sSec));

    }
    private void updateGold(){
        SwingUtilities.invokeLater(()->lGold.setText("Gold " + gs.getGold()));
    }
    private void updateWave(){
        SwingUtilities.invokeLater(()->lWave.setText("Wave " + gs.getWave()));
    }
    private void updateLevel(){
        SwingUtilities.invokeLater(()->lLevel.setText("Level " + gs.getLevel()));
    }
}
