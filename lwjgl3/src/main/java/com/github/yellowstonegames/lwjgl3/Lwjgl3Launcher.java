package com.github.yellowstonegames.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.github.yellowstonegames.DungeonDemo;
import com.github.yellowstonegames.files.Config;

/**
 * Launches the desktop (LWJGL3) application.
 */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        // Needed for macOS support, but also Windows with non-ASCII usernames.
        if (StartOnFirstThreadHelper.startNewJvmIfRequired()) return;
        // Graal stuff
        org.lwjgl.system.Library.initialize();
        org.lwjgl.system.ThreadLocalUtil.setupEnvData();

        // load splashscreen

        // now that assets are ready, load main game screen
        mainGame();
    }

    private static void mainGame() {
        //start independent creators
        System.out.println("Loading...");

        //read in all external data files
        Config config = Config.instance(); // will cause the config file to be read if it hasn't already

        // need to cross-config set window size if it's not in the configs
        if (config.displayConfig.windowWidth <= 0) {
            config.displayConfig.windowWidth = config.displayConfig.defaultPixelWidth();
        }
        if (config.displayConfig.windowHeight <= 0) {
            config.displayConfig.windowHeight = config.displayConfig.defaultPixelHeight();
        }

        System.out.println("Files loaded!");
        DungeonDemo dungeonDemo = new DungeonDemo(config);

        //start independent listeners
        //load and initialize resources
        //initialize the display
        //initialize the world
        //start dependent creators
        //start dependent listeners
        //hand control over to the display
        Lwjgl3ApplicationConfiguration appConfig = new Lwjgl3ApplicationConfiguration();
        Lwjgl3WindowAdapter primaryWindowListener = new Lwjgl3WindowAdapter() {
            private Lwjgl3Window win;

            @Override
            public void created(Lwjgl3Window window) {
                super.created(window);
                win = window;
            }

            @Override
            public void maximized(boolean isMaximized) {
                config.displayConfig.maximized = isMaximized;
                if (!isMaximized) {
                    Gdx.app.postRunnable(() -> Gdx.graphics.setWindowedMode(
                        config.displayConfig.windowWidth,
                        config.displayConfig.windowHeight)
                    );
                }

                super.maximized(isMaximized);
            }

            @Override
            public boolean closeRequested() {
                if (!config.displayConfig.maximized && !config.displayConfig.fullscreen) {
                    config.displayConfig.windowXPosition = win.getPositionX();
                    config.displayConfig.windowYPosition = win.getPositionY();
                    config.displayConfig.windowWidth = Gdx.graphics.getWidth();
                    config.displayConfig.windowHeight = Gdx.graphics.getHeight();
                    config.displayConfig.monitorName = Gdx.graphics.getMonitor().name;
                    config.saveAll();
                }

                return super.closeRequested();
            }
        };

        appConfig.setWindowListener(primaryWindowListener);

        // get monitor info for display
        String lastMonitorName = config.displayConfig.monitorName;
        Graphics.Monitor monitor = null;
        if (lastMonitorName != null && !lastMonitorName.isEmpty()) {
            for (Graphics.Monitor m : Lwjgl3ApplicationConfiguration.getMonitors()) {
                if (m.name.equals(lastMonitorName)) {
                    monitor = m;
                    break;
                }
            }
        }
        if (monitor == null) {
            monitor = Lwjgl3ApplicationConfiguration.getPrimaryMonitor();
        }
        config.displayConfig.monitorName = monitor.name;

        Graphics.DisplayMode display = Lwjgl3ApplicationConfiguration.getDisplayMode(monitor);

        if (config.displayConfig.maximized) {
            appConfig.setMaximized(true);
        } else if (config.displayConfig.fullscreen) {
            appConfig.setFullscreenMode(display);
        } else {
            appConfig.setWindowedMode(config.displayConfig.windowWidth, config.displayConfig.windowHeight);

            int x = config.displayConfig.windowXPosition;
            int y = config.displayConfig.windowYPosition;
            System.out.println("Window position: (" + x + ", " + y + ")");

            if (x < 0) {
                x = (display.width - config.displayConfig.windowWidth) / 2;
                config.displayConfig.windowXPosition = x;
            }
            if (y < 0) {
                y = (display.height - config.displayConfig.windowHeight) / 2;
                config.displayConfig.windowYPosition = y;
            }

            appConfig.setWindowPosition(x, y); // This doesn't take into account the upper left including title bar, just content (libgdx / lwjgl limitation)
        }

        appConfig.setTitle(Config.gameTitle);
        appConfig.setWindowIcon(
            "images/icons/logo512.png",
            "images/icons/logo128.png",
            "images/icons/logo64.png",
            "images/icons/logo32.png",
            "images/icons/logo16.png");

        appConfig.useVsync(config.displayConfig.vsync);
        if (!(config.debugConfig.debugActive && config.debugConfig.showFPS)) {
            appConfig.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        }

        new Lwjgl3Application(dungeonDemo, appConfig) {
            @Override
            public void exit() {
                primaryWindowListener.closeRequested(); // have the primary window do its thing before leaving
                super.exit();
            }
        };
    }
}
