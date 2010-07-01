package com.osmand.views;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.Toast;

import com.osmand.OsmandSettings;
import com.osmand.ResourceManager;
import com.osmand.TransportIndexRepository;
import com.osmand.data.TransportStop;
import com.osmand.osm.MapUtils;

public class TransportStopsLayer implements OsmandMapLayer {
	private static final int startZoom = 12;
	
	
	private Paint pointAltUI;
	private OsmandMapTileView view;
	private List<TransportStop> objects = new ArrayList<TransportStop>();

	private ResourceManager resourceManager;
	
	
	@Override
	public void initLayer(OsmandMapTileView view) {
		this.view = view;

		pointAltUI = new Paint();
		pointAltUI.setColor(Color.rgb(0, 0, 255));
		pointAltUI.setAlpha(200);
		pointAltUI.setAntiAlias(true);
		resourceManager = ResourceManager.getResourceManager();
		pixRect.set(0, 0, view.getWidth(), view.getHeight());
	}
	
	public TransportStop getFromPoint(PointF point){
		if (objects != null) {
			int ex = (int) point.x;
			int ey = (int) point.y;
			int radius = getRadiusPoi(view.getZoom()) * 3 / 2;
			try {
				for (int i = 0; i < objects.size(); i++) {
					TransportStop n = objects.get(i);
					int x = view.getRotatedMapXForPoint(n.getLocation().getLatitude(), n.getLocation().getLongitude());
					int y = view.getRotatedMapYForPoint(n.getLocation().getLatitude(), n.getLocation().getLongitude());
					if (Math.abs(x - ex) <= radius && Math.abs(y - ey) <= radius) {
						return n;
					}
				}
			} catch (IndexOutOfBoundsException e) {
				// that's really rare case, but is much efficient than introduce synchronized block
			}
		}
		return null;
	}
	


	
	public int getTileSize(){
		return view.getTileSize();
	}

	
	@Override
	public boolean onTouchEvent(PointF point) {
		TransportStop n = getFromPoint(point);
		if(n != null){
			StringBuilder text = new StringBuilder(250);
			text.append("Stop").append(" : ").append(n.getName(OsmandSettings.usingEnglishNames(view.getContext()))); //$NON-NLS-2$
			text.append("\n").append("Routes").append(" : ");  //$NON-NLS-1$//$NON-NLS-3$
			List<TransportIndexRepository> reps = ResourceManager.getResourceManager().searchTransportRepositories(n.getLocation().getLatitude(), n.getLocation().getLongitude());
			if(!reps.isEmpty()){
				List<String> l;
				if(OsmandSettings.usingEnglishNames(view.getContext())){
					 l = reps.get(0).getRouteDescriptionsForStop(n, "{1} {0} - {3}"); //$NON-NLS-1$
				} else {
					l = reps.get(0).getRouteDescriptionsForStop(n, "{1} {0} - {2}"); //$NON-NLS-1$
				}
				for(String s : l){
					text.append("\n").append(s); //$NON-NLS-1$
				}
			}
			Toast.makeText(view.getContext(), text.toString(), Toast.LENGTH_LONG).show();
			return true;
		}
		return false;
	}
	
	public int getRadiusPoi(int zoom){
		if(zoom < startZoom){
			return 0;
		} else if(zoom <= 15){
			return 7;
		} else if(zoom == 16){
			return 10;
		} else if(zoom == 17){
			return 14;
		} else {
			return 18;
		}
	}

	Rect pixRect = new Rect();
	RectF tileRect = new RectF();
	
	@Override
	public void onDraw(Canvas canvas) {
		if (view.getZoom() >= startZoom) {
			pixRect.set(0, 0, view.getWidth(), view.getHeight());
			view.calculateTileRectangle(pixRect, view.getCenterPointX(), 
					view.getCenterPointY(), view.getXTile(), view.getYTile(), tileRect);
			double topLatitude = MapUtils.getLatitudeFromTile(view.getZoom(), tileRect.top);
			double leftLongitude = MapUtils.getLongitudeFromTile(view.getZoom(), tileRect.left);
			double bottomLatitude = MapUtils.getLatitudeFromTile(view.getZoom(), tileRect.bottom);
			double rightLongitude = MapUtils.getLongitudeFromTile(view.getZoom(), tileRect.right);

			objects.clear();
			resourceManager.searchTransportAsync(topLatitude, leftLongitude, bottomLatitude, rightLongitude, view.getZoom(), objects);
			for (TransportStop o : objects) {
				int x = view.getMapXForPoint(o.getLocation().getLongitude());
				int y = view.getMapYForPoint(o.getLocation().getLatitude());
				canvas.drawCircle(x, y, getRadiusPoi(view.getZoom()), pointAltUI);
			}

		}
	}

	@Override
	public void destroyLayer() {
	}

	@Override
	public boolean drawInScreenPixels() {
		return false;
	}

	@Override
	public boolean onLongPressEvent(PointF point) {
		// TODO open search transport
		return false;
	}

}
