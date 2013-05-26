
package com.bitfire.uracer.game.logic.gametasks.hud;

import com.badlogic.gdx.utils.Array;
import com.bitfire.uracer.events.GameRendererEvent;
import com.bitfire.uracer.events.GameRendererEvent.Order;
import com.bitfire.uracer.events.GameRendererEvent.Type;
import com.bitfire.uracer.game.GameEvents;
import com.bitfire.uracer.game.logic.gametasks.GameTask;
import com.bitfire.utils.ItemsManager;

/** Encapsulates an head-up manager that will callback HudElement events for their updating and drawing operations. */
public final class Hud extends GameTask implements GameRendererEvent.Listener {

	private static final GameRendererEvent.Type RenderEventBeforePost = GameRendererEvent.Type.BatchAfterMeshes;
	private static final GameRendererEvent.Type RenderEventAfterPost = GameRendererEvent.Type.BatchAfterPostProcessing;
	private static final GameRendererEvent.Type RenderEventDebug = GameRendererEvent.Type.BatchDebug;

	private final ItemsManager<HudElement> managerBeforePost = new ItemsManager<HudElement>();
	private final ItemsManager<HudElement> managerAfterPost = new ItemsManager<HudElement>();
	private final ItemsManager<HudElement> managerDebug = new ItemsManager<HudElement>();

	public Hud () {
		GameEvents.gameRenderer.addListener(this, RenderEventBeforePost, GameRendererEvent.Order.DEFAULT);
		GameEvents.gameRenderer.addListener(this, RenderEventAfterPost, GameRendererEvent.Order.DEFAULT);
		GameEvents.gameRenderer.addListener(this, RenderEventDebug, GameRendererEvent.Order.DEFAULT);
	}

	@Override
	public void gameRendererEvent (GameRendererEvent.Type type, Order order) {
		if (order != GameRendererEvent.Order.DEFAULT) {
			return;
		}

		if (type == Type.BatchAfterMeshes) {
			Array<HudElement> items = managerBeforePost.items;
			for (int i = 0; i < items.size; i++) {
				items.get(i).onRender(GameEvents.gameRenderer.batch);
			}
		} else if (type == Type.BatchAfterPostProcessing) {
			Array<HudElement> items = managerAfterPost.items;
			for (int i = 0; i < items.size; i++) {
				items.get(i).onRender(GameEvents.gameRenderer.batch);
			}
		} else if (type == Type.BatchDebug) {
			Array<HudElement> items = managerDebug.items;
			for (int i = 0; i < items.size; i++) {
				items.get(i).onRender(GameEvents.gameRenderer.batch);
			}
		}
	}

	public void addBeforePostProcessing (HudElement element) {
		managerBeforePost.add(element);
	}

	public void addAfterPostProcessing (HudElement element) {
		managerAfterPost.add(element);
	}

	public void addDebug (HudElement element) {
		managerDebug.add(element);
	}

	public void remove (HudElement element) {
		managerBeforePost.remove(element);
		managerAfterPost.remove(element);
		managerDebug.remove(element);
	}

	@Override
	public void dispose () {
		super.dispose();
		GameEvents.gameRenderer.removeListener(this, RenderEventBeforePost, GameRendererEvent.Order.DEFAULT);
		GameEvents.gameRenderer.removeListener(this, RenderEventAfterPost, GameRendererEvent.Order.DEFAULT);
		GameEvents.gameRenderer.removeListener(this, RenderEventDebug, GameRendererEvent.Order.DEFAULT);
		managerBeforePost.dispose();
		managerAfterPost.dispose();
	}

	@Override
	public void reset () {
		for (int i = 0; i < managerBeforePost.items.size; i++) {
			managerBeforePost.items.get(i).onReset();
		}

		for (int i = 0; i < managerAfterPost.items.size; i++) {
			managerAfterPost.items.get(i).onReset();
		}

		for (int i = 0; i < managerDebug.items.size; i++) {
			managerDebug.items.get(i).onReset();
		}
	}

	@Override
	protected void onTick () {
		for (int i = 0; i < managerBeforePost.items.size; i++) {
			managerBeforePost.items.get(i).onTick();
		}

		for (int i = 0; i < managerAfterPost.items.size; i++) {
			managerAfterPost.items.get(i).onTick();
		}

		for (int i = 0; i < managerDebug.items.size; i++) {
			managerDebug.items.get(i).onTick();
		}
	}
}
