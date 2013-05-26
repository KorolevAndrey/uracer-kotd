
package com.bitfire.uracer.game.logic.gametasks;

import com.badlogic.gdx.utils.Array;
import com.bitfire.uracer.game.logic.gametasks.hud.Hud;
import com.bitfire.uracer.game.logic.gametasks.messager.Messager;
import com.bitfire.uracer.game.logic.gametasks.sounds.SoundManager;
import com.bitfire.uracer.game.logic.gametasks.trackeffects.TrackEffects;
import com.bitfire.uracer.game.task.TaskManagerEvent;
import com.bitfire.uracer.game.world.GameWorld;

/** Manages the creation and destruction of the main game tasks. */
public final class GameTasksManager {
	private GameWorld gameWorld = null;
	private Array<GameTask> tasks = new Array<GameTask>(10);

	/** keeps track of the concrete game tasks (note that they are all publicly accessible for performance reasons) */

	// physics step
	public PhysicsStep physicsStep = null;

	// sound
	public SoundManager sound = null;

	// alerts and infos
	public Messager messager = null;

	// hud
	public Hud hud = null;

	// special effects
	public TrackEffects effects = null;

	public GameTasksManager (GameWorld world) {
		gameWorld = world;
	}

	public void createTasks () {
		// physics step
		physicsStep = new PhysicsStep(gameWorld.getBox2DWorld(), TaskManagerEvent.Order.MINUS_4);

		// sound manager
		sound = new SoundManager();
		add(sound);

		// message manager
		messager = new Messager();
		add(messager);

		// hud manager
		hud = new Hud();
		add(hud);

		// effects manager
		effects = new TrackEffects();
		add(effects);
	}

	private void add (GameTask task) {
		tasks.add(task);
	}

	public void dispose () {
		for (GameTask task : tasks) {
			task.dispose();
		}
	}

	public void reset () {
		for (GameTask task : tasks) {
			task.reset();
		}
	}

	public void restart () {
		for (GameTask task : tasks) {
			task.restart();
		}
	}
}
