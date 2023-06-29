package com.github.yellowstonegames;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.yellowstonegames.core.FullPalette;
import com.github.yellowstonegames.data.Mob;
import com.github.yellowstonegames.files.Config;
import com.github.yellowstonegames.glyph.GlyphActor;
import com.github.yellowstonegames.glyph.GlyphGrid;
import com.github.yellowstonegames.glyph.MoreActions;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.path.DijkstraMap;
import com.github.yellowstonegames.place.DungeonProcessor;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.util.RNG;
import com.github.yellowstonegames.util.Text;

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
    private Region seen, inView, blockage;
    private final Noise waves = new Noise(123, 0.5f, Noise.FOAM_FRACTAL, 1);
    private final Noise ridges = new Noise(12345, 0.6f, Noise.FOAM_FRACTAL, 1);
    private Mob player;
    private CoordObjectOrderedMap<Mob> enemies;
    private DijkstraMap playerToCursor;
    private final ObjectList<Coord> toCursor = new ObjectList<>(100);
    private final ObjectList<Coord> awaitedMoves = new ObjectList<>(50);
    private Coord cursor = Coord.get(-1, -1);
    private final Vector2 pos = new Vector2();
    private Runnable post;
    private LightingManager lighting;

    private Config config;

    private static final int DUNGEON_WIDTH = 100;
    private static final int DUNGEON_HEIGHT = 100;

    private static final int DEEP_OKLAB = describeOklab("dark dull cobalt");
    private static final int SHALLOW_OKLAB = describeOklab("dull denim");
    private static final int LAVA_OKLAB = describeOklab("dark rich ember");
    private static final int CHAR_OKLAB = describeOklab("darker dullmost black ember");
    private static final int GRASS_OKLAB = describeOklab("dullest darkest green");
    private static final int DRY_OKLAB = describeOklab("duller apricot sage");
    private static final int STONE_OKLAB = describeOklab("darkmost gray dullest bronze");
    private static final int MEMORY_RGBA = describe("darker gray black");
    private static final int deepText = toRGBA8888(offsetLightness(DEEP_OKLAB));
    private static final int shallowText = toRGBA8888(offsetLightness(SHALLOW_OKLAB));
    private static final int lavaText = toRGBA8888(offsetLightness(LAVA_OKLAB));
    private static final int charText = toRGBA8888(offsetLightness(CHAR_OKLAB));
    private static final int grassText = toRGBA8888(offsetLightness(GRASS_OKLAB));
    private static final int stoneText = toRGBA8888(describeOklab("gray dullmost butter bronze"));

    public DungeonDemo(Config config) {
        this.config = config;
    }

    @Override
    public void create() {
//        System.out.println("USABLE_LETTERS:");
//        System.out.println(Text.USABLE_LETTERS);
//        System.out.println("USABLE_SYMBOLS:");
//        System.out.println(Text.USABLE_SYMBOLS);

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
//        ShaderProgram shader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"),
//                Gdx.files.internal("shaders/colorblindness-cor.frag.glsl"));
////                Gdx.files.internal("shaders/colorblindness-sim.frag.glsl"));
//        if(!shader.isCompiled())
//            System.out.println(shader.getLog());
//        stage.getBatch().setShader(shader);
//        font.shader = shader;

        gg = new GlyphGrid(font, DUNGEON_WIDTH, DUNGEON_HEIGHT, true);
        gg.viewport.setWorldSize(config.displayConfig.mapSize.gridWidth, config.displayConfig.mapSize.gridHeight);
        gg.backgrounds = new int[DUNGEON_WIDTH][DUNGEON_HEIGHT];
        //use Ă to test glyph height
        String name = Language.ANCIENT_EGYPTIAN.word(TimeUtils.millis(), true);
//        String replaced = Pattern.compile("([aeiou])").replacer("@").replace(name, 1);
//        if(name.equals(replaced))
//            replaced += "@";

        player = new Mob();
//        player.glyph = gg.getFont().markupGlyph("[dark richer red raspberry][+imperial-crown]");
        player.glyph = gg.getFont().markupGlyph("[dark richer red raspberry]@");
        player.actor = new GlyphActor(player.glyph, gg.getFont());
        player.setName(name);
        gg.addActor(player.actor);
        enemies = new CoordObjectOrderedMap<>(100);
        post = () -> {
            int playerX = Math.round(player.actor.getX()), playerY = Math.round(player.actor.getY());
            lighting.calculateFOV(playerX, playerY, playerX - 10, playerY - 10, playerX + 11, playerY + 11);
            seen.or(inView.refill(lighting.fovResult, 0.001f, 2f));
            blockage.remake(seen).not().fringe8way();
            LineTools.pruneLines(dungeon, seen, prunedDungeon);
            gg.setVisibilities(inView::contains);
            if(!awaitedMoves.isEmpty())
                awaitedMoves.remove(0);
        };

        dungeonProcessor = new DungeonProcessor(DUNGEON_WIDTH, DUNGEON_HEIGHT, random);
        dungeonProcessor.addWater(DungeonProcessor.ALL, 20);
        dungeonProcessor.addGrass(DungeonProcessor.ALL, 30);
        dungeonProcessor.addLake(20, '₤', '¢');
        waves.setFractalType(Noise.RIDGED_MULTI);
        ridges.setFractalType(Noise.RIDGED_MULTI);
        seen = new Region(DUNGEON_WIDTH, DUNGEON_HEIGHT);
        blockage = new Region(DUNGEON_WIDTH, DUNGEON_HEIGHT);
        prunedDungeon = new char[DUNGEON_WIDTH][DUNGEON_HEIGHT];
        inView = new Region(DUNGEON_WIDTH, DUNGEON_HEIGHT);
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
                if(!awaitedMoves.isEmpty())
                    return false;
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
        if(player.actor.hasActions()) return;
        final Coord old = player.actor.getLocation();
        final Coord next = Coord.get(Math.round(player.actor.getX() + way.deltaX), Math.round(player.actor.getY() + way.deltaY));
        if(next.isWithin(DUNGEON_WIDTH, DUNGEON_HEIGHT) && bare[next.x][next.y] == '.') {
            player.actor.addAction(MoreActions.slideTo(next.x, next.y, 0.2f, post));
            if(enemies.containsKey(next)){
                gg.burst(
                        next.x,
                        next.y,
                        1.5f, 7, ',',
                        0x992200FF, 0x99220000,
                        0f, 120f, 1f);
                gg.removeActor(enemies.remove(next).actor);
                lighting.removeLight(next);
            }
            lighting.moveLight(old, next);
        } else {
            player.actor.addAction(MoreActions.bump(way, 0.3f));
        }
    }

    public void regenerate(){
        enemies.clear();
        Actor[] kids = gg.children.begin();
        for(int c = 0; c < gg.children.size; c++)
            kids[c].clearActions();
        gg.children.end();
        gg.clearChildren(true);
        dungeonProcessor.setPlaceGrid(dungeon = LineTools.hashesToLines(dungeonProcessor.generate(), true));
        bare = dungeonProcessor.getBarePlaceGrid();
        EnhancedRandom rng = dungeonProcessor.rng;
        ArrayTools.insert(dungeon, prunedDungeon, 0, 0);
        lighting = new LightingManager(FOV.generateSimpleResistances(bare), "dark gray black", Radius.CIRCLE, 2.5f);
        Region floors = new Region(bare, '.');
        Coord player = floors.singleRandom(rng);
        this.player.actor.setLocation(player);
        gg.addActor(this.player.actor);
        floors.remove(player);
        Coord[] selected = floors.separatedPoisson(RNG.rng, 4f, 100);
        for (int i = 0, ci = 0; i < selected.length; i++, ci++) {
            int color = rng.randomElement(FullPalette.COLOR_WHEEL_PALETTE_FLUSH);
            Mob mob = new Mob(gg, selected[i], Text.USABLE_LETTERS.charAt(ci += RNG.rng.next(1)), toRGBA8888(darken(color, 0.1f)));
            enemies.put(selected[i], mob);
            gg.addActor(mob.actor);
            lighting.addLight(selected[i], new Radiance(rng.nextFloat(3f) + 2f,
                    lighten(color, 0.2f), 0.5f, 0f));
        }
        lighting.addLight(player, new Radiance(5f, FullPalette.COSMIC_LATTE, 0.3f, 0f));
        lighting.calculateFOV(player.x, player.y, player.x - 10, player.y - 10, player.x + 11, player.y + 11);
        seen.remake(inView.refill(lighting.fovResult, 0.001f, 2f));
        blockage.remake(seen).not().fringe8way();
        LineTools.pruneLines(dungeon, seen, prunedDungeon);
        gg.setVisibilities(inView::contains);
//        gg.backgrounds = new int[config.displayConfig.mapSize.gridWidth][config.displayConfig.mapSize.gridHeight];
//        ArrayTools.fill(gg.backgrounds, 0);
        gg.map.clear();
        if(playerToCursor == null)
            playerToCursor = new DijkstraMap(bare, Measurement.EUCLIDEAN);
        else
            playerToCursor.initialize(bare);
        playerToCursor.setGoal(player);
        playerToCursor.partialScan(13, blockage);
    }

    public void recolor(){
        float modifiedTime = (TimeUtils.millis() & 0xFFFFFL) * 0x1p-9f;
        int rainbow = toRGBA8888(
                limitToGamut(100,
                        (int) (TrigTools.sinTurns(modifiedTime * 0.2f) * 40f) + 128, (int) (TrigTools.cosTurns(modifiedTime * 0.2f) * 40f) + 128, 255));
//        FOV.reuseFOV(res, light, playerX, playerY, LineWobble.wobble(12345, modifiedTime) * 2.5f + 4f, Radius.CIRCLE);
        for (int y = 0; y < DUNGEON_HEIGHT; y++) {
            for (int x = 0; x < DUNGEON_WIDTH; x++) {
                if (inView.contains(x, y)) {
                    if(toCursor.contains(Coord.get(x, y))){
                        gg.backgrounds[x][y] = rainbow;
                        gg.put(x, y, prunedDungeon[x][y], stoneText);
                    }
                    else {
                        switch (prunedDungeon[x][y]) {
                            case '~':
                                gg.backgrounds[x][y] = (lighten(DEEP_OKLAB, 0.6f * Math.min(1.2f, Math.max(0, lighting.fovResult[x][y] + waves.getConfiguredNoise(x, y, modifiedTime)))));
                                gg.put(x, y, prunedDungeon[x][y], deepText);
                                break;
                            case ',':
                                gg.backgrounds[x][y] = (lighten(SHALLOW_OKLAB, 0.6f * Math.min(1.2f, Math.max(0, lighting.fovResult[x][y] + waves.getConfiguredNoise(x, y, modifiedTime)))));
                                gg.put(x, y, prunedDungeon[x][y], shallowText);
                                break;
                            case '₤':
                                gg.backgrounds[x][y] = (lighten(LAVA_OKLAB, 0.5f * Math.min(1.5f, Math.max(0, lighting.fovResult[x][y] + ridges.getConfiguredNoise(x, y, modifiedTime)))));
                                gg.put(x, y, prunedDungeon[x][y], lavaText);
                                break;
                            case '¢':
                                gg.backgrounds[x][y] = (lighten(CHAR_OKLAB, 0.2f * Math.min(0.8f, Math.max(0, lighting.fovResult[x][y] + ridges.getConfiguredNoise(x, y, modifiedTime)))));
                                gg.put(x, y, prunedDungeon[x][y], charText);
                                break;
                            case '"':
                                gg.backgrounds[x][y] = lerpColors(GRASS_OKLAB, DRY_OKLAB, MathTools.square(IntPointHash.hash256(x, y, 12345) * 0x1.8p-9f));
// (darken(lerpColors(GRASS_OKLAB, DRY_OKLAB, waves.getConfiguredNoise(x, y) * 0.5f + 0.5f), 0.4f * Math.min(1.1f, Math.max(0, 1f - lighting.fovResult[x][y] + waves.getConfiguredNoise(x, y, modifiedTime * 0.7f)))));
                                gg.put(x, y, prunedDungeon[x][y], grassText);
                                break;
                            case ' ':
                                gg.backgrounds[x][y] = 0;
                                break;
                            default:
//                                gg.backgrounds[x][y] = toRGBA8888(lighten(STONE_OKLAB, 0.6f * lighting.fovResult[x][y]));
                                gg.backgrounds[x][y] = 0;
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
                        case '₤':
                            gg.backgrounds[x][y] = toRGBA8888(edit(LAVA_OKLAB, 0f, 0f, 0f, 0f, 0.7f, 0f, 0f, 1f));
                            gg.put(x, y, prunedDungeon[x][y], lavaText);
                            break;
                        case '¢':
                            gg.backgrounds[x][y] = toRGBA8888(edit(CHAR_OKLAB, 0f, 0f, 0f, 0f, 0.7f, 0f, 0f, 1f));
                            gg.put(x, y, prunedDungeon[x][y], charText);
                            break;
                        case ' ':
                            gg.backgrounds[x][y] = 0;
                            break;
                        default:
                            gg.backgrounds[x][y] = MEMORY_RGBA;
                            gg.put(x, y, prunedDungeon[x][y], stoneText);
                    }
                } else {
                    gg.backgrounds[x][y] = 0;
                }
            }
        }
        lighting.draw(gg.backgrounds);
        for (int i = 0; i < toCursor.size(); i++) {
            Coord curr = toCursor.get(i);
            if(inView.contains(curr))
                gg.backgrounds[curr.x][curr.y] = rainbow;
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
        lighting.update();
        recolor();
        handleHeldKeys();
        for (int i = 0; i < enemies.size(); i++) {
            enemies.getAt(i).actor.setRotation((System.currentTimeMillis() & 0xFFFFFL) * 0.25f);
            Coord pos = enemies.keyAt(i);
            if(inView.contains(pos))
                gg.map.remove(pos);
        }
        gg.map.remove(player.actor.getLocation());

        if(!gg.areChildrenActing() && !awaitedMoves.isEmpty())
        {
            Coord m = awaitedMoves.get(0);
            if (!toCursor.isEmpty())
                toCursor.remove(0);
            move(player.actor.getLocation().toGoTo(m));
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
                    playerToCursor.setGoal(player.actor.getLocation());
                    // DijkstraMap.partialScan only finds the distance to get to a cell if that distance is less than some limit,
                    // which is 13 here. It also won't try to find distances through an impassable cell, which here is the blockage
                    // GreasedRegion that contains the cells just past the edge of the player's FOV area.
                    playerToCursor.partialScan(13, blockage);
                }
            }
        }

        ScreenUtils.clear(Color.BLACK);
        Camera camera = gg.viewport.getCamera();
        camera.position.set(player.actor.getX(), player.actor.getY(), 0f);
        // this keeps the map stationary, but does not allow the map to be larger than the screen.
//        camera.position.set(gg.getGridWidth() * 0.5f, gg.getGridHeight() * 0.5f, 0f);
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
            && screenX < DUNGEON_WIDTH
            && screenY >= 0
            && screenY < DUNGEON_HEIGHT;
    }
}
