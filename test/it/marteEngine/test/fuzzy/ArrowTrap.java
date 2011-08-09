package it.marteEngine.test.fuzzy;

import it.marteEngine.ME;
import it.marteEngine.ResourceManager;
import it.marteEngine.World;
import it.marteEngine.entity.Entity;
import it.marteEngine.entity.PlatformerEntity;
import it.marteEngine.tween.Ease;
import it.marteEngine.tween.NumTween;
import it.marteEngine.tween.Tween.TweenerMode;

import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.util.Log;

public class ArrowTrap extends Entity {

	private static final int SHOOT_TIME = 2000;

	public static final String ARROW_TRAP = "arrowTrap";

	private static final int SIGHT_SIZE = 6;

	protected boolean faceRight = false;

	private float sx;

	private float sy;

	private Line sight;

	private int shootTimer = 0;

	private boolean fade;

	private NumTween fadeTween = new NumTween(1, 0, 10, TweenerMode.ONESHOT,
			Ease.CUBE_OUT, false);

	private static Sound fireSnd;

	public ArrowTrap(float x, float y) throws SlickException {
		super(x, y);
		setGraphic(ResourceManager.getImage("arrowTrap"));
		addType(ARROW_TRAP, SOLID);
		setHitBox(0, 0, 32, 32);

		sx = x;
		sy = y + height / 2;

		if (faceRight) {
			sight = new Line(sx, sy, sx + 32 * SIGHT_SIZE, sy);
		} else {
			sight = new Line(sx, sy, sx - 32 * SIGHT_SIZE, sy);
		}

		fireSnd = ResourceManager.getSound("fireArrow");

	}

	@Override
	public void update(GameContainer container, int delta)
			throws SlickException {

		super.update(container, delta);

		if (!fade) {
			super.update(container, delta);

			Entity player = collide(PLAYER, x, y - 1);
			if (player != null) {
				fade = true;
				((PlatformerEntity) player).jump();
			}
			// shooting time!
			shoot(delta);

		} else {
			fadeTween.update(delta);
			setAlpha(fadeTween.getValue());
			if (getAlpha() == 0) {
				ME.world.remove(this);
			}
		}

	}

	private void shoot(int delta) throws SlickException {
		shootTimer += delta;
		if (shootTimer > SHOOT_TIME) {
			shootTimer = 0;
			List<Entity> ent = intersect(sight);
			for (Entity entity : ent) {
				if (entity.isType(FuzzyPlayer.PLAYER)) {
					Log.info("player here!!");
					ME.world.add(new Arrow(sx - 20, sy - 5, faceRight),
							World.GAME);
					if (!fireSnd.playing()) {
						fireSnd.play();
					}
					break;
				}
			}
		}
	}

	@Override
	public void render(GameContainer container, Graphics g)
			throws SlickException {
		super.render(container, g);

		if (ME.debugEnabled) {
			g.setColor(Color.red);
			g.draw(sight);
			g.setColor(Color.black);
		}
	}
}
