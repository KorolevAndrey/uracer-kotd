
package com.bitfire.uracer.game.logic.gametasks;

import com.bitfire.uracer.game.logic.gametasks.sounds.SoundEffect;
import com.bitfire.utils.ItemsManager;

public class SoundManager extends GameTask {
	private final ItemsManager<SoundEffect> manager = new ItemsManager<SoundEffect>();

	public SoundManager () {
	}

	@Override
	public void dispose () {
		super.dispose();
		manager.dispose();
	}

	public void add (SoundEffect effect) {
		manager.add(effect);
	}

	public void remove (SoundEffect effect) {
		effect.stop();
		manager.remove(effect);
	}

	public void stop () {
		for (SoundEffect s : manager) {
			s.stop();
		}
	}

	@Override
	public void onPause () {
		for (SoundEffect s : manager) {
			s.pause();
		}
	}

	@Override
	public void onResume () {
		for (SoundEffect s : manager) {
			s.resume();
		}
	}

	@Override
	protected void onTick () {
		for (SoundEffect s : manager) {
			s.tick();
		}
	}

	@Override
	public void onRestart () {
		for (SoundEffect s : manager) {
			s.restart();
		}
	}

	@Override
	public void onReset () {
		for (SoundEffect s : manager) {
			s.reset();
		}
	}
}
