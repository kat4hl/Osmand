package net.osmand.osm;

import java.util.Map;

public class Node extends Entity {

	private double latitude;
	private double longitude;
	// currently not used
//	private boolean visible = true;
	
	public Node(double latitude, double longitude, long id){
		super(id);
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	@Override
	public LatLon getLatLon() {
		return new LatLon(latitude, longitude);
	}
	
	@Override
	public void initializeLinks(Map<EntityId, Entity> entities) {
		// nothing to initialize
		
	}

}
