package com.tracv.model;


import com.tracv.game_util.PathBuilder;
import com.tracv.game_util.TerrainParser;
import com.tracv.gamecomponents.*;
import com.tracv.types.TerrainType;
import com.tracv.util.Constants;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

/**
 * Represents a mutable Map of the game which contains terrain and
 * game components
 * Rep Invariant:
 */
public class GameMap {
    private List<GameComponent> gameComponents;

    private Terrain[][] terrains;
    private Terrain start, destination;

    private List<Terrain> path;

    private Double blockSize;
    //private Base base;

    //sublists of gameComponents.
    private List<Tower> towers;
    private List<Enemy> enemies;
    private List<Projectile> projectiles;
    private List<Base> bases;

    private Map<Enemy, List<Projectile>> targetMap; //Holds enemies and projetiles targetting them.


    private PathBuilder pathBuilder;

    private ExecutorService pool;

    private Lock gcLock;

    public Lock getGcLock(){
        return gcLock;
    }

    /**
     * Constructor for GameMap, makes a new GameMap with the input terrainTypes containing
     * no GameComponents
     *
     */
    public GameMap () {
        pool = Executors.newFixedThreadPool(10);
        gameComponents = new ArrayList<>();
        reset();
    }

    public void loadLevel(String levelTerrainFile) {
        TerrainType[][] terrainTypes = TerrainParser.parseTerrainFile(levelTerrainFile);
        buildTerrain(terrainTypes);
        this.blockSize = Constants.DEFAULT_BLOCK_SIZE;
    }

    public void reset() {
        synchronized(gameComponents) {
            long startT = System.nanoTime();
            this.gameComponents.clear();
            towers = new ArrayList<>();
            enemies = new ArrayList<>();
            projectiles = new ArrayList<>();
            bases = new ArrayList<>();
            targetMap = new HashMap<>();
            path = new ArrayList<>();
            long endT = System.nanoTime();
        }
    }


    private void buildTerrain(TerrainType[][] terrainTypes) {

        //TODO allow mouse zoom.
        int width = (int)Math.round(Constants.DEFAULT_BLOCK_SIZE);
        int height = (int)Math.round(Constants.DEFAULT_BLOCK_SIZE);
        terrains = new Terrain[terrainTypes.length][terrainTypes[0].length];
        for(int i=0; i<terrainTypes.length; i++){
            for(int j=0; j<terrainTypes[i].length; j++){
                terrains[i][j] = new Terrain(terrainTypes[i][j], j, i, width, height);

                //TODO currently only supports 1 nexus.
                if(terrainTypes[i][j] == TerrainType.NEXUS){
                    System.out.println("Found base");
                    Base base = new Base(1000, terrains[i][j], null);
                    bases.add(base);
                    gameComponents.add(base);
                    destination = terrains[i][j];
                }else if(terrainTypes[i][j] == TerrainType.START){
                    System.out.println("Found start!");
                    start = terrains[i][j];
                }
            }
        }

        pathBuilder = new PathBuilder(terrains);
        path = pathBuilder.generatePath(start, destination);
    }
    public Terrain getStart(){
        return start;
    }


    private class Remover implements Runnable{
        private GameComponent gc;
        public Remover(GameComponent gc){
            this.gc = gc;
        }

        @Override
        public void run(){
            synchronized(gameComponents) {
                gameComponents.remove(gc);
            }
            if(gc instanceof Projectile){
                Projectile p = (Projectile) gc;
                synchronized(projectiles) {
                    projectiles.remove(p);
                }
                synchronized(targetMap){
                    Enemy e = p.getTarget();
                    if(e != null && targetMap.get(e) != null) {
                        //Enemy could've died and been removed from map.
                        targetMap.get(e).remove(p);
                    }
                }

            }else if(gc instanceof Enemy){
                Enemy e = (Enemy) gc;
                synchronized(enemies) {
                    enemies.remove(e);
                }
                synchronized(targetMap){
                    targetMap.remove(e);
                }

            }else if(gc instanceof Tower){
                synchronized(towers) {
                    towers.remove(gc);
                }

            }else if(gc instanceof Base){
                synchronized(bases) {
                    bases.remove(gc);
                }

            }else{
                System.out.println("Wrong component type");
            }

        }
    }
    private class Adder implements Runnable {
        private GameComponent gc;
        public Adder(GameComponent gc){
            this.gc = gc;
        }
        @Override
        public void run(){
            if(gc instanceof Projectile){
                Projectile p = (Projectile) gc;
                synchronized(projectiles){
                    projectiles.add((Projectile)gc);
                }
                synchronized(targetMap){
                    if(p.getTarget() == null) {
                        System.out.println("Oops, enemy died before the projectile fired, uhh... " +
                                "Technically should retarget, but we gonna toss it for now until we find an easier way to fix it... ");
                    }else {
                        targetMap.get(p.getTarget()).add(p);
                    }
                }

            }else if(gc instanceof Enemy){
                Enemy e = (Enemy) gc;
                e.setPath(path);
                synchronized(enemies) {
                    enemies.add(e);
                }
                synchronized(targetMap){
                    targetMap.put(e, new ArrayList<>());
                }

            }else if(gc instanceof Tower){
                synchronized(towers) {
                    towers.add((Tower) gc);
                }

            }else if(gc instanceof Base){
                synchronized(bases){
                    bases.add((Base)gc);
                }

            }else{
                System.out.println("Wrong component type");
            }

            synchronized(gameComponents) {
                gameComponents.add(gc);
            }
        }
    }
    /**
     * Adds the specific component to the GameMap
     * @param gc the component to add
     * @return true if the add operation was successful
     */
    public void addComponent(GameComponent gc) {
        pool.execute(new Adder(gc));
    }

    /**
     * Removes teh specific component from the GameMap
     * @param gc the component to remove
     * @return true if the remove operation was successful
     */
    public void removeComponent(GameComponent gc) {
        pool.execute(new Remover(gc));
    }

    /**
     * Getter for the current terrainTypes of the GameMap
     * @return a 2D array representing the terrainTypes
     */
    public Terrain[][] getTerrains() {
        return terrains;
    }

    public Double getBlockSize(){
        return blockSize;
    }

    public void setBlockSize(Double blockSize){
        this.blockSize = blockSize;

    }

    public void setTarget(Projectile p, Enemy e){
        synchronized(targetMap){
            targetMap.get(e).add(p);
        }
    }


    /**
     * Getter to return a list of the GameComponents in the GameMap
     * @return a list of GameComponents within the GameMap
     */
    public List<GameComponent> getGameComponents() {
        return gameComponents;
    }

    public Map<Enemy,List<Projectile>> getTargetMap() {
        return targetMap;
    }

    //TODO Fix this implementation
    public Base getBase() {
        return bases.get(0);
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public List<Tower> getTowers() {
        return towers;
    }


    public Dimension getMapSize() {
        double width = Constants.DEFAULT_BLOCK_SIZE * terrains[0].length;
        double height = Constants.DEFAULT_BLOCK_SIZE * terrains.length;
        return new Dimension((int)width, (int)height);
    }


}
