/*
 * Eternity Pixel Dungeon
 * Post-Processing Shaders - Smooth Pixel & Edge Anti-Aliasing Shader
 */

package com.watabou.glscripts;

import com.badlogic.gdx.Gdx;
import com.watabou.glwrap.Attribute;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Uniform;

import java.nio.Buffer;
import java.nio.FloatBuffer;

public class SmoothPixelShader extends Script {

	public Uniform uTex;
	public Uniform uResolution;
	public Uniform uBrightness;
	public Uniform uVignetteIntensity;
	public Uniform uColorTint;
	public Uniform uBloomEnabled;
	public Uniform uSmoothFilter;
	public Attribute aXY;
	public Attribute aUV;

	public SmoothPixelShader() {
		super();
		compile( SHADER );
		uTex               = uniform( "uTex" );
		uResolution        = uniform( "uResolution" );
		uBrightness        = uniform( "uBrightness" );
		uVignetteIntensity = uniform( "uVignetteIntensity" );
		uColorTint         = uniform( "uColorTint" );
		uBloomEnabled      = uniform( "uBloomEnabled" );
		uSmoothFilter      = uniform( "uSmoothFilter" );
		aXY                = attribute( "aXYZW" );
		aUV                = attribute( "aUV" );
	}

	@Override
	public void use() {
		super.use();
		aXY.enable();
		aUV.enable();
	}

	public void drawQuad( FloatBuffer vertices, float width, float height ) {
		drawQuad( vertices, width, height, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, false, true );
	}

	public void drawQuad( FloatBuffer vertices, float width, float height,
						  float brightness, float vignetteIntensity,
						  float tintR, float tintG, float tintB, float tintA,
						  boolean bloom, boolean smooth ) {
		if (uResolution != null) uResolution.value2f( width, height );
		if (uBrightness != null) uBrightness.value1f( brightness );
		if (uVignetteIntensity != null) uVignetteIntensity.value1f( vignetteIntensity );
		if (uColorTint != null) uColorTint.value4f( tintR, tintG, tintB, tintA );
		if (uBloomEnabled != null) uBloomEnabled.value1f( bloom ? 1.0f : 0.0f );
		if (uSmoothFilter != null) uSmoothFilter.value1f( smooth ? 1.0f : 0.0f );

		((Buffer)vertices).position( 0 );
		aXY.vertexPointer( 2, 4, vertices );

		((Buffer)vertices).position( 2 );
		aUV.vertexPointer( 2, 4, vertices );

		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE, Gdx.gl20.GL_UNSIGNED_SHORT, 0 );
	}

	public static SmoothPixelShader get() {
		return Script.use( SmoothPixelShader.class );
	}

	private static final String SHADER =
		// Vertex shader
		"attribute vec4 aXYZW;\n" +
		"attribute vec2 aUV;\n" +
		"varying vec2 vUV;\n" +
		"void main() {\n" +
		"  gl_Position = aXYZW;\n" +
		"  vUV = aUV;\n" +
		"}\n" +
		"//\n" +
		// Fragment shader: Smart Anti-Aliased Pixel Filter & Composite Post-Processing
		"#ifdef GL_ES\n" +
		"  #extension GL_OES_standard_derivatives : enable\n" +
		"  precision mediump float;\n" +
		"#endif\n" +
		"varying vec2 vUV;\n" +
		"uniform sampler2D uTex;\n" +
		"uniform vec2 uResolution;\n" +
		"uniform float uBrightness;\n" +
		"uniform float uVignetteIntensity;\n" +
		"uniform vec4 uColorTint;\n" +
		"uniform float uBloomEnabled;\n" +
		"uniform float uSmoothFilter;\n" +
		"\n" +
		"void main() {\n" +
		"  vec4 color;\n" +
		"  vec2 texSize = uResolution;\n" +
		"  if (uSmoothFilter > 0.5 && texSize.x > 0.0 && texSize.y > 0.0) {\n" +
		"    // Smart subpixel anti-aliasing for pixel art\n" +
		"    vec2 uv = vUV * texSize;\n" +
		"    vec2 seam = floor(uv + 0.5);\n" +
		"    vec2 dudv = max(abs(dFdx(uv)) + abs(dFdy(uv)), vec2(1.0));\n" +
		"    vec2 smoothUV = (seam + clamp((uv - seam) / dudv, -0.5, 0.5)) / texSize;\n" +
		"\n" +
		"    color = texture2D( uTex, smoothUV );\n" +
		"\n" +
		"    // Edge smoothing pass (4-neighborhood anti-aliasing)\n" +
		"    vec2 onePixel = vec2(1.0) / texSize;\n" +
		"    vec4 cN = texture2D( uTex, smoothUV + vec2(0.0, -onePixel.y) );\n" +
		"    vec4 cS = texture2D( uTex, smoothUV + vec2(0.0,  onePixel.y) );\n" +
		"    vec4 cW = texture2D( uTex, smoothUV + vec2(-onePixel.x, 0.0) );\n" +
		"    vec4 cE = texture2D( uTex, smoothUV + vec2( onePixel.x, 0.0) );\n" +
		"\n" +
		"    vec4 neighborAvg = (cN + cS + cW + cE) * 0.25;\n" +
		"    float diff = length(color.rgb - neighborAvg.rgb);\n" +
		"    if (diff > 0.08) {\n" +
		"      color = mix(color, neighborAvg, 0.25);\n" +
		"    }\n" +
		"  } else {\n" +
		"    color = texture2D( uTex, vUV );\n" +
		"  }\n" +
		"\n" +
		"  vec3 rgb = color.rgb;\n" +
		"\n" +
		"  // Bloom highlight boost\n" +
		"  if (uBloomEnabled > 0.5) {\n" +
		"    float lum = max(max(rgb.r, rgb.g), rgb.b);\n" +
		"    if (lum > 0.55) {\n" +
		"      float factor = (lum - 0.55) / 0.45;\n" +
		"      rgb += rgb * factor * 0.65;\n" +
		"    }\n" +
		"  }\n" +
		"\n" +
		"  // Biome / LUT color grading\n" +
		"  if (uColorTint.a > 0.0) {\n" +
		"    vec3 tinted = rgb * uColorTint.rgb;\n" +
		"    rgb = mix(rgb, tinted, uColorTint.a);\n" +
		"  }\n" +
		"\n" +
		"  // Vignette\n" +
		"  if (uVignetteIntensity > 0.0) {\n" +
		"    vec2 uv = vUV - 0.5;\n" +
		"    float dist = length(uv);\n" +
		"    float vignette = smoothstep(0.7, 0.2, dist * uVignetteIntensity);\n" +
		"    rgb *= vignette;\n" +
		"  }\n" +
		"\n" +
		"  // Brightness multiplier\n" +
		"  rgb *= uBrightness;\n" +
		"\n" +
		"  gl_FragColor = vec4(rgb, color.a);\n" +
		"}\n";
}
