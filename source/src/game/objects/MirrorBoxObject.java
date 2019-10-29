package game.objects;

import processing.core.PApplet;
import game.projectiles.MagicProjectile;
import game.projectiles.ProjectileObject;
import game.projectiles.Swing;
import game.engine.sidescroller.Tileset;
import processing.core.PVector;

public class MirrorBoxObject extends GameObject {

	public int direction;
	public boolean rotating;
	public boolean activated;
	public boolean collided;
	final int BOX_RIGHT = 0;
	final int BOX_DOWN = 1;
	final int BOX_LEFT = 2;
	final int BOX_UP = 3;
	final int BOX_W = 64;
	final int BOX_H = 64;

	public MirrorBoxObject() {
		super();

		direction = 0;

		type = type.OBJECT;

		baseObjectData.setId("MIRROR_BOX");
		image = Tileset.getTile("MIRROR_BOX");

		baseObjectData.setWidth(BOX_W);
		baseObjectData.setHeight(BOX_H);

		collision = new CollidableObject(BOX_W, BOX_H, 0, 0, true);
		collision.flag = "TRANSPARENT_BULLET";
		gameScene.collidableObjects.add(collision);
	}

	public void display() {
		switch (direction) {
		case BOX_RIGHT:
			setMirrorBox(0);
			break;
		case BOX_DOWN:
			setMirrorBox(90);
			break;
		case BOX_LEFT:
			setMirrorBox(180);
			break;
		case BOX_UP:
			setMirrorBox(270);
			break;
		}
	}

	public void update() {
		if (rotating) 
	      	image = animation.animate();
		collision.getBaseObjectData().setPos(baseObjectData.getPos());

		// Change Mirror Box Axis
		for (int i = 0; i < gameScene.getPlayer().swings.size(); i++) {
			Swing swing = gameScene.getPlayer().swings.get(i);

			if (collidesWithSwing(swing)) {
				if (!swing.activated) {
					rotating = true;


					// Setup Animation					
					animation.changeAnimation(Tileset.getAnimation("MIRROR_BOX::ROTATE"), false, 1);

					swing.activated = true;
				}
			}
		}

		// Reflect Magic Projectile
		for (int i = 0; i < gameScene.projectileObjects.size(); i++) {
			ProjectileObject projectile = gameScene.projectileObjects.get(i);

			activated = false;
			if (projectile.id.equals("MAGIC")) {
				if (collidesWithProjectile(projectile) && !projectile.hit) {
					activated = true;
					projectileDirection(projectile);
				}
				activateMirrorBox();
			}
		}
		setMirrorDirection();
	}

	public void activateMirrorBox() {
		if (!rotating) {
			if (activated) {
				image = Tileset.getTile("MIRROR_BOX_HIT");
			} else {
				image = Tileset.getTile("MIRROR_BOX");
			}
		}
	}

	public void setMirrorDirection() {
		if (animation.ended && rotating) {
			image = Tileset.getTile("MIRROR_BOX");
			rotating = false;
			direction = (direction + 1) % 4;// Allow rotation with the use of modulus
		}
	}

	public void projectileDirection(ProjectileObject projectile) {
		// Give multi direction for each rotation
		/*
		 * Each mirror has a composite to their actual position which they hit for e.g.
		 * BOX_RIGHT when something hits it down it supposed to deflect it right so it
		 * rotation is | | v \ - - > There is also the possibility of :
		 * 
		 * ^ | | \ < - - - The code allows this to happen setting the appropiate
		 * position of the projectile so it is properly alligned with the mirror box.
		 */
		switch (direction) {
		case BOX_RIGHT:
			bounceProjectile(projectile, LEFT, UP, 'x');
			bounceProjectile(projectile, DOWN, RIGHT, 'y');
			return;
		case BOX_DOWN:
			bounceProjectile(projectile, LEFT, DOWN, 'x');
			bounceProjectile(projectile, UP, RIGHT, 'y');
			return;
		case BOX_LEFT:
			bounceProjectile(projectile, UP, LEFT, 'y');
			bounceProjectile(projectile, RIGHT, DOWN, 'x');
			return;
		case BOX_UP:
			bounceProjectile(projectile, RIGHT, UP, 'x');
			bounceProjectile(projectile, DOWN, LEFT, 'y');
			return;
		}
	}

	public boolean collidesWithSwing(Swing swing) {
		PVector pos = baseObjectData.getPos();
		int width = baseObjectData.getWidth();
		int height = baseObjectData.getHeight();
		return (swing.pos.x + swing.width / 2 > pos.x - width / 2 && swing.pos.x - swing.width / 2 < pos.x + width / 2)
				&& (swing.pos.y + swing.height / 2 > pos.y - height / 2
						&& swing.pos.y - swing.height / 2 < pos.y + height / 2);
	}

	public boolean collidesWithProjectile(ProjectileObject swing) {
		PVector pos = baseObjectData.getPos();
		int width = baseObjectData.getWidth();
		int height = baseObjectData.getHeight();
		return (swing.pos.x + swing.width / 2 > pos.x - width / 2 && swing.pos.x - swing.width / 2 < pos.x + width / 2)
				&& (swing.pos.y + swing.height / 2 > pos.y - height / 2
						&& swing.pos.y - swing.height / 2 < pos.y + height / 2);
	}

	public void setMirrorBox(float rotate) {
		PVector pos = baseObjectData.getPos();

		// Setup image of mirror and rotation
		applet.pushMatrix();
		applet.translate(pos.x, pos.y);
		applet.rotate(PApplet.radians(rotate));
		applet.image(image, 0, 0);
		applet.popMatrix();
	}
	public void bounceProjectile(ProjectileObject projectile, int flyDir, int deflectDir, char axisSwitch) {
		PVector pos = baseObjectData.getPos();

		//Deflect the projectile based on how it hitting the mirror
		if (projectile.direction == flyDir) {
			projectile.prevDirection = projectile.direction;
			projectile.direction = deflectDir;
			if (axisSwitch == 'x')
				projectile.pos.x = pos.x;
			else if (axisSwitch == 'y')
				projectile.pos.y = pos.y;
		}
		hitWrongSide(projectile, flyDir, deflectDir);
	}

	public void hitWrongSide(ProjectileObject projectile, int flyDir, int deflectDir) {
		/*
		 * we wanna check if we are flying in the direction of the deflect. if we are
		 * and haven't flew back from the origin deflect then that means, we hit the
		 * wrong side.
		 */
		if (projectile.direction == deflectDir && projectile.prevDirection != flyDir)
			((MagicProjectile) projectile).hit(collision);
	}
}