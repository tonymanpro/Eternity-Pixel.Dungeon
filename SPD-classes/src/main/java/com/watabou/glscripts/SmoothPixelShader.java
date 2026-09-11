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
	public Attribute aXY;
	public Attribute aUV;

	public SmoothPixelShader() {
		super();
		compile( SHADER );
		uTex        = uniform( "uTex" );
		uResolution = uniform( "uResolution" );
		aXY         = attribute( "aXYZW" );
		aUV         = attribute( "aUV" );
	}

	@Override
	public void use() {
		super.use();
		aXY.enable();
		aUV.enable();
	}

	public void drawQuad( FloatBuffer vertices, float width, float height ) {
		if (uResolution != null) {
			uResolution.value2f( width, height );
		}

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
		// Fragment shader: Smart Anti-Aliased Pixel Filter
		"#ifdef GL_ES\n" +
		"  precision mediump float;\n" +
		"#endif\n" +
		"varying vec2 vUV;\n" +
		"uniform sampler2D uTex;\n" +
		"uniform vec2 uResolution;\n" +
		"\n" +
		"void main() {\n" +
		"  vec2 texSize = uResolution;\n" +
		"  if (texSize.x <= 0.0 || texSize.y <= 0.0) {\n" +
		"    gl_FragColor = texture2D( uTex, vUV );\n" +
		"    return;\n" +
		"  }\n" +
		"\n" +
		"  // Smart subpixel anti-aliasing for pixel art\n" +
		"  vec2 uv = vUV * texSize;\n" +
		"  vec2 seam = floor(uv + 0.5);\n" +
		"  vec2 dudv = max(abs(dFdx(uv)) + abs(dFdy(uv)), vec2(1.0));\n" +
		"  vec2 smoothUV = (seam + clamp((uv - seam) / dudv, -0.5, 0.5)) / texSize;\n" +
		"\n" +
		"  // Sample central smooth texture\n" +
		"  vec4 color = texture2D( uTex, smoothUV );\n" +
		"\n" +
		"  // Edge smoothing pass (4-neighborhood anti-aliasing)\n" +
		"  vec2 onePixel = vec2(1.0) / texSize;\n" +
		"  vec4 cN = texture2D( uTex, smoothUV + vec2(0.0, -onePixel.y) );\n" +
		"  vec4 cS = texture2D( uTex, smoothUV + vec2(0.0,  onePixel.y) );\n" +
		"  vec4 cW = texture2D( uTex, smoothUV + vec2(-onePixel.x, 0.0) );\n" +
		"  vec4 cE = texture2D( uTex, smoothUV + vec2( onePixel.x, 0.0) );\n" +
		"\n" +
		"  vec4 neighborAvg = (cN + cS + cW + cE) * 0.25;\n" +
		"  float diff = length(color.rgb - neighborAvg.rgb);\n" +
		"  if (diff > 0.08) {\n" +
		"    color = mix(color, neighborAvg, 0.25);\n" +
		"  }\n" +
		"\n" +
		"  gl_FragColor = color;\n" +
		"}\n";
}
