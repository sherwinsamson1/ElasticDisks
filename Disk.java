//
// Sherwin Samson
// 2/27/18
// 
// A subclass of point, where point is the center of the moving disk.
// Also handles collisions between disks.
//


import java.awt.Color;
import java.awt.Graphics;


class Disk extends Point {

	protected int radius;
	public Vect velocity;
	public Color borderColor;
	public boolean anchored = false;	// default value


	// Constructor helper that calls constructor(int, int, int, Color, Color)
	public Disk(int x, int y, int radius, Color color ) {
		this(x, y, radius, color, color);
	}

	// Constructor that creates Point(int, int) and initializes class variables
	public Disk(int x, int y, int radius, Color color, Color borderColor) {
		super(x, y);
		init(radius, color, borderColor);
	}
	
	// Constructor(int, Color)
	// Uses default values of Point()
	public Disk(int radius, Color color) {
		super();
		init(radius, color, color);
	}
	
	// Constructor(int, int, int, Vect, Color)
	public Disk(int x, int y, int radius, Vect velocity, Color c) {
		this(x, y, radius, c, c);
		this.velocity = velocity;
	}
	



	// Method to initialize class values.
	private void init(int radius, Color color, Color borderColor) {
		this.radius = radius;
		this.color = color;
		this.borderColor = borderColor;
		this.velocity = new Vect();
	}
	
	// Takes a disk argument and copies its values.
	public void copyDisk(Disk d) {
        this.color = d.color;
		this.borderColor = d.borderColor;
		this.radius = d.radius;
		this.velocity = d.velocity;
	}
	
	// Creates a random anchored disk, avoiding overlaps with other disks.
	public Disk(Disk[] otherDisks){
		boolean collision;
		Disk temp;
		anchored = true;
		int tries, maxTries = 10000, minRadius = 40;

		do {
			tries = 0;
			do { 
				temp = new Disk(
						Util.getRandom(minX, maxX), 
						Util.getRandom(minY,maxY), 
						Util.getRandom(minRadius, minRadius + (maxX - minX)/9),
						Util.getDarkColor()	,
						Color.LIGHT_GRAY);
				temp.anchored = true;
				
				collision = false;
				for (int j = 0; j < otherDisks.length; j++)  {
					Disk other = otherDisks[j];
					if (other != null && other.collision(temp)) 
						collision = true;
				}
				tries++;
			} while (collision && tries < maxTries);
			minRadius--;
			//if (minRadius < 40) System.out.println(minRadius);
		} while (collision && minRadius >= 1);
		
		copyDisk(temp);
	}

	// Returns true if a disk collides with another disk
	public boolean collision(Disk d) {
		double r = getDistance(d);
		return r <= radius + d.radius  &&  d!= this;
	}
	
	// Draws disk for appletviewer
	public void draw(Graphics g, Color c, Color border) {
		Color temp = g.getColor();  // save color

		g.setColor(c); // temporarily change color

		int d = Util.round(2*radius);
		g.fillOval(Util.round(x-radius), Util.round(y-radius), d,d);

		if (!this.color.equals(this.borderColor)) {
			g.setColor(border);
			g.drawOval(Util.round(x-radius), Util.round(y-radius), d,d);	
		}

		g.setColor(temp); // restore color
	}

	// Calls draw with color and borderColor
	public void draw(Graphics g) {
		draw(g, color, borderColor);
	}


	public void setRadius(int radius) {
		this.radius = radius;
	}
	

	public double getArea() {
		return (double)(Math.PI*radius*radius);
	}


	public void erase(Graphics g) {
		draw(g,background, background);
	}

	// Updates velocity if disk boundary crosses border and is going in that direction
	public void move() {

		if ((x+radius) >= maxX) {
			if (this.velocity.vx > 0){
				this.velocity = this.velocity.negateX();
			}
		}

		if ((x-radius) <= minX) {
			if (this.velocity.vx < 0) {
				this.velocity = this.velocity.negateX();
			}
		}

		if ((y+radius) >= maxY) {
			if (this.velocity.vy > 0) {
				this.velocity = this.velocity.negateY();
			}
		}

		if ((y-radius) <= minY) {
			if (this.velocity.vy < 0) {
				this.velocity = this.velocity.negateY();
			}
		}

		move(velocity);
	}


	public void move(Disk [] disks) {
		if (anchored) return;

		for (int i = 0; i < disks.length; i++){
			if (disks[i] != null)
				handleCollision(disks[i]);
		}
		
		double speed = this.velocity.getLength(); 

		if (speed > 6){
			this.velocity.setLength(speed * 0.99);  // some friction for super fast speeds
		}

		move();
	}

	// Handles collision angles and velocity.
	public void elasticCollision(Disk d) {
        double r = getDistance(d);

        if (r <= Math.abs(d.radius - this.radius)) return;

        Vect temp = new Vect(this,d);

        if (!this.velocity.getDifference(d.velocity).formsAcuteAngle(temp)) return;
        
        double m1 = this.getArea();
        double m2 = d.getArea();
        
        Vect p1 = this.velocity.getProjection(temp);
        Vect p2 = d.velocity.getProjection(temp);
        
        Vect q1 = this.velocity.getProjection(temp.getAngle() + (Math.PI/2.0));
        Vect q2 = d.velocity.getProjection(temp.getAngle() + (Math.PI/2.0));
        
        Vect newP1 = p1.getProduct(m1-m2).getSum(p2.getProduct(2.0*m2)).getProduct(1.0/(m1+m2));
        Vect newP2 = p2.getProduct(m2-m1).getSum(p1.getProduct(2.0*m1)).getProduct(1.0/(m2+m1));
        this.velocity = q1.getSum(newP1);
        d.velocity = q2.getSum(newP2);
	}

	// Detects if collision is with moving or anchored disks.
	public void handleCollision(Disk d) {

		if (collision(d)){

			if (!d.anchored){
				elasticCollision(d);
			}
			else {
                if(this.getDistance(d) < Math.abs(d.radius-this.radius)) return;

                Vect v = new Vect(this, d);

                if (!this.velocity.getDifference(d.velocity).formsAcuteAngle(v)) return;

                double angle = v.getAngle() + Math.PI/2;
                velocity.reflect(angle);
			}
		}
	}

	// Returns Point.toString() + velocity.toString().
	public String toString() {
		return super.toString() + " " + this.velocity.toString();
	}
	
}

