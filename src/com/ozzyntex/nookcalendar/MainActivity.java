package com.ozzyntex.nookcalendar;

import com.ozzyntex.nookcalendar.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
 {
	
	TableLayout.LayoutParams headerParams_;
	TableLayout.LayoutParams rowParams_;
	TableRow.LayoutParams cellParams_;
	TableLayout table_;
	
	Calendar calStart_;
	int numOfWeeksToShow_;
	int firstDayOfWeek_;
	
	GestureDetectorCompat gestureDetector_;
	
	public MainActivity() {
		headerParams_ = new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, 0);
		rowParams_ = new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, 0);
		rowParams_.weight = 1;
		cellParams_= new TableRow.LayoutParams(0, LayoutParams.FILL_PARENT);
		cellParams_.weight = 1;
		numOfWeeksToShow_ = 4;
		firstDayOfWeek_ = Calendar.MONDAY;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);
		calStart_ = Calendar.getInstance();
		table_ = (TableLayout)findViewById(R.id.table);
		table_.setOnTouchListener(new OnSwipeTouchListener(this) {
		    public void onSwipeTop() {
		        calStart_.add(Calendar.WEEK_OF_YEAR, 1);
		        showCalendar();
		    }
		    public void onSwipeBottom() {
		        calStart_.add(Calendar.WEEK_OF_YEAR, -1);
		        showCalendar();
		    }
		    public void onSingleTap() {
		    	refresh();
		    }
		    public void onDoubleTap(float x, float y) {
		    	showDateFromPosition(x,y);
		    }
		});

		showCalendar();
		saveScreenshot("calendar");
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		showCalendar();
		saveScreenshot("calendar");
	}
	
    // Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu, menu);
        return true;
    }
    
    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
         
        switch (item.getItemId())
        {
        case R.id.menu_sync:
        	refresh();
            return true;
        case R.id.menu_exit:
            this.finish();
            return true;
 
        default:
            return super.onOptionsItemSelected(item);
        }
    }   
    
    protected void refresh() {
        Toast.makeText(MainActivity.this, "Refresh", Toast.LENGTH_SHORT).show();
        showCalendar();
    }
    
    protected void showDateFromPosition(float x, float y) {
    	Calendar date = (Calendar)calStart_.clone();
    	date.set(Calendar.DAY_OF_WEEK, firstDayOfWeek_);
    	for(Integer i = 0; i < table_.getChildCount(); i++) {
    	    TableRow week = (TableRow)table_.getChildAt(i);
    	    int location[] = new int[2];
    	    week.getLocationOnScreen(location);
    	    int weekY = location[1];
    	    if(y > weekY && y < (weekY + week.getHeight())) {
    	    	for(Integer d = 0; d < week.getChildCount(); ++d) {
    	    		View day = week.getChildAt(d);
    	    		day.getLocationOnScreen(location);
    	    		int dayX = location[0];
    	    		if(x > dayX && x < (dayX + day.getWidth())) {
    	    			date.add(Calendar.DAY_OF_YEAR, (i-1)*7+d);
    	    			String text = DateFormat.format("MMM dd", date.getTime()).toString();
    	    			Vector<String> events =  new Vector<String>();
    	    			getEventsForDate(date, events, "\t");
    	    			for(int event = 0; event < events.size(); ++event) {
    	    				text = text + "\n" + events.get(event);
    	    			}
    	    			Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
    	    			return;
    	    		}
    	    	}
    	    	
    	    }
    	}
    	
    }
    
	protected Bitmap loadBitmapFromView(View v) {
	    DisplayMetrics dm = this.getResources().getDisplayMetrics(); 
	    v.measure(MeasureSpec.makeMeasureSpec(dm.widthPixels, MeasureSpec.EXACTLY),
	            MeasureSpec.makeMeasureSpec(dm.heightPixels, MeasureSpec.EXACTLY));
	    v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
	    Bitmap returnedBitmap = Bitmap.createBitmap(v.getMeasuredWidth(),
	            v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
	    Canvas c = new Canvas(returnedBitmap);
	    v.draw(c);
	    return returnedBitmap;
	}
	
	protected void saveScreenshot(String file) {
		// get Bitmap from the view
		Bitmap bitmap = loadBitmapFromView(table_); 
		// save it in /media/screensavers/calendar
	    File imageFile = new File(File.separator + "media" + File.separator + "screensavers" + File.separator + "calendar", file+".png");
	    OutputStream fout = null;
	    try {
	        fout = new FileOutputStream(imageFile);
	        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fout);
	        fout.flush();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	    	try {
	    		fout.close();
	    	} catch(Exception e) {
	    		e.printStackTrace();
	    	}
	    }

	}
	
	protected void getEventsForDate(Calendar cal, Vector<String> events, String separator) {
		ContentResolver contentResolver = this.getContentResolver();
		Calendar startOfDay = Calendar.getInstance();
		// set the start of day to all zero values (this ensures the milliseconds are correctly set to zero
		startOfDay.clear();
		// set the start of day to be date + 00:00:00.000
		startOfDay.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
		// copy the start of day to be end of day
		Calendar endOfDay = (Calendar)startOfDay.clone();
		// add 1 day to the end of day
		endOfDay.add(Calendar.DAY_OF_YEAR, 1);
		
		Cursor cursor = contentResolver.query(Uri.parse("content://calendar/instances/when/"+startOfDay.getTimeInMillis()+"/"+endOfDay.getTimeInMillis())
				,(new String[] { "_id", "title", "begin", "end", "allDay" })
				,  "(allDay == 1 and begin <= " + startOfDay.getTimeInMillis() + " and end >= " + endOfDay.getTimeInMillis() + ")"
				 + " or (allDay != 1 )" 
				, null, "begin ASC, end-begin DESC");
		
		while(cursor.moveToNext()) {
			String title  = cursor.getString(cursor.getColumnIndex("title")).replace("Bank Holiday", "BH");
			Date start = new Date(cursor.getLong(cursor.getColumnIndex("begin")));
			Long allDay = cursor.getLong(cursor.getColumnIndex("allDay"));	
			if(allDay == 0) {
				events.add(DateFormat.format("kk:mm", start) +separator + title);
			} else {
				events.add(title);
			}
			
		}
	}
		
	protected void showCalendar() {

		Calendar today = Calendar.getInstance();
		int thisDay = today.get(Calendar.DATE);
		int thisMonth = today.get(Calendar.MONTH);
		
		Calendar date = (Calendar)calStart_.clone();
		date.setFirstDayOfWeek(firstDayOfWeek_);
		date.set(Calendar.DAY_OF_WEEK, firstDayOfWeek_);
		
		table_.removeAllViews();	
		
		// add days of week as the top row
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] weekdays = dfs.getWeekdays();
		TableRow weekdayHeader = new TableRow(this);
		headerParams_.height = 20;
		weekdayHeader.setLayoutParams(headerParams_);
		int dayOfWeek = firstDayOfWeek_;
		for(int d = 0; d < 7; ++d) {
			TextView tv = new TextView(this);
			tv.setLayoutParams(cellParams_);
			tv.setBackgroundResource(R.drawable.cell_border);
			tv.setTextSize((float)18);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			SpannableString spanString = new SpannableString(weekdays[dayOfWeek]);
			spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
			tv.setText(spanString);
			weekdayHeader.addView(tv);
			++dayOfWeek;
			if(dayOfWeek > Calendar.SATURDAY) {
				dayOfWeek = Calendar.SUNDAY;
			}
		}
		table_.addView(weekdayHeader);
		
		rowParams_.height = table_.getHeight()/numOfWeeksToShow_;
		for(int w = 0; w < numOfWeeksToShow_; ++w) {
			// add a row for each week
			TableRow week = new TableRow(this);
			week.setLayoutParams(rowParams_);
			for(int d = 0; d < 7; ++d) {
				// events for each day are displayed in a vertical list 
				LinearLayout day = new LinearLayout(this);
				day.setOrientation(LinearLayout.VERTICAL);
				day.setLayoutParams(cellParams_);
				day.setBackgroundResource(R.drawable.cell_border);
				
				Vector<String> events = new Vector<String>();
				// add the date as the first event which becomes the header
				events.add(DateFormat.format("MMM dd", date.getTime()).toString());
				// get the rest of the events for this date
				getEventsForDate(date, events, "\n");
				
				for(int event = 0; event < events.size(); ++event) {
					TextView tv = new TextView(this);
					tv.setTextSize((float)18);
					if(event == 0) { // first event is the header
						// set the colours for the header depending on whether it's today, a weekday or weekend
						if(thisDay == date.get(Calendar.DATE) && thisMonth == date.get(Calendar.MONTH)) {
							// if the day being displayed is today then it has a header with white bg and black text
							tv.setBackgroundResource(R.drawable.cell_border);
							tv.setTextColor(Color.BLACK);
						} else if(d < 5) { 
							// weekday headers have a black bg and white text
							tv.setBackgroundColor(Color.BLACK);
							tv.setTextColor(Color.WHITE);
						} else { 
							// weekend headers have a dark grey bg and white text
							tv.setBackgroundColor(Color.DKGRAY);
							tv.setTextColor(Color.WHITE);
						}
						// fill out the text of the header using a spannable string so it can be made bold
						tv.setGravity(Gravity.CENTER_HORIZONTAL);
						SpannableString spanString = new SpannableString(events.get(event));
						spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
						tv.setText(spanString);
					} else { // all the other events
						tv.setBackgroundResource(R.drawable.cell_border);
						tv.setPadding(2, 0, 2, 0);
						tv.setText(events.get(event));
					}
					day.addView(tv);
				}
				// add the day to the week
				week.addView(day);
				// move on to the next day
				date.add(Calendar.DAY_OF_YEAR, 1);
			}
			// add the week
			table_.addView(week);
		}
        
	}
}
