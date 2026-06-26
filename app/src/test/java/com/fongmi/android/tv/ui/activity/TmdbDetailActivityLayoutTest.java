package com.fongmi.android.tv.ui.activity;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class TmdbDetailActivityLayoutTest {

    @Test
    public void fusionDetailBackdropDrawsBehindSystemBars() throws Exception {
        Path sourcePath = findMainJavaPath().resolve(Path.of("com", "fongmi", "android", "tv", "ui", "activity", "TmdbDetailActivity.java"));
        String source = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
        int method = source.indexOf("private void applyDetailEdgeToEdge()");
        int init = source.indexOf("protected void initView(Bundle savedInstanceState)");
        int theme = source.indexOf("private void applyDetailTheme()");

        assertTrue(sourcePath + " is missing applyDetailEdgeToEdge", method >= 0);
        assertTrue("TMDB detail must draw the backdrop behind the system bars",
                source.indexOf("WindowCompat.setDecorFitsSystemWindows(window, false)", method) > method);
        assertTrue("TMDB detail status bar must stay transparent over the backdrop",
                source.indexOf("window.setStatusBarColor(Color.TRANSPARENT)", method) > method);
        assertTrue("TMDB detail navigation bar must stay transparent over the backdrop",
                source.indexOf("window.setNavigationBarColor(Color.TRANSPARENT)", method) > method);
        assertTrue("TMDB detail must keep system bar icon contrast in sync with the detail theme",
                source.indexOf("setAppearanceLightStatusBars", method) > method);
        assertTrue("TMDB detail must configure edge-to-edge during initialization",
                source.indexOf("applyDetailEdgeToEdge();", init) > init);
        assertTrue("TMDB detail must re-apply edge-to-edge after theme changes",
                source.indexOf("applyDetailEdgeToEdge();", theme) > theme);
    }

    @Test
    public void fusionDetailBackdropCropsToFillScreen() throws Exception {
        Path sourcePath = findMainJavaPath().resolve(Path.of("com", "fongmi", "android", "tv", "ui", "activity", "TmdbDetailActivity.java"));
        String source = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
        int method = source.indexOf("private boolean shouldCropBackdrop()");
        assertTrue(sourcePath + " is missing shouldCropBackdrop", method >= 0);

        int methodEnd = source.indexOf("\n    }", method);
        String body = source.substring(method, methodEnd);
        assertTrue("Fusion detail must center-crop artwork so portrait screens do not show top/bottom background bars",
                body.contains("return true;"));
    }

    private static Path findMainJavaPath() {
        Path moduleRelative = Path.of("src", "main", "java");
        if (Files.exists(moduleRelative)) return moduleRelative;
        return Path.of("app", "src", "main", "java");
    }
}
