package ui.events;

import java.awt.event.MouseEvent;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class MousePressedEvent extends AMouseEvent {

	public MousePressedEvent(MouseEvent m) {
		super(m);
	}

	public MouseEvent mouseEvent;
	
	public Point3d rayOrigin; // ?
	public Vector3d ray; // ?
	public Point2d guiCoords; 
	
	public int getID() {
		return IVidisEvent.MousePressedEvent;
	}

}