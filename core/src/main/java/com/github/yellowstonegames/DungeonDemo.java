package com.github.yellowstonegames;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.LineWobble;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.FullPalette;
import com.github.yellowstonegames.files.Config;
import com.github.yellowstonegames.glyph.GlyphActor;
import com.github.yellowstonegames.glyph.GlyphGrid;
import com.github.yellowstonegames.glyph.MoreActions;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.mobs.Mob;
import com.github.yellowstonegames.path.DijkstraMap;
import com.github.yellowstonegames.place.DungeonProcessor;
import com.github.yellowstonegames.text.Language;

import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.Input.Keys.*;
import static com.github.yellowstonegames.core.DescriptiveColor.*;

/**
 * This is from another demo, meant to serve as a basis for later changes.
 */
@SuppressWarnings("ReassignedVariable") // A gift for you, Eben.
public class DungeonDemo extends ApplicationAdapter {
    private Stage stage;
    private GlyphGrid gg;
    private DungeonProcessor dungeonProcessor;
    private char[][] bare, dungeon, prunedDungeon;
    private float[][] res, light;
    private Region seen, inView, blockage;
    private final Noise waves = new Noise(123, 0.5f, Noise.FOAM, 1);
    private Mob player;
    private CoordObjectOrderedMap<Mob> enemies;
    private DijkstraMap playerToCursor;
    private final ObjectList<Coord> toCursor = new ObjectList<>(100);
    private final ObjectList<Coord> awaitedMoves = new ObjectList<>(50);
    private Coord cursor = Coord.get(-1, -1);
    private final Vector2 pos = new Vector2();
    private Runnable post;

    private Config config;

    private static final int DEEP_OKLAB = describeOklab("dark dull cobalt");
    private static final int SHALLOW_OKLAB = describeOklab("dull denim");
    private static final int GRASS_OKLAB = describeOklab("duller dark green");
    private static final int DRY_OKLAB = describeOklab("dull light apricot sage");
    private static final int STONE_OKLAB = describeOklab("darkmost gray dullest bronze");
    private static final int deepText = toRGBA8888(offsetLightness(DEEP_OKLAB));
    private static final int shallowText = toRGBA8888(offsetLightness(SHALLOW_OKLAB));
    private static final int grassText = toRGBA8888(offsetLightness(GRASS_OKLAB));
    private static final int stoneText = toRGBA8888(describeOklab("gray dullmost butter bronze"));

    public DungeonDemo(Config config) {
        this.config = config;
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_INFO);
        long seed = TimeUtils.millis() >>> 21;
        Gdx.app.log("SEED", "Initial seed is " + seed);
        EnhancedRandom random = new WhiskerRandom(seed);
        stage = new Stage();
        KnownFonts.setAssetPrefix("fonts/");
//        Font font = addGameIcons(KnownFonts.getIosevkaSlab(), "", "", -24, -24, 0);
        // adjustLineHeight(1.25f) may not be needed in the next release of TextraTypist...?
        Font font = KnownFonts.addGameIcons(KnownFonts.getIosevkaSlab().scaleTo(16f, 28f).adjustLineHeight(1.25f));
//        Font font = new Font("Iosevka-Slab-standard.fnt", "Iosevka-Slab-standard.png", STANDARD, 0f, 0f, 0f, 0f, true)
//            .scaleTo(15f, 24f).setTextureFilter().setName("Iosevka Slab");
//        Font font = KnownFonts.getCascadiaMonoMSDF().scaleTo(15f, 25f);

//        font = KnownFonts.getCascadiaMono().scale(0.5f, 0.5f);
//        font = KnownFonts.getIosevka().scale(0.75f, 0.75f);
//        Font font = KnownFonts.getCascadiaMono();
//        Font font = KnownFonts.getInconsolata();
//        font = KnownFonts.getDejaVuSansMono().scale(0.75f, 0.75f);
//        Font font = KnownFonts.getCozette();
//        Font font = KnownFonts.getAStarry();
//        Font font = KnownFonts.getIosevkaMSDF().scaleTo(24, 24);
//        Font font = KnownFonts.getAStarry().scaleTo(16, 16);
//        Font font = KnownFonts.getAStarry().fitCell(24, 24, true);
//        Font font = KnownFonts.getInconsolataMSDF().fitCell(24, 24, true);
//        Font font = KnownFonts.getKingthingsPetrock().scaleTo(19, 25);
        ShaderProgram shader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"),
                Gdx.files.internal("shaders/colorblindness-cor.frag.glsl"));
//                Gdx.files.internal("shaders/colorblindness-sim.frag.glsl"));
        if(!shader.isCompiled())
            System.out.println(shader.getLog());
        stage.getBatch().setShader(shader);
        font.shader = shader;

        int mapGridWidth = config.displayConfig.mapSize.gridWidth;
        int mapGridHeight = config.displayConfig.mapSize.gridHeight;
        gg = new GlyphGrid(font, mapGridWidth, mapGridHeight, true);
        //use Ă to test glyph height
        String name = Language.ANCIENT_EGYPTIAN.word(TimeUtils.millis(), true);
//        String replaced = Pattern.compile("([aeiou])").replacer("@").replace(name, 1);
//        if(name.equals(replaced))
//            replaced += "@";

        player = new Mob();
        player.glyph = new GlyphActor("[dark richer red raspberry]@", gg.getFont());
//        player.glyph = new GlyphActor("[dark richer red raspberry][+imperial-crown]", gg.getFont());
        player.glyph.setName(name);
        gg.addActor(player.glyph);
        enemies = new CoordObjectOrderedMap<>(26);
        post = () -> {
            seen.or(inView.refill(FOV.reuseFOV(res, light,
                Math.round(player.glyph.getX()), Math.round(player.glyph.getY()), 6.5f, Radius.CIRCLE), 0.001f, 999f));
            blockage.remake(seen).not().fringe8way();
            LineTools.pruneLines(dungeon, seen, prunedDungeon);
            gg.setVisibilities(inView::contains);
        };

        dungeonProcessor = new DungeonProcessor(mapGridWidth, mapGridHeight, random);
        dungeonProcessor.addWater(DungeonProcessor.ALL, 30);
        dungeonProcessor.addGrass(DungeonProcessor.ALL, 10);
        waves.setFractalType(Noise.RIDGED_MULTI);
        light = new float[mapGridWidth][mapGridHeight];
        seen = new Region(mapGridWidth, mapGridHeight);
        blockage = new Region(mapGridWidth, mapGridHeight);
        prunedDungeon = new char[mapGridWidth][mapGridHeight];
        inView = new Region(mapGridWidth, mapGridHeight);
        input.setInputProcessor(new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode){
                    case ESCAPE:
                    case Q:
                        Gdx.app.exit();
                        break;
                    case R:
                        regenerate();
                        break;
                    default: return false;
                }
                return true;
            }
            // if the user clicks and mouseMoved hasn't already assigned a path to toCursor, then we call mouseMoved
            // ourselves and copy toCursor over to awaitedMoves.
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                pos.set(screenX, screenY);
                gg.viewport.unproject(pos);
                if (onGrid(MathUtils.floor(pos.x), MathUtils.floor(pos.y))) {
                    mouseMoved(screenX, screenY);
                    awaitedMoves.addAll(toCursor);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return mouseMoved(screenX, screenY);
            }

            // causes the path to the mouse position to become highlighted (toCursor contains a list of Coords that
            // receive highlighting). Uses DijkstraMap.findPathPreScanned() to find the path, which is rather fast.
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                if(!awaitedMoves.isEmpty())
                    return false;
                pos.set(screenX, screenY);
                gg.viewport.unproject(pos);
                if (onGrid(screenX = MathUtils.floor(pos.x), screenY = MathUtils.floor(pos.y))) {
                    // we also need to check if screenX or screenY is the same cell.
                    if (cursor.x == screenX && cursor.y == screenY) {
                        return false;
                    }
                    cursor = Coord.get(screenX, screenY);
                    // This uses DijkstraMap.findPathPreScannned() to get a path as a List of Coord from the current
                    // player position to the position the user clicked on. The "PreScanned" part is an optimization
                    // that's special to DijkstraMap; because the part of the map that is viable to move into has
                    // already been fully analyzed by the DijkstraMap.partialScan() method at the start of the
                    // program, and re-calculated whenever the player moves, we only need to do a fraction of the
                    // work to find the best path with that info.
                    toCursor.clear();
                    playerToCursor.findPathPreScanned(toCursor, cursor);
                    // findPathPreScanned includes the current cell (goal) by default, which is helpful when
                    // you're finding a path to a monster or loot, and want to bump into it, but here can be
                    // confusing because you would "move into yourself" as your first move without this.
                    if (!toCursor.isEmpty()) {
                        toCursor.remove(0);
                    }
                }
                return false;
            }
        });

        regenerate();
        stage.addActor(gg);
    }

    public void move(Direction way){
        // this prevents movements from restarting while a slide is already in progress.
        if(player.glyph.hasActions()) return;

        final Coord next = Coord.get(Math.round(player.glyph.getX() + way.deltaX), Math.round(player.glyph.getY() + way.deltaY));
        if(next.isWithin(config.displayConfig.mapSize.gridWidth, config.displayConfig.mapSize.gridHeight) && bare[next.x][next.y] == '.') {
            player.glyph.addAction(MoreActions.slideTo(next.x, next.y, 0.2f, post));
            if(enemies.containsKey(next)){
                gg.burst(
                        next.x,
                        next.y,
                        1.5f, 7, ',',
                        0x992200FF, 0x99220000,
                        0f, 120f, 1f);
                gg.removeActor(enemies.remove(next).glyph);

            }
        } else {
            player.glyph.addAction(MoreActions.bump(way, 0.3f));
        }
    }

    public void regenerate(){
        dungeonProcessor.setPlaceGrid(dungeon = LineTools.hashesToLines(dungeonProcessor.generate(), true));
        bare = dungeonProcessor.getBarePlaceGrid();
        ArrayTools.insert(dungeon, prunedDungeon, 0, 0);
        res = FOV.generateSimpleResistances(bare);
        Region floors = new Region(bare, '.');
        Coord player = floors.singleRandom(dungeonProcessor.rng);
        this.player.glyph.setPosition(player.x, player.y);
        floors.remove(player);
        Coord[] selected = floors.separatedBlue(0.125f, 26);
        for (int i = 0; i < 26 && i < selected.length; i++) {
            GlyphActor enemy = new GlyphActor((char)('A'+i), DescriptiveColor.toRGBA8888(FullPalette.randomColorWheel(dungeonProcessor.rng)), gg.getFont());
            enemy.setPosition(selected[i].x, selected[i].y);
            Mob mob = new Mob();
            mob.glyph = enemy;
            enemies.put(selected[i], mob);
            gg.addActor(enemy);
        }
        seen.remake(inView.refill(FOV.reuseFOV(res, light, player.x, player.y, 6.5f, Radius.CIRCLE), 0.001f, 2f));
        blockage.remake(seen).not().fringe8way();
        LineTools.pruneLines(dungeon, seen, prunedDungeon);
        gg.setVisibilities(inView::contains);
        gg.backgrounds = new int[config.displayConfig.mapSize.gridWidth][config.displayConfig.mapSize.gridHeight];
        gg.map.clear();
        if(playerToCursor == null)
            playerToCursor = new DijkstraMap(bare, Measurement.EUCLIDEAN);
        else
            playerToCursor.initialize(bare);
        playerToCursor.setGoal(player);
        playerToCursor.partialScan(13, blockage);
    }

    public void recolor(){
        int playerX = Math.round(player.glyph.getX());
        int playerY = Math.round(player.glyph.getY());
        float modifiedTime = (TimeUtils.millis() & 0xFFFFFL) * 0x1p-9f;
        int rainbow = toRGBA8888(
                limitToGamut(100,
                        (int) (TrigTools.sinTurns(modifiedTime * 0.2f) * 40f) + 128, (int) (TrigTools.cosTurns(modifiedTime * 0.2f) * 40f) + 128, 255));
        FOV.reuseFOV(res, light, playerX, playerY, LineWobble.wobble(12345, modifiedTime) * 2.5f + 4f, Radius.CIRCLE);
        for (int y = 0; y < config.displayConfig.mapSize.gridHeight; y++) {
            for (int x = 0; x < config.displayConfig.mapSize.gridWidth; x++) {
                if (inView.contains(x, y)) {
                    if(toCursor.contains(Coord.get(x, y))){
                        gg.backgrounds[x][y] = rainbow;
                        gg.put(x, y, prunedDungeon[x][y], stoneText);
                    }
                    else {
                        switch (prunedDungeon[x][y]) {
                            case '~':
                                gg.backgrounds[x][y] = toRGBA8888(lighten(DEEP_OKLAB, 0.6f * Math.min(1.2f, Math.max(0, light[x][y] + waves.getConfiguredNoise(x, y, modifiedTime)))));
                                gg.put(x, y, prunedDungeon[x][y], deepText);
                                break;
                            case ',':
                                gg.backgrounds[x][y] = toRGBA8888(lighten(SHALLOW_OKLAB, 0.6f * Math.min(1.2f, Math.max(0, light[x][y] + waves.getConfiguredNoise(x, y, modifiedTime)))));
                                gg.put(x, y, prunedDungeon[x][y], shallowText);
                                break;
                            case '"':
                                gg.backgrounds[x][y] = toRGBA8888(darken(lerpColors(GRASS_OKLAB, DRY_OKLAB, waves.getConfiguredNoise(x, y) * 0.5f + 0.5f), 0.4f * Math.min(1.1f, Math.max(0, 1f - light[x][y] + waves.getConfiguredNoise(x, y, modifiedTime * 0.7f)))));
                                gg.put(x, y, prunedDungeon[x][y], grassText);
                                break;
                            case ' ':
                                gg.backgrounds[x][y] = 0;
                                break;
                            default:
                                gg.backgrounds[x][y] = toRGBA8888(lighten(STONE_OKLAB, 0.6f * light[x][y]));
                                gg.put(x, y, prunedDungeon[x][y], stoneText);
                        }
                    }
                } else if (seen.contains(x, y)) {
                    switch (prunedDungeon[x][y]) {
                        case '~':
                            gg.backgrounds[x][y] = toRGBA8888(edit(DEEP_OKLAB, 0f, 0f, 0f, 0f, 0.7f, 0f, 0f, 1f));
                            gg.put(x, y, prunedDungeon[x][y], deepText);
                            break;
                        case ',':
                            gg.backgrounds[x][y] = toRGBA8888(edit(SHALLOW_OKLAB, 0f, 0f, 0f, 0f, 0.7f, 0f, 0f, 1f));
                            gg.put(x, y, prunedDungeon[x][y], shallowText);
                            break;
                        case ' ':
                            gg.backgrounds[x][y] = 0;
                            break;
                        default:
                            gg.backgrounds[x][y] = toRGBA8888(edit(STONE_OKLAB, 0f, 0f, 0f, 0f, 0.7f, 0f, 0f, 1f));
                            gg.put(x, y, prunedDungeon[x][y], stoneText);
                    }
                } else {
                    gg.backgrounds[x][y] = 0;
                }
            }
        }
    }

    /**
     * Supports WASD, vi-keys (hjklyubn), arrow keys, and numpad for movement, plus '.' or numpad 5 to stay still.
     */
    public void handleHeldKeys() {
        if(input.isKeyPressed(A) || input.isKeyPressed(H) || input.isKeyPressed(LEFT) || input.isKeyPressed(NUMPAD_4))
            move(Direction.LEFT);
        else if(input.isKeyPressed(S) || input.isKeyPressed(J) || input.isKeyPressed(DOWN) || input.isKeyPressed(NUMPAD_2))
            move(Direction.DOWN);
        else if(input.isKeyPressed(W) || input.isKeyPressed(K) || input.isKeyPressed(UP) || input.isKeyPressed(NUMPAD_8))
            move(Direction.UP);
        else if(input.isKeyPressed(D) || input.isKeyPressed(L) || input.isKeyPressed(RIGHT) || input.isKeyPressed(NUMPAD_6))
            move(Direction.RIGHT);
        else if(input.isKeyPressed(Y) || input.isKeyPressed(NUMPAD_7))
            move(Direction.UP_LEFT);
        else if(input.isKeyPressed(U) || input.isKeyPressed(NUMPAD_9))
            move(Direction.UP_RIGHT);
        else if(input.isKeyPressed(B) || input.isKeyPressed(NUMPAD_1))
            move(Direction.DOWN_LEFT);
        else if(input.isKeyPressed(N) || input.isKeyPressed(NUMPAD_3))
            move(Direction.DOWN_RIGHT);
        else if(input.isKeyPressed(PERIOD) || input.isKeyPressed(NUMPAD_5) || input.isKeyPressed(NUMPAD_DOT))
            move(Direction.NONE);
    }

    @Override
    public void render() {
//        stage.getBatch().getShader().setUniformi("u_mode", 1);
        recolor();
        handleHeldKeys();
        for (int i = 0; i < enemies.size(); i++) {
            enemies.getAt(i).glyph.setRotation((System.currentTimeMillis() & 0xFFFFFL) * 0.25f);
        }

        if(!gg.areChildrenActing() && !awaitedMoves.isEmpty())
        {
            Coord m = awaitedMoves.remove(0);
            if (!toCursor.isEmpty())
                toCursor.remove(0);
            move(player.glyph.getLocation().toGoTo(m));
        }
        else {
            if (!gg.areChildrenActing()) {
//                postMove();
                // this only happens if we just removed the last Coord from awaitedMoves, and it's only then that we need to
                // re-calculate the distances from all cells to the player. We don't need to calculate this information on
                // each part of a many-cell move (just the end), nor do we need to calculate it whenever the mouse moves.
                if (awaitedMoves.isEmpty()) {
                    // the next two lines remove any lingering data needed for earlier paths
                    playerToCursor.clearGoals();
                    playerToCursor.resetMap();
                    // the next line marks the player as a "goal" cell, which seems counter-intuitive, but it works because all
                    // cells will try to find the distance between themselves and the nearest goal, and once this is found, the
                    // distances don't change as long as the goals don't change. Since the mouse will move and new paths will be
                    // found, but the player doesn't move until a cell is clicked, the "goal" is the non-changing cell, so the
                    // player's position, and the "target" of a pathfinding method like DijkstraMap.findPathPreScanned() is the
                    // currently-moused-over cell, which we only need to set where the mouse is being handled.
                    playerToCursor.setGoal(player.glyph.getLocation());
                    // DijkstraMap.partialScan only finds the distance to get to a cell if that distance is less than some limit,
                    // which is 13 here. It also won't try to find distances through an impassable cell, which here is the blockage
                    // GreasedRegion that contains the cells just past the edge of the player's FOV area.
                    playerToCursor.partialScan(13, blockage);
                }
            }
        }

        ScreenUtils.clear(Color.BLACK);
        Camera camera = gg.viewport.getCamera();
        camera.position.set(gg.getGridWidth() * 0.5f, gg.getGridHeight() * 0.5f, 0f);
        camera.update();
        stage.act();
        stage.draw();

        if (config.debugConfig.debugActive && config.debugConfig.showFPS) {
            Gdx.graphics.setTitle(Config.gameTitle + "  " + "FPS: " + Gdx.graphics.getFramesPerSecond());
        } else {
            Gdx.graphics.setTitle(Config.gameTitle);
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        gg.resize(width, height);
    }

    private boolean onGrid(int screenX, int screenY) {
        return screenX >= 0
            && screenX < config.displayConfig.mapSize.gridWidth
            && screenY >= 0
            && screenY < config.displayConfig.mapSize.gridHeight;
    }
}
