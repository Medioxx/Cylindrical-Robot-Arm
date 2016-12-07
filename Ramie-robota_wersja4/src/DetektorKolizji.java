import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Shape3D;
import javax.media.j3d.WakeupOnCollisionEntry;

import com.sun.j3d.utils.geometry.Sphere;




public class DetektorKolizji extends Behavior {
	private boolean inKolizja;
	private Shape3D shape;
	private WakeupOnCollisionEntry wEnter;
	Sphere element;
	public boolean isInKolizja() {
		return inKolizja;
	}

	public void setInKolizja(boolean inCollision) {
		this.inKolizja = inCollision;
	}

	  public DetektorKolizji(Sphere obiekt, BoundingSphere sphere) {
		    inKolizja = false;
		    element = obiekt;
		    element.setCollisionBounds(sphere);
		  }
	
	public void initialize() {
		wEnter = new WakeupOnCollisionEntry(element);
		wakeupOn(wEnter);
	}

	@Override
	public void processStimulus(@SuppressWarnings("rawtypes") Enumeration criteria) {
		inKolizja = true;

		if (inKolizja) {
			System.out.println("Kolizja z robotem");
		}

		wakeupOn(wEnter);

	}
}