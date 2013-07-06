
package com.bitfire.uracer.game.actors;

import com.bitfire.uracer.configuration.Config;
import com.bitfire.uracer.game.GameEvents;
import com.bitfire.uracer.game.events.GhostCarEvent;
import com.bitfire.uracer.game.logic.replaying.Replay;
import com.bitfire.uracer.game.world.GameWorld;

/** Implements an automated Car, playing previously recorded events. It will ignore car-to-car collisions, but will respect
 * in-track collisions and responses.
 * 
 * @author manuel */

public final class GhostCar extends Car {
	private static final int FadeEvents = 30;
	private Replay replay;
	private CarForces[] replayForces;
	private int replayForcesCount;
	private int indexPlay;
	private boolean hasReplay;
	private final int id;
	private boolean fadeOutEventTriggered, startedEventTriggered;
	private boolean started;
	private float alpha;

	public GhostCar (int id, GameWorld gameWorld, CarPreset.Type presetType) {
		super(gameWorld, CarType.ReplayCar, InputMode.InputFromReplay, presetType, false);
		this.id = id;
		started = false;
		replay = new Replay();
		resetDistanceAndSpeed(true, true);
		removeReplay();
		alpha = Config.Graphics.DefaultGhostCarOpacity;
		stillModel.setAlpha(0);
		getTrackState().ghostArrived = false;
	}

	public int getId () {
		return id;
	}

	public Replay getReplay () {
		return replay;
	}

	/** starts playing the available Replay, if any */
	public void start () {
		if (!started) {
			stillModel.setAlpha(0);
			resetWithTrackState();
			setActive(true);
			getTrackState().ghostArrived = false;
			started = true;
		}
	}

	/** stops playing the replay and returns to being idle */
	public void stop () {
		if (started) {
			started = false;
			stillModel.setAlpha(0);
			setActive(false);
			resetPhysics();
		}
	}

	public boolean isPlaying () {
		return started;
	}

	public void setAlpha (float alpha) {
		this.alpha = alpha;
	}

	// input data for this car cames from a Replay object
	public void setReplay (Replay replay) {
		stop();

		hasReplay = (replay != null && replay.getEventsCount() > 0 && replay.isValid());
		replayForces = null;
		replayForcesCount = 0;

		if (hasReplay) {
			this.replay.copyData(replay);
		} else {
			this.replay.reset();
		}

		if (hasReplay) {
			replayForces = replay.getCarForces();
			replayForcesCount = replay.getEventsCount();
			resetWithTrackState();
			indexPlay = 0;
			fadeOutEventTriggered = false;
			startedEventTriggered = false;
			stillModel.setAlpha(0);
		}
	}

	private void resetWithTrackState () {
		getTrackState().ghostArrived = false;

		resetPhysics();
		resetDistanceAndSpeed(true, true);

		if (hasReplay) {
			setWorldPosMt(replay.getStartPosition(), replay.getStartOrientation());
			gameTrack.resetTrackState(this);
		}
	}

	public void removeReplay () {
		stop();
		setReplay(null);
	}

	public boolean hasReplay () {
		return hasReplay;
	}

	@Override
	public boolean isActive () {
		return super.isActive() && hasReplay;
	}

	@Override
	public boolean isVisible () {
		return isActive() && stillModel.getAlpha() > 0;
	}

	@Override
	protected void onComputeCarForces (CarForces forces) {
		// returns empty forces in case its not started nor ready
		forces.reset();

		if (started && hasReplay) {

			if (!startedEventTriggered) {
				startedEventTriggered = true;
				GameEvents.ghostCars.trigger(this, GhostCarEvent.Type.ReplayStarted);
			}

			if (indexPlay < replayForcesCount) {
				forces.set(replayForces[indexPlay]);
			}

			stillModel.setAlpha(alpha);

			// also change opacity, fade in/out based on events played / total events
			if (indexPlay <= FadeEvents) {
				stillModel.setAlpha(((float)indexPlay / (float)FadeEvents) * alpha);
			} else if (replay.getEventsCount() - indexPlay <= FadeEvents) {
				float val = (float)(replay.getEventsCount() - indexPlay) / (float)FadeEvents;
				stillModel.setAlpha(val * alpha);

				if (!fadeOutEventTriggered) {
					fadeOutEventTriggered = true;
					GameEvents.ghostCars.trigger(this, GhostCarEvent.Type.onGhostFadingOut);
				}
			}
		}
	}

	@Override
	public void onAfterPhysicsSubstep () {
		super.onAfterPhysicsSubstep();
		if (!started) return;

		if (hasReplay) {
			indexPlay++;

			if (indexPlay == replayForcesCount) {
				GameEvents.ghostCars.trigger(this, GhostCarEvent.Type.ReplayEnded);
			}
		}
	}
}
