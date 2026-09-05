/*
 * Eternity Pixel Dungeon
 * Post-Processing Shaders - Vignette and Color Grading (LUT) Shader
 */

package com.watabou.glscripts;

import com.badlogic.gdx.Gdx;
import com.watabou.glwrap.Attribute;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Uniform;

import java.nio.Buffer;
import java.nio.FloatBuffer;

public class VignetteLutShader extends Script {

	public Uniform uTex;
	public Uniform uVignetteIntensity;
	public Uniform uColorTint;
	public Attribute aXY;
	public Attribute aUV;

	public VignetteLutShader() {
		super();
		compile( SHADER );
		uTex               = uniform( "uTex" );
		uVignetteIntensity = uniform( "uVignetteIntensity" );
		uColorTint         = uniform( "uColorTint" );
		aXY                = attribute( "aXYZW" );
		aUV                = attribute( "aUV" );
	}

	@Override
	public void use() {
		super.use();
		aXY.enable();
		aUV.enable();
	}

	public void drawQuad( FloatBuffer vertices ) {
		((Buffer)vertices).position( 0 );
		aXY.vertexPointer( 2, 4, vertices );

		((Buffer)vertices).position( 2 );
		aUV.vertexPointer( 2, 4, vertices );

		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE, Gdx.gl20.GL_UNSIGNED_SHORT, 0 );
	}

	public static VignetteLutShader get() {
		return Script.use( VignetteLutShader.class );
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
		// Fragment shader
		"#ifdef GL_ES\n" +
		"  precision mediump float;\n" +
		"#endif\n" +
		"varying vec2 vUV;\n" +
		"uniform sampler2D uTex;\n" +
		"uniform float uVignetteIntensity;\n" +
		"uniform vec4 uColorTint;\n" +
		"void main() {\n" +
		"  vec4 color = texture2D( uTex, vUV );\n" +
		"  vec2 uv = vUV - 0.5;\n" +
		"  float dist = length(uv);\n" +
		"  float vignette = smoothstep(0.7, 0.2, dist * uVignetteIntensity);\n" +
		"  vec3 tintedColor = color.rgb * uColorTint.rgb;\n" +
		"  vec3 finalColor = mix(color.rgb, tintedColor, uColorTint.a);\n" +
		"  gl_FragColor = vec4(finalColor * vignette, color.a);\n" +
		"}\n";
}
