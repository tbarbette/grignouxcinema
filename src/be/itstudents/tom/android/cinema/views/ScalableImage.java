package be.itstudents.tom.android.cinema.views;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class ScalableImage extends View {
	private double maxScaleFactor = 1.5d;
	private double minScaleFactor = 0.1d; 
    private float mPosX;
    private float mPosY;
    
    private float mLastTouchX;
    private float mLastTouchY;
	private Drawable image;
    Paint paint;

    public ScalableImage(Context context) {
        this(context, null, 0);
    }
    
    public ScalableImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ScalableImage(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    	SWIPE_MIN_DISTANCE =(int)(ViewConfiguration.get(context).getScaledTouchSlop() * 5);
    	paint = new Paint();    
    	paint.setDither(true);
    	paint.setAntiAlias(true);
    	paint.setFilterBitmap(true);
    }
    
    public void setImage(Drawable img) throws NullPointerException {    
    	if (img != null) {
    		mScaleFactor = ((double)getWidth() / (double)img.getBounds().width());
    		baseFactor = mScaleFactor;
    		minScaleFactor = baseFactor;
    	}
    	image = img;
    	if (onMove != null)
    		onMove.onMove(mScaleFactor, mPosX, mPosY);
    	invalidate();
    }


    private double mScaleFactor = 1.f;
    private static final int INVALID_POINTER_ID = -1;
	private static float SWIPE_MIN_DISTANCE;
	private int mActivePointerId = INVALID_POINTER_ID;
	private boolean scaling = false;
	private double mScaleOnTouch = 1f;
	private double mDistOnTouch;
	private long lastDownTime = -1;
	private FlipHandler flipHandler;
	private float mDecX;
	private boolean destroyed = false;
	private double baseFactor;
	private boolean movingX;
	private float mLastPosX;

	private PointD mRealPosOnTouch;
	
    public void setFlipHandler(FlipHandler flipHandler) {
		this.flipHandler = flipHandler;
	}

	@Override
    public boolean onTouchEvent(MotionEvent ev) {
       
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
        	 ViewConfiguration.get(getContext());
			if (lastDownTime != -1 && (System.currentTimeMillis() - lastDownTime) < ViewConfiguration.getDoubleTapTimeout()) {
				lastDownTime = -1;
				double newScaleFactor = mScaleFactor * 1.5f;
				if (newScaleFactor < minScaleFactor) newScaleFactor = minScaleFactor;
				if (newScaleFactor > maxScaleFactor) {
					newScaleFactor = baseFactor;
					mPosX = 0;
					mPosY = 0;
				} else {
					mScaleOnTouch = mScaleFactor;
					mRealPosOnTouch = getRealPos(new PointD((-mPosX) + ev.getX(0), (-mPosY) + ev.getY(0)),mScaleOnTouch);
					
	            	PointD absPosOnTouch = mRealPosOnTouch;
	
	            	PointD newPosOntouch = getScaledPos(absPosOnTouch, newScaleFactor);
	
	            	float visibleX = (float)(ev.getX(0));
	            	float visibleY = (float)(ev.getY(0));
	            	mPosX = (float) (-newPosOntouch.x) + visibleX;
	            	mPosY = (float) (-newPosOntouch.y) + visibleY;
				}
            	mScaleFactor = newScaleFactor;
            	
				invalidate();
				break;

             } else {
             	lastDownTime = System.currentTimeMillis();
             }
            final float x = ev.getX();
            final float y = ev.getY();
           
            mLastTouchX = x;
            mLastTouchY = y;
            mActivePointerId = ev.getPointerId(0);
            
            mDecX = 0;
            
           
        	break;
           
        }
        case MotionEvent.ACTION_POINTER_DOWN: {
             if (ev.getPointerCount() >= 2) {
             	if (!scaling) {
             		scaling = true;
             		mScaleOnTouch = mScaleFactor;
             		
             		mDistOnTouch = Math.sqrt(Math.pow(ev.getX(0) - ev.getX(1), 2) + Math.pow(ev.getY(0) - ev.getY(1), 2));
             		PointD p1 = new PointD((-mPosX) + ev.getX(0), (-mPosY) + ev.getY(0));
             		PointD p2 = new PointD((-mPosX) + ev.getX(1), (-mPosY) + ev.getY(1));
             		
             		mRealPosOnTouch = getRealPos(PointD.center(p1,p2),mScaleOnTouch);
             	}
             }
             break;
        
        }
        	
            
        case MotionEvent.ACTION_MOVE: {
        	lastDownTime = -1;
            final int pointerIndex = ev.findPointerIndex(mActivePointerId);
            if (pointerIndex == -1) return false;
            final float x = ev.getX(pointerIndex);
            final float y = ev.getY(pointerIndex);

            
   
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                mPosX += dx;
                mPosY += dy;
                
                
                
               
                
                if (!scaling && !movingX && image.getBounds().width() * mScaleFactor + mPosX < this.getWidth()) {
            		mPosX = (float) (-1 * (image.getBounds().width() * mScaleFactor) + this.getWidth());
            		mDecX -= dx;
                	if (mDecX > SWIPE_MIN_DISTANCE && flipHandler != null) {
            
                		if (flipHandler != null) flipHandler.next();
                		
                	
                	}
                	
            	} else if (!scaling && mPosX > 0) {
                	mPosX = 0;
                	mDecX += dx;

                	if (mDecX > SWIPE_MIN_DISTANCE && flipHandler != null) {
                	
                		if (flipHandler != null) flipHandler.previous();
                		
                		
                	}
                } else 
                	mDecX = 0;
                
                
                if (dy > 2) {
                	mDecX = 0;
                }

               	
               	
                
                if (mPosY >= 0) {
                	mPosY = 0;
                }
                
                if (Math.abs(mPosX - mLastPosX) > 1) 
                	movingX = true;

                invalidate();
            
            
            if (scaling && ev.getPointerCount() >= 2) {

            	double dist = Math.sqrt(Math.pow(ev.getX(0) - ev.getX(1), 2) + Math.pow(ev.getY(0) - ev.getY(1), 2));
            	double newScaleFactor = mScaleOnTouch * (dist / mDistOnTouch);
            	if (newScaleFactor < minScaleFactor) newScaleFactor = minScaleFactor;
            	if (newScaleFactor > maxScaleFactor)
            		newScaleFactor = maxScaleFactor;
            	PointD absPosOnTouch = mRealPosOnTouch;

            	PointD newPosOntouch = getScaledPos(absPosOnTouch, newScaleFactor);

            	PointD p1 = new PointD(ev.getX(0),ev.getY(0));
         		PointD p2 = new PointD(ev.getX(1),ev.getY(1));
         		PointD center = PointD.center(p1, p2);
            	float visibleX = (float)(ev.getX(0));
            	float visibleY = (float)(ev.getY(0));
            	mPosX = (float)((-newPosOntouch.x) + center.x);
            	mPosY = (float)((-newPosOntouch.y) + center.y);
            	mScaleFactor = newScaleFactor;
            	invalidate();
            }
            
            if (image.getBounds().height() * mScaleFactor + mPosY < this.getHeight()) {
          		mPosY = (float) (-1 * (image.getBounds().height() * mScaleFactor) + this.getHeight());
            }    
           	if (image.getBounds().width() * mScaleFactor + mPosX < this.getWidth()) {
          		mPosX = (float) (-1 * (image.getBounds().width() * mScaleFactor) + this.getWidth());
            }  
           	
            mLastPosX = mPosX;
            mLastTouchX = x;
            mLastTouchY = y;

            break;
        }
            
        case MotionEvent.ACTION_UP: {
            mActivePointerId = INVALID_POINTER_ID;
        	scaling = false;
        	movingX = false;
            break;
        }
            
        case MotionEvent.ACTION_CANCEL: {
            mActivePointerId = INVALID_POINTER_ID;
            scaling = false;
            movingX = false;
            break;
        }
        
        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) 
                    >> MotionEvent.ACTION_POINTER_ID_SHIFT;
            final int pointerId = ev.getPointerId(pointerIndex);
            if (pointerId == mActivePointerId) {
                // This was our active pointer going up. Choose a new
                // active pointer and adjust accordingly.
                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                mLastTouchX = ev.getX(newPointerIndex);
                mLastTouchY = ev.getY(newPointerIndex);
                mActivePointerId = ev.getPointerId(newPointerIndex);
            }
            
            if (scaling && ev.getPointerCount() < 2) {
            	scaling = false;
            	
            }
            break;
        }
        }
        if (mScaleFactor > 1.5f) mScaleFactor = 1.5f;
        onMove.onMove(mScaleFactor,mPosX, mPosY);
        return true;
    }

	
    private PointD getScaledPos(PointD p, double scale) {
		PointD d = new PointD(p.x * scale, p.y * scale);
		return d;
	}

	private PointD getRealPos(PointD p, double scale) {
    	PointD d = new PointD();
    	d.x = p.x / scale;
    	d.y = p.y / scale;
		return d;
	}

	@Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.save();
        if (image != null) {
	        canvas.translate(mPosX, mPosY);
	        canvas.scale((float)mScaleFactor, (float)mScaleFactor);
	        image.draw(canvas);
	            
        }
        canvas.restore();
        if (loading) {
        	float dp =  getResources().getDisplayMetrics().density;
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setTextSize(16 * getResources().getDisplayMetrics().density);
			paint.setColor(Color.rgb(200, 30, 70));

			canvas.drawRoundRect(new RectF(4 * dp, this.getHeight() - ((16 + 12) * dp), this.getWidth() - 4 * dp, this.getHeight() - ((4) * dp)), 4 * dp, 4 * dp, paint);
			paint.setColor(Color.WHITE);
        	canvas.drawText("Chargement...", this.getWidth() / 2, this.getHeight() - ((4 + 8) * dp), paint);
        }
        
    }

    public interface FlipHandler {
    	public void previous();
    	public void next();
    }

    public interface OnMove {
    	public void onMove(double mScaleFactor, float sleft, float stop);
    }
    
    private OnMove onMove;
	private boolean loading;
    
	public void destroy() {

		destroyed  = true;
	}

	public boolean isDestroyed() {
		return destroyed;
	}


	public void setOnMove(OnMove onMove) {
		this.onMove = onMove;
	}

	public void setLoading(boolean b) {
		this.loading = b;
		invalidate();
	}

	public OnMove getOnZoom() {
		return onMove;
	}
}