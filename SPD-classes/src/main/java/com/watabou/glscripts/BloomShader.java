/*
 * Eternity Pixel Dungeon
 * Post-Processing Shaders - Bloom Shader
 */

package com.watabou.glscripts;

import com.badlogic.gdx.Gdx;
import com.watabou.glwrap.Attribute;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Uniform;

import java.nio.Buffer;
import java.nio.FloatBuffer;

public class BloomShader extends Script {

	public Uniform uTex;
	public Uniform uIntensity;
	public Uniform uThreshold;
	public Attribute aXY;
	public Attribute aUV;

	public BloomShader() {
		super();
		compile( SHADER );
		uTex        = uniform( "uTex" );
		uIntensity  = uniform( "uIntensity" );
		uThreshold  = uniform( "uThreshold" );
		aXY         = attribute( "aXYZW" );
		aUV         = attribute( "aUV" );
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

	public static BloomShader get() {
		return Script.use( BloomShader.class );
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
		"uniform float uIntensity;\n" +
		"uniform float uThreshold;\n" +
		"void main() {\n" +
		"  vec4 baseColor = texture2D( uTex, vUV );\n" +
		"  vec3 color = baseColor.rgb;\n" +
		"  float brightness = max(max(color.r, color.g), color.b);\n" +
		"  vec3 bloom = vec3(0.0);\n" +
		"  if (brightness > uThreshold) {\n" +
		"    float factor = (brightness - uThreshold) / (1.0 - uThreshold);\n" +
		"    bloom = color * factor * uIntensity;\n" +
		"  }\n" +
		"  gl_FragColor = vec4(color + bloom, baseColor.a);\n" +
		"}\n";
}
