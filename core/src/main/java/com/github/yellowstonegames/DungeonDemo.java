package com.github.yellowstonegames;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.FullPalette;
import com.github.yellowstonegames.core.GapShuffler;
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
import static com.github.tommyettinger.textra.Font.DistanceFieldType.SDF;
import static com.github.yellowstonegames.core.DescriptiveColor.*;

/**
 * This is from another demo, meant to serve as a basis for later changes.
 */
@SuppressWarnings("ReassignedVariable") // A gift for you, Eben.
public class DungeonDemo extends ApplicationAdapter {
    private static final boolean PROFILING = false;
    private Stage worldStage;
    private Stage screenStage;
    private GlyphGrid gg;
    private DungeonProcessor dungeonProcessor;
    private char[][] bare, dungeon, prunedDungeon;
    private Region seen, inView, blockage, justSeen, justHidden, newlyVisible;
    private final Noise waves = new Noise(123, 0.5f, Noise.FOAM_FRACTAL, 1);
    private final Noise ridges = new Noise(12345, 0.6f, Noise.FOAM_FRACTAL, 1);
    private Mob player;
    private CoordObjectOrderedMap<Mob> enemies;
    private DijkstraMap playerToCursor;
    private final ObjectDeque<Coord> toCursor = new ObjectDeque<>(100);
    private final ObjectDeque<Coord> awaitedMoves = new ObjectDeque<>(50);
    private Coord cursor = Coord.get(-1, -1);
    private final Vector2 pos = new Vector2();
    private Runnable post;
    private VisionFramework vision = new VisionFramework();
    private LightingManager lighting;
    private float[][] previousLightLevels;
    private long lastMove;
    private Font varWidthFont;
    private ObjectDeque<Container<TypingLabel>> messages = new ObjectDeque<>(30);
    private Table messageGroup;
    private Table root;

    public RNG random;
    public GapShuffler<String> killMessages;

    private Config config;

    private GLProfiler profiler;

    private static final int DUNGEON_WIDTH = 100;
    private static final int DUNGEON_HEIGHT = 100;

    private static final float MESSAGE_SHRINK = 0.45f;

    private static final int BG_OKLAB = describeOklab("dark gray black");
    private static final int DEEP_OKLAB = describeOklab("darker duller teal cobalt");
    private static final int SHALLOW_OKLAB = describeOklab("dark dull denim");
    private static final int LAVA_OKLAB = describeOklab("dark rich ember");
    private static final int CHAR_OKLAB = describeOklab("darker dullmost black ember");
    private static final int GRASS_OKLAB = describeOklab("darker black moss green");
    private static final int DRY_OKLAB = describeOklab("duller apricot sage");
    private static final int STONE_OKLAB = describeOklab("darkmost gray dullest bronze");
    private static final int MEMORY_BG = describe("darker gray black");
    private static final int MEMORY_FG = describe("black silver");
    private static final int deepText = toRGBA8888(lighten(offsetLightness(DEEP_OKLAB), 0.2f));
    private static final int shallowText = toRGBA8888(lighten(offsetLightness(SHALLOW_OKLAB), 0.2f));
    private static final int lavaText = toRGBA8888(darken((LAVA_OKLAB), 0.3f));
    private static final int charText = toRGBA8888(darken((CHAR_OKLAB), 0.2f));
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
        if(PROFILING)
            profiler = new GLProfiler(Gdx.graphics);
        long seed = config.settings.seedValue;
        Gdx.app.log("SEED", "Initial seed is " + seed);
        random = new RNG(seed);
        worldStage = new Stage();
        screenStage = new Stage();
        String prefix = "fonts/";
        KnownFonts.setAssetPrefix(prefix);
        // OK, this is a total mess.
        // Here, we sort-of duplicate KnownFonts.getIosevkaSlab(), but change the size, offsetY, and descent.
        // Having descent = 0 is normally incorrect, but seems to work well with GlyphGrid for some reason.
        Font font = new Font(prefix + "Iosevka-Slab-sdf.dat",
            new TextureRegion(new Texture(prefix + "Iosevka-Slab-sdf.png")), 0f, 0f, 0f, 0f, true, true) // offsetY changed
                .scaleTo(16, 28).fitCell(28, 28, true)
//                .setDescent(0f) // changed a lot
                .setLineMetrics(0f, -0.125f, 0f, -0.25f).setTextureFilter().setName("Iosevka Slab");
//        Font font = addGameIcons(KnownFonts.getIosevkaSlab(), "", "", -24, -24, 0);
//        Font font = KnownFonts.addGameIcons(KnownFonts.getIosevkaSlab().scaleTo(16f, 28f).adjustLineHeight(1.25f));
        varWidthFont = KnownFonts.getGentiumUnItalic(SDF).scaleTo(54f, 28f)
                .setTextureFilter().setFancyLinePosition(0, 0f).setDescent(-16f)
                .setLineMetrics(0f, 0f, 0f, -0.3125f).setInlineImageMetrics(0f, 8f, 8f);
//        Font font = new Font("Iosevka-Slab-standard.fnt", "Iosevka-Slab-standard.png", STANDARD, 0f, 0f, 0f, 0f, true)
//            .scaleTo(15f, 24f).setTextureFilter().setName("Iosevka Slab");

        // this section can be enabled during development, but only when the font files change.
        // it prints the String that will be put in Text.USABLE_CHARS .
/*
        if(false) {
            CharList cl = new CharList(256);
            IntMap.Keys ks = font.mapping.keys();
            while (ks.hasNext) {
                cl.add((char) ks.next());
            }
            cl.sort();
            System.out.println("USABLE_CHARS\n" + cl.toDenseString());
        }
 */
//        ShaderProgram shader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"),
//                Gdx.files.internal("shaders/colorblindness-cor.frag.glsl"));
////                Gdx.files.internal("shaders/colorblindness-sim.frag.glsl"));
//        if(!shader.isCompiled())
//            System.out.println(shader.getLog());
//        stage.getBatch().setShader(shader);
//        font.shader = shader;
        killMessages = new GapShuffler<>(new String[]{
                "%s was {OCEAN=0.7;1.25;0.11;1.0;0.65}{CANNON}obliterated!{RESET}",
                "%s just got [dark dull pear]{SLOWER}{SICK}{STYLE=~}wasted...{RESET}",
                "%s took a real {SLIDE=1.1;0.25;true}punch{ENDSLIDE} in the gut, and they're out.{RESET}",
                "%s became {RAINBOW=1;1;0.6;0.75}{WAVE}one with the cosmos{ENDWAVE}{ENDRAINBOW}, as fine dust...{RESET}",
                "%s got [dark FIREBRICK]{SPEED=8}killed [white]{WAIT=0.4}ten {WAIT=0.4}times {WAIT=0.4}{NORMAL}before they hit the ground!{RESET}"}, random.copy());
        gg = new GlyphGrid(font, DUNGEON_WIDTH, DUNGEON_HEIGHT, true);
        gg.viewport.setWorldSize(config.displayConfig.mapSize.gridWidth, config.displayConfig.mapSize.gridHeight);
        gg.backgrounds = new int[DUNGEON_WIDTH][DUNGEON_HEIGHT];

        messageGroup = new Table().background(new TextureRegionDrawable(varWidthFont.mapping.get(varWidthFont.solidBlock)).tint(new Color(0.1f, 0.1f, 0.1f, 0.6f)));
        messageGroup.left();

        root = new Table();
        root.setFillParent(true);
        Table nest = new Table();
        nest.add(messageGroup).size(config.displayConfig.messageSize.pixelWidth() * MESSAGE_SHRINK, config.displayConfig.messageSize.pixelHeight());
        root.add(nest).bottom().expand().padBottom(25f);

        screenStage.addActor(root);
        //use Ă to test glyph height
        String name = "[gold]" + Language.ANCIENT_EGYPTIAN.word(TimeUtils.millis(), true) + " " + Language.ANCIENT_EGYPTIAN.word(Hasher.randomize3(TimeUtils.millis()), true) + "[white]";
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
//            int playerX = Math.round(player.actor.getX()), playerY = Math.round(player.actor.getY());
//            justSeen.remake(seen);
//            seen.or(inView.refill(lighting.fovResult, 0.001f, 2f));
//            blockage.remake(seen).not();
//            justSeen.notAnd(seen);
//            justHidden.andNot(blockage);
//            blockage.fringe8way();

            if(!awaitedMoves.isEmpty())
                awaitedMoves.removeFirst();
            playerToCursor.clearGoals();
            playerToCursor.resetMap();
            playerToCursor.setGoal(player.actor.getLocation());
            playerToCursor.partialScan(13, blockage);
        };

        dungeonProcessor = new DungeonProcessor(DUNGEON_WIDTH, DUNGEON_HEIGHT, random);
        dungeonProcessor.addWater(DungeonProcessor.ALL, 20);
        dungeonProcessor.addGrass(DungeonProcessor.ALL, 30);
        dungeonProcessor.addLake(20, '₤', '¢');
        waves.setFractalType(Noise.RIDGED_MULTI);
        ridges.setFractalType(Noise.RIDGED_MULTI);
//        seen = new Region(DUNGEON_WIDTH, DUNGEON_HEIGHT);
//        blockage = new Region(DUNGEON_WIDTH, DUNGEON_HEIGHT);
//        justSeen = justSeen == null ? seen.copy() : justSeen.remake(seen);
//        justHidden = justHidden == null ? new Region(DUNGEON_WIDTH, DUNGEON_HEIGHT) : justHidden.resizeAndEmpty(DUNGEON_WIDTH, DUNGEON_HEIGHT);
//        newlyVisible = newlyVisible == null ? seen.copy() : newlyVisible.remake(seen);
//        prunedDungeon = new char[DUNGEON_WIDTH][DUNGEON_HEIGHT];
//        inView = new Region(DUNGEON_WIDTH, DUNGEON_HEIGHT);
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
                    case V:
                        if(config.debugConfig.debugActive) {
                            config.debugConfig.debugActive = false;
                            config.debugConfig.showFPS = false;
                            Gdx.graphics.setVSync(true);
                            Gdx.graphics.setForegroundFPS(300);
                        } else {
                            config.debugConfig.debugActive = true;
                            config.debugConfig.showFPS = true;
                            Gdx.graphics.setVSync(false);
                            Gdx.graphics.setForegroundFPS(0);
                        }
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
                    if (cursor.x == screenX && cursor.y == screenY || gg.areChildrenActing()) {
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
                        toCursor.removeFirst();
                    }
                }
                return false;
            }
        });

        regenerate();
        worldStage.addActor(gg);
//        message("Laĕşudiphiĕşĕşĕşĕşĕşĕş Ghathŕuphighat was {OCEAN=0.7;1.25;0.11;1.0;0.65}{CANNON}obliterated!{RESET}");
//        message("Haisubhi Markhuśongaipaim was {OCEAN=0.7;1.25;0.11;1.0;0.65}{CANNON}obliterated!{RESET}");
//        message("Haisubhi Markhuśongaipaim was {OCEAN=0.7;1.25;0.11;1.0;0.65}{CANNON}obliterated!{RESET}");
//        message("Haisubhi Markhuśongaipaim was {OCEAN=0.7;1.25;0.11;1.0;0.65}{CANNON}obliterated!{RESET}");
        message("[*]WELCOME[*] to your [/]DOOM[/], %s!", player.getName());
    }

    public void move(Direction way){
        // this prevents movements from restarting while a slide is already in progress.
        if(player.actor.hasActions()) return;
        lastMove = TimeUtils.millis();
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
                Mob dead = enemies.remove(next);
                gg.removeActor(dead.actor);
                lighting.removeLight(next);
                message(killMessages.next(), dead.getName());
            }
            lighting.moveLight(old, next);
            vision.moveViewer(old, next);
            vision.finishChanges();
//
//            ArrayTools.set(lighting.fovResult, previousLightLevels);
//            justHidden.refill(previousLightLevels, 0f).not();
//            lighting.calculateFOV(next.x, next.y, next.x - 10, next.y - 10, next.x + 11, next.y + 11);
//            // assigns to blockage all cells that were NOT visible in the latest lightLevels calculation.
//            blockage.refill(lighting.fovResult, 0f);
//            // store current previously-in-view cells as justSeen, so they can be used to ease those cells into being seen.
//            justSeen.remake(justHidden);
//            // blockage.not() flips its values so now it stores all cells that ARE visible in the latest lightLevels calc.
//            inView.remake(blockage.not());
//            // stores cells that are currently visible but had never been seen at all (transparent) in earlier frames.
//            newlyVisible.remake(inView).andNot(seen);
//            // then, seen has all of those cells that have been visible (ever) included in with its cells.
//            seen.or(inView);
//            // this is roughly `justSeen = inView - justSeen;`, if subtraction worked on Regions.
//            justSeen.notAnd(inView);
//            // this is roughly `justHidden = justHidden - inView;`, where justHidden had included all previously visible
//            // cells, and now will have all currently visible cells removed from it. This leaves the just-hidden cells.
//            justHidden.andNot(inView);
//            // changes blockage so instead of all currently visible cells, it now stores the cells that would have been
//            // adjacent to those cells.
//            blockage.fringe8way();
//            LineTools.pruneLines(dungeon, seen, prunedDungeon);
            gg.setVisibilities(vision.inView::contains);

        } else {
            player.actor.addAction(MoreActions.bump(way, 0.3f));
        }
    }

    public void regenerate(){
        lastMove = TimeUtils.millis();
        enemies.clear();
        Actor[] kids = gg.getChildren().begin();
        for(int c = 0; c < gg.getChildren().size; c++)
            kids[c].clearActions();
        gg.getChildren().end();
        gg.clearChildren(true);
        dungeonProcessor.setPlaceGrid(dungeon = dungeonProcessor.generate(), dungeonProcessor.getEnvironment());
        bare = dungeonProcessor.getBarePlaceGrid();
        EnhancedRandom rng = dungeonProcessor.rng;
//        ArrayTools.insert(dungeon, prunedDungeon, 0, 0);
        Region floors = new Region(bare, '.');
        Coord player = floors.singleRandom(rng);
        vision.restart(dungeon, player, 2.5f, BG_OKLAB);
        vision.lighting.backgroundColor = BG_OKLAB;
        lighting = vision.lighting;
        seen = vision.seen;
        blockage = vision.blockage;
        justSeen = vision.justSeen;
        justHidden = vision.justHidden;
        newlyVisible = vision.newlyVisible;
        inView = vision.inView;
        previousLightLevels = vision.previousLightLevels;
        dungeon = vision.linePlaceMap;
        prunedDungeon = vision.prunedPlaceMap;

        this.player.actor.setLocation(player);
        gg.addActor(this.player.actor);
        floors.remove(player);
        Coord[] selected = floors.separatedPoisson(RNG.rng, 4f, 1000);
        for (int i = 0, ci = RNG.rng.nextInt(Text.USABLE_LETTERS.length()); i < selected.length; i++, ci++) {
            int color = RNG.rng.randomElement(FullPalette.COLOR_WHEEL_PALETTE_FLUSH);
            Mob mob = new Mob(gg, selected[i], Text.USABLE_LETTERS.charAt(ci = (ci + RNG.rng.next(1)) % Text.USABLE_LETTERS.length()), toRGBA8888(darken(color, 0.1f)));
            enemies.put(selected[i], mob);
            gg.addActor(mob.actor);
            lighting.addLight(selected[i], new Radiance(rng.nextFloat(3f) + 2f,
                    DescriptiveColor.edit(color, 0.75f, 0f, 0f, 0f, 0f, 1.5f, 1.5f, 1f), 0.5f, 0f));
        }
        lighting.addLight(player, new Radiance(5f, FullPalette.COSMIC_LATTE, 0.3f, 0f));
//        lighting.calculateFOV(player.x, player.y, player.x - 10, player.y - 10, player.x + 11, player.y + 11);
//        seen.remake(inView.refill(lighting.fovResult, 0.001f, 2f));
//        blockage.remake(seen).not().fringe8way();
//        LineTools.pruneLines(dungeon, seen, prunedDungeon);
        vision.finishChanges();
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

    public void message(String formatMarkup, Object... arguments) {
//        System.out.println(formatMarkup);
        TypingLabel label = null;
        Container<TypingLabel> con = null;
        float tall = 0;
        for(Container<TypingLabel> c : messages){
            tall += c.getHeight();
        }
        while(tall >= config.displayConfig.messageSize.pixelHeight()){
            con = messages.removeFirst();
            label = con.getActor();
            messageGroup.removeActor(con);
            messageGroup.pack();
            tall = 0;
            for(Container<TypingLabel> c : messages){
                tall += c.getHeight();
            }
        }

        if(label == null)
        {
            label = new TypingLabel("", varWidthFont);
            label.setWrap(true);
            label.setMaxLines(1);
            label.setEllipsis("...");
            label.restart(String.format(formatMarkup, arguments));
        }
        else {
            label.setSize(0, 0);
            label.restart(String.format(formatMarkup, arguments));
        }
        if(con == null)
        {
            con = new Container<>(label);
        }
        con.prefWidth(config.displayConfig.messageSize.pixelWidth() * MESSAGE_SHRINK);
        label.setAlignment(Align.bottomLeft);
        messages.addLast(con);
        messageGroup.add(con).row();
        root.pack();
    }

    public int bgMix(char c, int x, int y, float modifiedTime, float change, int lightColor) {
        if (!(vision.inView.contains(x, y) || vision.justHidden.contains(x, y))) return MEMORY_BG;
        int base;
        switch (c) {
            case '~':
                base = darken(DEEP_OKLAB, -0.1f + 0.25f * Math.min(0.7f, Math.max(0.2f, waves.getConfiguredNoise(x, y, modifiedTime))));
                break;
            case ',':
                base = darken(SHALLOW_OKLAB, 0.25f * Math.min(0.7f, Math.max(0.2f, waves.getConfiguredNoise(x, y, modifiedTime))));
                break;
            case '₤':
                base = lighten(LAVA_OKLAB, 0.5f * Math.min(1.5f, Math.max(0, ridges.getConfiguredNoise(x, y, modifiedTime))));
                break;
            case '¢':
                base = lighten(CHAR_OKLAB, 0.3f * Math.min(0.5f, Math.max(0.1f, ridges.getConfiguredNoise(x, y, modifiedTime))));
                break;
            case ':':
                base = lerpColors(CHAR_OKLAB, SILVER, 0.6f);
                break;
            case '"':
                base = lerpColors(GRASS_OKLAB, DRY_OKLAB, MathTools.square(IntPointHash.hash256(x, y, 12345) * 0x1.8p-9f));
                break;
            case ' ':
                base = TRANSPARENT;
                break;
            default:
                base = lightColor;
                break;
        }
        if(vision.justHidden.contains(x, y))
            return lerpColors(toRGBA8888(base), MEMORY_BG, change * 1E-3f);
        if(vision.newlyVisible.contains(x, y)) {
            int rgbaBase = toRGBA8888(base);
            return lerpColors(rgbaBase & -256, rgbaBase, change * 1E-3f);
        }
        if(vision.justSeen.contains(x, y))
            return lerpColors(MEMORY_BG, toRGBA8888(base), change * 1E-3f);
        return toRGBA8888(oklab(lightness(base), channelA(base) + channelA(lightColor) - 0.5f, channelB(base) + channelB(lightColor) - 0.5f, alpha(lightColor)));
    }


    public int fgMix(char c, int x, int y, int lightColor) {
        if (!(vision.inView.contains(x, y) || vision.justHidden.contains(x, y))) return MEMORY_FG;
        int base;
        boolean early = false;
        switch (c) {
            case '~':
                base = offsetLightness(DEEP_OKLAB);
                break;
            case ',':
                base = offsetLightness(SHALLOW_OKLAB);
                break;
            case '₤':
                base = offsetLightness(LAVA_OKLAB);
                break;
            case '¢':
                base = offsetLightness(CHAR_OKLAB);
                break;
            case ':':
                base = offsetLightness(SILVER);
                break;
            case '"':
                base = offsetLightness(GRASS_OKLAB);
                break;
            case ' ':
                base = TRANSPARENT;
                early = true;
                break;
            default:
                base = WHITE;
                early = true;
                break;
        }
        if(vision.justHidden.contains(x, y))
            return lerpColors(MEMORY_FG, toRGBA8888(base), alpha(lightColor));
        if(early) return toRGBA8888(base);
        return toRGBA8888(oklab(lightness(base), channelA(base) + channelA(lightColor) - 0.5f, channelB(base) + channelB(lightColor) - 0.5f, alpha(lightColor)));
    }

    public void recolor(){
        float time = (TimeUtils.millis() & 0xFFFFFL) * 0x1p-9f;

        float sinceLast = TimeUtils.timeSinceMillis(lastMove);
        final float change = Math.min(Math.max(sinceLast * 3f, 0f), 1000f);

        vision.update(change);

        int rainbow = toRGBA8888(DescriptiveColor.maximizeSaturation(160,
            (int) (TrigTools.sinTurns(time * 0.5f) * 30f) + 128, (int) (TrigTools.cosTurns(time * 0.5f) * 30f) + 128, 255));

        float[][] lightLevels = vision.lighting.fovResult;

        for (int x = 0; x < DUNGEON_WIDTH; x++) {
            for (int y = 0; y < DUNGEON_HEIGHT; y++) {
                char glyph = vision.prunedPlaceMap[x][y];
                if (vision.seen.contains(x, y) || vision.justHidden.contains(x, y)) {
                    // cells that were seen more than one frame ago, and aren't visible now, appear as a gray memory.
                    gg.backgrounds[x][y] = bgMix(glyph, x, y, time, change, vision.backgroundColors[x][y]);
                    gg.put(x, y, glyph, fgMix(glyph, x, y, vision.getForegroundColor(x, y, change)));
                } else {
                    gg.backgrounds[x][y] = 0;
                }
            }
        }
        Mob monster;
        for (int i = 0; i < DUNGEON_WIDTH; i++) {
            for (int j = 0; j < DUNGEON_HEIGHT; j++) {
                if (lightLevels[i][j] > 0.01) {
                    if ((monster = enemies.get(Coord.get(i, j))) != null) {
                        monster.actor.getColor().set(toRGBA8888(vision.getForegroundColor(i, j, change)));
                    }
                }
            }
        }

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
        if (PROFILING) {
            if (input.isKeyJustPressed(P)) {
                profiler.enable();
                profiler.reset();
            } else {
                profiler.disable();
            }
        }
        recolor();
        handleHeldKeys();
        for (int i = 0; i < enemies.size(); i++) {
            Actor a = enemies.getAt(i).actor;
            a.setRotation((System.currentTimeMillis() & 0xFFFFFL) * 0.25f);
            Coord pos = enemies.keyAt(i);
            if(inView.contains(pos))
                gg.map.remove(pos);
        }
        gg.map.remove(player.actor.getLocation());

        if(!gg.areChildrenActing() && !awaitedMoves.isEmpty())
        {
            Coord m = awaitedMoves.peekFirst();
            if (!toCursor.isEmpty())
                toCursor.removeFirst();
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
        worldStage.act();
        worldStage.draw();
        screenStage.act();
        screenStage.draw();
        if (config.debugConfig.debugActive && config.debugConfig.showFPS) {
            Gdx.graphics.setTitle(Config.gameTitle + "  " + "FPS: " + Gdx.graphics.getFramesPerSecond());
        } else {
            Gdx.graphics.setTitle(Config.gameTitle);
        }
        if(PROFILING) {
            if (input.isKeyJustPressed(P)) {
                GLProfiler p = profiler;
                Gdx.app.log("profiler", "Calls: " + p.getCalls() + ", Draw Calls: " + p.getDrawCalls() +
                    ", Shader Switches: " + p.getShaderSwitches() + ", Texture Binds: " + p.getTextureBindings() +
                    ", Vertices: " + p.getVertexCount());
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        gg.resize(width, height);
        screenStage.getViewport().update(width, height, true);

    }

    private boolean onGrid(int screenX, int screenY) {
        return screenX >= 0
            && screenX < DUNGEON_WIDTH
            && screenY >= 0
            && screenY < DUNGEON_HEIGHT;
    }
}
