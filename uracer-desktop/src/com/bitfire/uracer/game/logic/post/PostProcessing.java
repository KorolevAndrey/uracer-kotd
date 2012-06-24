package com.bitfire.uracer.game.logic.post;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.LongMap;
import com.bitfire.uracer.configuration.Config;
import com.bitfire.uracer.postprocessing.PostProcessor;
import com.bitfire.uracer.postprocessing.PostProcessorEffect;
import com.bitfire.uracer.postprocessing.effects.Bloom;
import com.bitfire.uracer.postprocessing.effects.CrtMonitor;
import com.bitfire.uracer.postprocessing.effects.Curvature;
import com.bitfire.uracer.postprocessing.effects.Vignette;
import com.bitfire.uracer.postprocessing.effects.Zoomer;
import com.bitfire.uracer.utils.Hash;

/** Encapsulates a post-processor animator that manages effects such as bloom and zoomblur to compose
 * and enhance the gaming experience. */
public class PostProcessing {

	public enum Effects {
		Zoomer, Bloom, Vignette, Crt, Curvature;

		public String name;

		private Effects() {
			name = this.toString();
		}
	}

	private boolean hasPostProcessor;
	private final PostProcessor postProcessor;

	// public access to stored effects
	public LongMap<PostProcessorEffect> effects = new LongMap<PostProcessorEffect>();

	// animators
	public LongMap<PostProcessingAnimator> animators = new LongMap<PostProcessingAnimator>();
	private PostProcessingAnimator currentAnimator;

	public PostProcessing( PostProcessor postProcessor ) {
		this.postProcessor = postProcessor;
		hasPostProcessor = (this.postProcessor != null);

		if( hasPostProcessor ) {
			configurePostProcessor( postProcessor );
			currentAnimator = null;
		}
	}

	public void configurePostProcessor( PostProcessor postProcessor ) {
		postProcessor.setEnabled( true );
		postProcessor.setClearBits( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );
		postProcessor.setClearDepth( 1f );
		postProcessor.setBufferTextureWrap( TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );

		if( UserPreferences.bool( Preference.Zoom ) ) {
			Zoomer z = (UserPreferences.bool( Preference.ZoomRadialBlur ) ? new Zoomer( RadialBlur.Quality.valueOf( UserPreferences.string( Preference.ZoomRadialBlurQuality ) ) ) : new Zoomer());
			z.setBlurStrength( 0 );
			addEffect( Effects.Zoomer.name, z );
		}

		if( UserPreferences.bool( Preference.Bloom ) ) {
			addEffect( Effects.Bloom.name, new Bloom( Config.PostProcessing.RttFboWidth, Config.PostProcessing.RttFboHeight ) );
		}

		if( UserPreferences.bool( Preference.Vignetting ) ) {
			// if there is no bloom, let's control the final saturation via
			// the vignette filter
			addEffect( Effects.Vignette.name, new Vignette( !UserPreferences.bool( Preference.Bloom ) ) );
		}

		if( UserPreferences.bool( Preference.CrtScreen ) ) {
			addEffect( Effects.Crt.name, new CrtMonitor( UserPreferences.bool( Preference.RadialDistortion ), false ) );
		} else if( UserPreferences.bool( Preference.RadialDistortion ) ) {
			addEffect( Effects.Curvature.name, new Curvature() );
		}

		Gdx.app.log( "PostProcessing", "Post-processing enabled and configured" );
	}

	public void addEffect( String name, PostProcessorEffect effect ) {
		if( hasPostProcessor ) {
			postProcessor.addEffect( effect );
			effects.put( Hash.APHash( name ), effect );
		}
	}

	public PostProcessorEffect getEffect( String name ) {
		return effects.get( Hash.APHash( name ) );
	}

	public void addAnimator( String name, PostProcessingAnimator animator ) {
		animators.put( Hash.APHash( name ), animator );
	}

	public PostProcessingAnimator getAnimator( String name ) {
		return animators.get( Hash.APHash( name ) );
	}

	public void enableAnimator( String name ) {
		if( !hasPostProcessor ) {
			return;
		}

		PostProcessingAnimator next = animators.get( Hash.APHash( name ) );
		if( next != null ) {
			currentAnimator = next;
			currentAnimator.reset();
		}
	}

	public void disableAnimator() {
		if( hasPostProcessor && currentAnimator != null ) {
			currentAnimator.reset();
			currentAnimator = null;
		}
	}

	public void onBeforeRender() {
		if( hasPostProcessor && currentAnimator != null ) {
			currentAnimator.update();
		}
	}
}
