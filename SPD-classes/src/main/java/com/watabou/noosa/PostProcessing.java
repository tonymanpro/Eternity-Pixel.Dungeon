/*
 * Eternity Pixel Dungeon
 * Post-Processing System (Bloom, Vignette & LUT Shaders)
 */

package com.watabou.noosa;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.watabou.glscripts.BloomShader;
import com.watabou.glscripts.VignetteLutShader;
import com.watabou.glwrap.Quad;

import java.nio.FloatBuffer;

public class PostProcessing {

	private static FrameBuffer fbo;
	private static FloatBuffer vertices;
	private static float[] quadData = new float[16];

	private static int lastW = 0;
	private static int lastH = 0;

	public static boolean bloomEnabled = false;
	public static boolean vignetteEnabled = false;

	// Color tint for biomes / LUT (R, G, B, Intensity/Blend)
	public static float tintR = 1f;
	public static float tintG = 1f;
	public static float tintB = 1f;
	public static float tintA = 0f; // 0 = default, no tint

	public static boolean isEnabled() {
		return bloomEnabled || vignetteEnabled;
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

		// Render Bloom pass if enabled
		if (bloomEnabled) {
			BloomShader bloom = BloomShader.get();
			bloom.uTex.value1i(0);
			bloom.uThreshold.value1f(0.55f);
			bloom.uIntensity.value1f(0.65f);
			bloom.drawQuad(vertices);
		}

		// Render Vignette & Color Grading LUT pass if enabled
		if (vignetteEnabled) {
			VignetteLutShader shader = VignetteLutShader.get();
			shader.uTex.value1i(0);
			shader.uVignetteIntensity.value1f(0.85f);
			shader.uColorTint.value4f(tintR, tintG, tintB, tintA);
			shader.drawQuad(vertices);
		} else if (!bloomEnabled) {
			// Fallback if shaders fail or unhandled
			NoosaScriptNoLighting script = NoosaScriptNoLighting.get();
			script.uTex.value1i(0);
			script.drawQuad(vertices);
		}
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
