/*
 * Eternity Pixel Dungeon
 * Post-Processing System (Smooth Pixel, Bloom, Vignette & LUT Shaders)
 */

package com.watabou.noosa;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.watabou.glscripts.BloomShader;
import com.watabou.glscripts.SmoothPixelShader;
import com.watabou.glscripts.VignetteLutShader;
import com.watabou.glwrap.Quad;

import java.nio.FloatBuffer;

public class PostProcessing {

	private static FrameBuffer fbo;
	private static FloatBuffer vertices;
	private static float[] quadData = new float[16];

	private static int lastW = 0;
	private static int lastH = 0;

	public static boolean smoothFilterEnabled = true;
	public static boolean bloomEnabled = false;
	public static boolean vignetteEnabled = false;
	public static float brightness = 1.0f;

	// Color tint for biomes / LUT (R, G, B, Intensity/Blend)
	public static float tintR = 1f;
	public static float tintG = 1f;
	public static float tintB = 1f;
	public static float tintA = 0f; // 0 = default, no tint

	public static boolean isEnabled() {
		return smoothFilterEnabled || bloomEnabled || vignetteEnabled || (brightness != 1.0f) || (tintA > 0f);
	}

	public static void begin() {
		if (!isEnabled()) return;

		int w = Game.width;
		int h = Game.height;

		if (w <= 0 || h <= 0) return;

		if (fbo == null || lastW != w || lastH != h) {
			if (fbo != null) {
				try {
					fbo.dispose();
				} catch (Exception ignored) {}
			}
			try {
				fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
				lastW = w;
				lastH = h;
			} catch (Exception e) {
				fbo = null;
				return;
			}
		}

		fbo.begin();
		Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
	}

	public static void end() {
		if (!isEnabled() || fbo == null) return;

		fbo.end();

		if (vertices == null) {
			vertices = Quad.create();
		}

		// Fill quad in Normalized Device Coordinates (-1 to 1) with UVs (0 to 1)
		Quad.fill(quadData, -1f, 1f, 1f, -1f, 0f, 1f, 1f, 0f);
		vertices.clear();
		vertices.put(quadData);
		vertices.flip();

		fbo.getColorBufferTexture().bind(0);

		// Unified single-pass composite post-processing
		SmoothPixelShader shader = SmoothPixelShader.get();
		shader.uTex.value1i(0);
		shader.drawQuad(
			vertices, (float)lastW, (float)lastH,
			brightness,
			vignetteEnabled ? 0.85f : 0.0f,
			tintR, tintG, tintB, tintA,
			bloomEnabled,
			smoothFilterEnabled
		);

		NoosaScript.get().resetCamera();
	}

	public static void setBiomeTint(float r, float g, float b, float a) {
		tintR = r;
		tintG = g;
		tintB = b;
		tintA = a;
	}

	public static void reset() {
		if (fbo != null) {
			try {
				fbo.dispose();
			} catch (Exception ignored) {}
			fbo = null;
		}
		lastW = 0;
		lastH = 0;
	}
}
