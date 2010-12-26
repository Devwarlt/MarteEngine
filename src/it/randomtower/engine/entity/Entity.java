package it.randomtower.engine.entity;

import it.randomtower.engine.ME;
import it.randomtower.engine.StateManager;
import it.randomtower.engine.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

public abstract class Entity implements Comparable<Entity> {

	/** default collidable type SOLID **/
	public static final String SOLID = "Solid";

	/** predefined type for player */
	public static final String PLAYER = "Player";

	/** the world this entity lives in */
	public World world = null;
	
	/** unique identifier **/
	public String name;

	/** x position **/
	public float x;
	/** y position **/
	public float y;
	
	/** x,y is the center of the image/animation, otherwise it's top left corner */
	private boolean centered = false;
	
	/** width of the entity. not necessarily the width of the hitbox. Used for world wrapping */
	public int width;
	/** height of the entity. not necessarily the height of the hitbox. Used for world wrapping */
	public int height;
	
	public float previousx, previousy;

	/** start x and y position stored for reseting for example. very helpful */
	public float startx, starty;
	
	public boolean wrapHorizontal = false;
	public boolean wrapVertical = false;
	
	/** speed vector (x,y): specifies x and y movement per update call in pixels **/
	public Vector2f speed = null;
	
	/** angle in degrees from 0 to 360, used for drawing the entity rotated. NOT used for direction! */
	public int angle = 0;
	
	/** scale used for both horizontal and vertical scaling. */
	public float scale = 1.0f;
	
	private Hashtable<String, Alarm> alarms = new Hashtable<String, Alarm>();

	/** spritesheet that holds animations **/
	protected SpriteSheet sheet;
	public Hashtable<String, Animation> animations = new Hashtable<String, Animation>();
	public String currentAnim;
	public int duration = 200;
	public int depth = -1;

	/** static image for not-animated entity **/
	public Image currentImage;
	
	/** available commands for entity **/
	public Hashtable<String, int[]> commands = new Hashtable<String, int[]>();

	/** collision type for entity **/
	private HashSet<String> type = new HashSet<String>();

	/** true for collidable entity, false otherwise **/
	public boolean collidable = true;
	/** true if this entity should be visible, false otherwise */
	public boolean visible = true;

	/** x offset for collision box */
	public float hitboxOffsetX;
	/** y offset for collision box */
	public float hitboxOffsetY;
	/** hitbox width of entity **/
	public int hitboxWidth;
	/** hitbox height of entity **/
	public int hitboxHeight;

	/** stateManager for entity **/
	public StateManager stateManager;
	
	/**
	 * create a new entity setting initial position (x,y)
	 * 
	 * @param x
	 * @param y
	 */
	public Entity(float x, float y) {
		this.x = x;
		this.y = y;
		this.startx = x;
		this.starty = y;
		stateManager = new StateManager();
	}
	
	public void setCentered(boolean on) {
		int whalf = 0, hhalf = 0;
		if (currentImage != null) {
			whalf = currentImage.getWidth() / 2;
			hhalf = currentImage.getHeight() / 2;
		}
		if (currentAnim != null) {
			whalf = animations.get(currentAnim).getWidth() / 2;
			hhalf = animations.get(currentAnim).getHeight() / 2;
		}
		if (on) {
			// modify hitbox position accordingly - move it a bit up and left
			this.hitboxOffsetX -= whalf;
			this.hitboxOffsetY -= hhalf;
			this.centered = true;
		} else {
			if (centered == true) {
				// reset hitbox position to top left origin
				this.hitboxOffsetX += whalf;
				this.hitboxOffsetY += hhalf;
			}
			this.centered = false;
		}
	}

	/**
	 * Update entity animation
	 * 
	 * @param container
	 * @param delta
	 * @throws SlickException
	 */
	public void update(GameContainer container, int delta)
			throws SlickException {
		if (stateManager!=null && stateManager.currentState()!=null){
			stateManager.update(container, delta);
			return;
		}
		if (animations != null) {
			if (currentAnim != null) {
				Animation anim = animations.get(currentAnim);
				if (anim != null) {
					anim.update(delta);
				}
			}
		}
		if (speed != null) {
			x += speed.x;
			y += speed.y;
		}
		checkWorldBoundaries();
		previousx = x;
		previousy = y;
	}

	/**
	 * Render entity
	 * 
	 * @param container
	 * @param g
	 * @throws SlickException
	 */
	public void render(GameContainer container, Graphics g)
			throws SlickException {
		if (!visible)
			return;
		if (stateManager!=null && stateManager.currentState()!=null){
			stateManager.render(g);
			return;
		}
		float xpos = x, ypos = y;
		if (currentAnim != null) {
			Animation anim = animations.get(currentAnim);
			int w = anim.getWidth()/2;
			int h = anim.getHeight()/2;
			if (centered) {
				xpos -= w;
				ypos -= h;
			}
			if (scale != 1.0f) {
				if (centered)
					g.translate(xpos-(w*scale-w),ypos-(h*scale-h));
				else
					g.translate(xpos,ypos);
				g.scale(scale, scale);
				if (angle != 0)
					g.rotate(x, y, angle);
				anim.draw(0, 0);
			} else {
				if (angle != 0)
					g.rotate(x, y, angle);
				anim.draw(xpos, ypos);
			}
			if (angle != 0 || scale != 1.0f)
				g.resetTransform();
		} else if (currentImage != null) {
			int w = currentImage.getWidth()/2;
			int h = currentImage.getHeight()/2;
			if (centered) {
				xpos -= w;
				ypos -= h;
				currentImage.setCenterOfRotation(w, h);
			} else
				currentImage.setCenterOfRotation(0, 0);
			
			if (angle != 0) {
				currentImage.setRotation(angle);
			}
			if (scale != 1.0f) {
				if (centered)
					g.translate(xpos-(w*scale-w),ypos-(h*scale-h));
				else
					g.translate(xpos,ypos);
				g.scale(scale, scale);
				g.drawImage(currentImage, 0, 0);
			}
			else
				g.drawImage(currentImage, xpos, ypos);
			if (scale != 1.0f)
				g.resetTransform();
		}
		if (ME.debugEnabled) {
			g.setColor(ME.borderColor);
			Rectangle hitBox = new Rectangle(x + hitboxOffsetX, y + hitboxOffsetY, hitboxWidth, hitboxHeight);
			g.draw(hitBox);
			g.setColor(Color.white);
			g.drawRect(x, y, 1, 1);
		}
	}

	/**
	 * Set an image as graphic
	 * @param image
	 */
	public void setGraphic(Image image) {
		this.currentImage = image;
		this.width = image.getWidth();
		this.height = image.getHeight();
	}

	/**
	 * Set a sprite sheet as graphic
	 * @param sheet
	 */
	public void setGraphic(SpriteSheet sheet) {
		this.sheet = sheet;
		this.width = sheet.getSprite(0, 0).getWidth();
		this.height = sheet.getSprite(0, 0).getHeight();
	}

	/**
	 * Add animation to entity, first animation added is current animation
	 * 
	 * @param name
	 * @param loop
	 * @param row
	 * @param frames
	 */
	public void addAnimation(String name, boolean loop, int row, int... frames) {
		Animation anim = new Animation(false);
		anim.setLooping(loop);
		for (int i = 0; i < frames.length; i++) {
			anim.addFrame(sheet.getSprite(frames[i], row), duration);
		}
		if (animations.size() == 0) {
			currentAnim = name;
		}
		animations.put(name, anim);
	}

	/**
	 * define commands
	 * 
	 * @param key
	 * @param keys
	 */
	public void define(String command, int... keys) {
		commands.put(command, keys);
	}

	/**
	 * Check if a command is down
	 * 
	 * @param key
	 * @return
	 */
	public boolean check(String command) {
		int[] checked = commands.get(command);
		if (checked == null)
			return false;
		for (int i = 0; i < checked.length; i++) {
			if (world.container.getInput().isKeyDown(checked[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if a command is pressed
	 * 
	 * @param key
	 * @return
	 */
	public boolean pressed(String command) {
		int[] checked = commands.get(command);
		if (checked == null)
			return false;
		for (int i = 0; i < checked.length; i++) {
			if (world.container.getInput().isKeyPressed(checked[i])) {
				return true;
			} else if (checked[i] == Input.MOUSE_LEFT_BUTTON
					|| checked[i] == Input.MOUSE_RIGHT_BUTTON) {
				if (world.container.getInput().isMousePressed(checked[i])) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Compare to another entity on zLevel
	 */
	public int compareTo(Entity o) {
		if (depth == o.depth)
			return 0;
		if (depth > o.depth)
			return 1;
		return -1;
	}

	/**
	 * Set hitbox for collision (by default if and entity have an hitbox, is
	 * collidable against other entities)
	 * 
	 * @param xOffset
	 * @param yOffset
	 * @param width
	 * @param height
	 */
	public void setHitBox(float xOffset, float yOffset, int width, int height) {
		setHitBox(xOffset, yOffset, width, height, true);
	}

	/**
	 * Set hitbox for collision and set if is collidable against other entities
	 * 
	 * @param xOffset
	 * @param yOffset
	 * @param width
	 * @param height
	 * @param collidable
	 */
	public void setHitBox(float xOffset, float yOffset, int width, int height, boolean collidable) {
		this.hitboxOffsetX = xOffset;
		this.hitboxOffsetY = yOffset;
		this.hitboxWidth = width;
		this.hitboxHeight = height;
		this.collidable = true;
	}

	/**
	 * Add collision types to entity
	 * 
	 * @param types
	 * @return
	 */
	public boolean addType(String... types) {
		return type.addAll(Arrays.asList(types));
	}

	/**
	 * check collision with another entity of given type
	 * 
	 * @param type
	 * @param x
	 * @param y
	 * @return
	 */
	public Entity collide(String type, float x, float y) {
		if (type == null || type.isEmpty())
			return null;
		// offset
		for (Entity entity : world.getEntities()) {
			if (entity.collidable && entity.type.contains(type)) {
				if (!entity.equals(this)
						&& x + hitboxOffsetX + hitboxWidth > entity.x + entity.hitboxOffsetX
						&& y + hitboxOffsetY + hitboxHeight > entity.y + entity.hitboxOffsetY
						&& x + hitboxOffsetX < entity.x + entity.hitboxOffsetX
								+ entity.hitboxWidth
						&& y + hitboxOffsetY < entity.y + entity.hitboxOffsetY
								+ entity.hitboxHeight) {
					this.collisionResponse(entity);
					entity.collisionResponse(this);
					return entity;
				}
			}
		}
		return null;
	}

	public Entity collide(String[] types, float x, float y) {
		for (String type : types) {
			Entity e = collide(type, x, y);
			if (e != null)
				return e;
		}
		return null;
	}
	
	public Entity collideWith(Entity other, float x, float y) {
		if (other.collidable) {
			if (!other.equals(this)
					&& x + hitboxOffsetX + hitboxWidth > other.x + other.hitboxOffsetX
					&& y + hitboxOffsetY + hitboxHeight > other.y + other.hitboxOffsetY
					&& x + hitboxOffsetX < other.x + other.hitboxOffsetX
							+ other.hitboxWidth
					&& y + hitboxOffsetY < other.y + other.hitboxOffsetY
							+ other.hitboxHeight) {
				this.collisionResponse(other);
				other.collisionResponse(this);
				return other;
			}
			return null;
		}
		return null;
	}

	
	public List<Entity> collideInto(String type, float x, float y) {
		if (type == null || type.isEmpty())
			return null;
		ArrayList<Entity> collidingEntities = null;
		for (Entity entity : world.getEntities()) {
			if (entity.collidable && entity.type.contains(type)) {
				if (!entity.equals(this)
						&& x + hitboxOffsetX + hitboxWidth > entity.x + entity.hitboxOffsetX
						&& y + hitboxOffsetY + hitboxHeight > entity.y + entity.hitboxOffsetY
						&& x + hitboxOffsetX < entity.x + entity.hitboxOffsetX
								+ entity.hitboxWidth
						&& y + hitboxOffsetY < entity.y + entity.hitboxOffsetY
								+ entity.hitboxHeight) {
					this.collisionResponse(entity);
					entity.collisionResponse(this);
					if (collidingEntities == null)
						collidingEntities = new ArrayList<Entity>();
					collidingEntities.add(entity);
				}
			}
		}
		return collidingEntities;
	}
	
	/**
	 * overload if you want to act on addition to world
	 */
	public void addedToWorld() {
		
	}
	
	/**
	 * overload if you want to act on removal from world
	 */
	public void removedFromWorld() {
		
	}

	/**
	 * Response to a collision with another entity
	 * 
	 * @param other
	 */
	public void collisionResponse(Entity other) {

	}

	/**
	 * overload if you want to act on leaving world boundaries
	 */
	public void leftWorldBoundaries() {
		
	}

	public Image getCurrentImage() {
		return currentImage;
	}

	public void setWorld(World world) {
		this.world = world;
	}
	
	public void checkWorldBoundaries() {
		if ((x + width) < 0) {
			leftWorldBoundaries();
			if (wrapHorizontal) {
				x = this.world.width + 1;
			}
		}
		if (x > this.world.width) {
			leftWorldBoundaries();
			if (wrapHorizontal) {
				x = (-width+1);
			}
		}
		if ((y + height) < 0) {
			leftWorldBoundaries();
			if (wrapVertical) {
				y = this.world.height + 1;
			}
		}
		if (y > this.world.height) {
			leftWorldBoundaries();
			if (wrapVertical) {
				y = (-height+1);
			}
		}
	}
	
	private String getTypes() {
		StringBuffer types = new StringBuffer();
		for (String singleType : type) {
			if (types.length() > 0)
				types.append(", ");
			types.append(singleType);
		}
		return types.toString();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("name: " + name);
		sb.append(", types: " + getTypes());
		sb.append(", depth: " + depth);
		sb.append(", x: " + this.x);
		sb.append(", y: " + this.y);
		return sb.toString();
	}

	public HashSet<String> getType() {
		return type;
	}
	
	public boolean isType(String type) {
		return type.contains(type);
	}
	
	/**
	 * remove ourselves from world
	 */
	public void destroy() {
		this.world.remove(this);
	}
	
	/***************** some methods to deal with angles and vectors ************************************/
	
	public int getAngleToPosition(Vector2f otherPos) {
		Vector2f diff = otherPos.sub(new Vector2f(x,y));
		return (((int) diff.getTheta()) + 90) % 360;
	}


	public int getAngleDiff(int angle1, int angle2) {
    	return ((((angle2 - angle1) % 360) + 540) % 360) - 180;
    }


	public Vector2f getPointWithAngleAndDistance(int angle, float distance) {
		Vector2f point;
		float tx, ty;
		double theta = StrictMath.toRadians(angle + 90);
		tx = (float) (this.x + distance * StrictMath.cos(theta));
		ty = (float) (this.y + distance * StrictMath.sin(theta));
		point = new Vector2f(tx, ty);
		return point;
	}
	
	/**
	 * Calculate vector from angle and magnitude 
	 * @param angle
	 * @param magnitude
	 * @return
	 * @author Alex Schearer
	 */
	public static Vector2f calculateVector(float angle, float magnitude) {
		Vector2f v = new Vector2f();
		v.x = (float) Math.sin(Math.toRadians(angle));
		v.x *= magnitude;
		v.y = (float) -Math.cos(Math.toRadians(angle));
		v.y *= magnitude;
		return v;
	}	

	/***************** some methods to deal with alarms ************************************/
	public void setAlarm(String name, int triggerTime, boolean oneShot, boolean startNow) {
		Alarm alarm = new Alarm(name, triggerTime, oneShot);
		alarms.put(name, alarm);
		if (startNow)
			alarm.start();
	}
	
	public void restartAlarm(String name) {
		Alarm alarm = alarms.get(name);
		if (alarm != null)
			alarm.start();
	}

	public void pauseAlarm(String name) {
		Alarm alarm = alarms.get(name);
		if (alarm != null)
			alarm.pause();
	}

	public void resumeAlarm(String name) {
		Alarm alarm = alarms.get(name);
		if (alarm != null)
			alarm.resume();
	}
	
	public void destroyAlarm(String name) {
		Alarm alarm = alarms.get(name);
		if (alarm != null)
			alarm.setDead(true);
	}
	
	/**
	 * overwrite this method if your entity shall react on alarms that reached their triggerTime
	 * @param name the name of the alarm that triggered right now
	 */
	public void alarmTriggered(String name) {
		// this method needs to be overwritten to deal with alarms
	}

	/**
	 * this method is called automatically by the World and must not be called by your game code.
	 * Don't touch this method ;-)
	 * Consider it private!
	 */
	public void updateAlarms() {
		ArrayList<String> deadAlarms = null;
		Set<String> alarmNames = alarms.keySet();
		if (!alarmNames.isEmpty()) {
			for (String alarmName : alarmNames) {
				Alarm alarm = alarms.get(alarmName);
				if (alarm.isActive()) {
					boolean retval = alarm.update();
					if (retval) {
						alarmTriggered(alarm.getName());
						if (alarm.isOneShotAlaram()) {
							alarm.setActive(false);
						} else {
							alarm.start();
						}
					}
				}
				if (alarm.isDead()) {
					if (deadAlarms == null)
						deadAlarms = new ArrayList<String>();
					deadAlarms.add(alarmName);
				}
			}
			if (deadAlarms != null) {
				for (String deadAlarm : deadAlarms) {
					alarms.put(deadAlarm, null);
				}
			}
		}
	}
}
