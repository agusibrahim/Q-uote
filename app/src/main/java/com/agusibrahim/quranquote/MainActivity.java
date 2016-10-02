package com.agusibrahim.quranquote;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.database.sqlite.SQLiteDatabase;
import java.util.*;
import android.database.Cursor;
import android.graphics.*;
import android.util.*;
import java.io.*;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.*;

public class MainActivity extends Activity 
{
	SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		db=new AssetDatabaseOpenHelper(MainActivity.this, "quran.db").openDatabase();
		final Spinner surat=(Spinner) findViewById(R.id.surat);
		final Spinner ayat=(Spinner) findViewById(R.id.ayat);
		final TextView arab=(TextView) findViewById(R.id.arab);
		final TextView terjemah=(TextView) findViewById(R.id.terjemah);
		final LinearLayout target=(LinearLayout) findViewById(R.id.target);
		arab.setTypeface(Typeface.createFromAsset(getAssets(), "me_quran.ttf"));
		surat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
					int pos=p1.getSelectedItemPosition();
					Cursor cur=db.rawQuery("SELECT ayat FROM quran WHERE surat="+(pos+1), null);
					List<String> list = new ArrayList<String>();
					cur.moveToFirst();
					for(int i=0;i<cur.getCount();i++){
						cur.moveToPosition(i);
						list.add(cur.getString( cur.getColumnIndex("ayat")));
					}
					ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.this,
																				android.R.layout.simple_spinner_item, list);
					dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					ayat.setAdapter(dataAdapter);
				}

				@Override
				public void onNothingSelected(AdapterView<?> p1) {
					// TODO: Implement this method
				}
			});
		ayat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
					int pos=p1.getSelectedItemPosition();
					Cursor cur=db.rawQuery("SELECT text, terjemah FROM quran WHERE surat="+(surat.getSelectedItemPosition()+1)+" AND ayat="+(pos+1), null);
					cur.moveToFirst();
					String txt=cur.getString( cur.getColumnIndex("text"));
					if(pos==0&surat.getSelectedItemPosition()!=0) txt=txt.substring(38);
					arab.setText(txt);
					terjemah.setText("\""+cur.getString( cur.getColumnIndex("terjemah"))+"\"");
					
				}

				@Override
				public void onNothingSelected(AdapterView<?> p1) {
					// TODO: Implement this method
				}
			});
		((Button) findViewById(R.id.savebtn)).setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1) {
					new Simpan().execute(target, ayat, surat);
				}
			});
    }
	// getBitmapFromView take from http://stackoverflow.com/a/9595919
	public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) 
		//has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else 
		//does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
	private class Simpan extends AsyncTask<Object, Void, Boolean> {
		File f;
		@Override
		protected Boolean doInBackground(Object[] p1) {
			LinearLayout target=(LinearLayout) p1[0];
			Spinner ayat=(Spinner) p1[1];
			Spinner surat=(Spinner) p1[2];
			//target.setDrawingCacheEnabled(true);
			
			f = new File(Environment.getExternalStorageDirectory()+"/quran-"+surat.getSelectedItemPosition()+"_"+ayat.getSelectedItemPosition()+".png");
			if(f.exists()) f.delete();
			try {
				FileOutputStream ostream = new FileOutputStream(f);
				getBitmapFromView(target).compress(CompressFormat.PNG, 10, ostream);
				target.setDrawingCacheEnabled(false);
				return true;
			} catch (FileNotFoundException e) {
				return false;
			}                                   
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				Toast.makeText(MainActivity.this, "Tersimpan di "+f.getPath(), Toast.LENGTH_LONG).show();
			}else Toast.makeText(MainActivity.this, "Gagal disimpan", Toast.LENGTH_LONG).show();
			super.onPostExecute(result);
		}
	}
}
