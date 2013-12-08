package net.einspunktnull.leightweight.activity;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.einspunktnull.android.greendao.GreenDaoActivity;
import net.einspunktnull.android.greendao.GreenDaoApplication;
import net.einspunktnull.android.greendao.GreenDaoDatabase;
import net.einspunktnull.date.DateUtil;
import net.einspunktnull.leightweight.R;
import net.einspunktnull.leightweight.db.LightweightDatabase;
import net.einspunktnull.leightweight.vo.Points;
import net.einspunktnull.lightweight.greenDao.Entry;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LightweightActivity extends GreenDaoActivity
{

	private static final int PROP_FAT = 0;
	private static final int PROP_CARB = 1;
	private static final int PROP_WATER = 2;
	private static final int PROP_SPORT = 3;

	// VIEW
	private static final int OPTIONSMENU_ITEM_PREFERENCES = 0;
	private static final int OPTIONSMENU_ITEM_INIT_RESET = 1;
	private TextView txtDate;
	private TextView txtFat;
	private TextView txtCarb;
	private TextView txtWater;
	private TextView txtSport;
	private Button btnWeight;
	private Button btnDatePrevious;
	private Button btnDateToday;
	private Button btnDateNext;
	private Button btnFatMinus;
	private Button btnFatPlus;
	private Button btnCarbMinus;
	private Button btnCarbPlus;
	private Button btnWaterMinus;
	private Button btnWaterPlus;
	private Button btnSportMinus;
	private Button btnSportPlus;
	private Button btnWeightMinus;
	private Button btnWeightPlus;

	// LOGIC
	private Date today;
	private Date startDate;
	private LightweightDatabase db;
	private HashMap<Long, Entry> entries;
	private Date currentDate;
	private SimpleDateFormat dateFormat;
	private Entry currentEntry;

	/****************************************************************
	 * GENERAL
	 ****************************************************************/
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lightweight);

		setupDate();
		setupDatabase();
		setupView();
	}

	@SuppressLint("SimpleDateFormat")
	private void setupDate()
	{
		today = DateUtil.floorDay(new Date());
		currentDate = today;
		dateFormat = new SimpleDateFormat(getString(R.string.app_dateFormat));
	}

	private void setupDatabase()
	{
		db = (LightweightDatabase) getDb();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		verifyAndActivate();
	}

	protected void reset()
	{
		db.clear();
		verifyAndActivate();
	}

	private void verifyAndActivate()
	{
		if (verify())
		{
			loggD("alles coool: aktivieren");
			createDbEntriesIfNeeded();
			activate();
		}
		else
		{
			loggD("alles schwul: nicht aktivieren");
			deactivate();
		}

	}

	private void activate()
	{
		entries = db.getEntris();
		setButtonsEnabled(true);
		changeToToday();
	}

	private void changeToPreviousDay()
	{
		Date prevDay = DateUtil.dayBefore(currentDate);
		changeDay(prevDay);
	}

	private void changeToNextDay()
	{
		Date nextDay = DateUtil.dayAfter(currentDate);
		changeDay(nextDay);
	}

	private void changeToToday()
	{
		changeDay(today);
	}

	private void changeDay(Date date)
	{
		Entry entry = getEntry(date);
		if (entry == null) return;

		btnDatePrevious.setEnabled(hasPreviousDay(date));
		btnDateNext.setEnabled(hasNextDay(date));

		currentDate = date;
		currentEntry = entry;

		String currDateTxt = dateFormat.format(currentDate);
		txtDate.setText(currDateTxt);

		updateTextViews();
	}

	private void updateTextViews()
	{
		updateTextFat();
		updateTextCarb();
		updateTextWater();
		updateTextSport();
		updateTextWeight();
	}

	private void updateTextFat()
	{
		long taken = currentEntry.getFat();
		long max = Points.DAILY_FAT_POINTS;
		long left = max - taken;
		long takenTotal = getPropTakenTotal(PROP_FAT);
		long maxTotal = entries.size() * max;
		long leftTotal = maxTotal - takenTotal;
		setStatusText(txtFat, "Fett", taken, max, left, leftTotal);
	}

	private void updateTextCarb()
	{
		long taken = currentEntry.getCarb();
		long max = Points.DAILY_CARB_POINTS;
		long left = max - taken;
		long takenTotal = getPropTakenTotal(PROP_CARB);
		long maxTotal = entries.size() * max;
		long leftTotal = maxTotal - takenTotal;
		setStatusText(txtCarb, "Kohlenhydrate", taken, max, left, leftTotal);
	}

	private void updateTextWater()
	{
		long taken = currentEntry.getWater();
		long max = Points.DAILY_WATER_POINTS;
		long left = max - taken;
		long takenTotal = getPropTakenTotal(PROP_WATER);
		long maxTotal = entries.size() * max;
		long leftTotal = maxTotal - takenTotal;
		setStatusText(txtWater, "Wasser", taken, max, left, leftTotal);
	}

	private void updateTextSport()
	{
		long taken = currentEntry.getSport();
		long max = Points.DAILY_SPORT_POINTS;
		long left = max - taken;
		long takenTotal = getPropTakenTotal(PROP_SPORT);
		long maxTotal = entries.size() * max;
		long leftTotal = maxTotal - takenTotal;
		setStatusText(txtSport, "Sport", taken, max, left, leftTotal);
	}

	@SuppressLint("DefaultLocale")
	private void updateTextWeight()
	{
		float taken = currentEntry.getWeight();
		String takenTxt = String.format("%.1f", taken);
		btnWeight.setText(takenTxt + " kg");
	}

	public long getPropTakenTotal(int prop)
	{
		long sum = 0L;

		for (Map.Entry<Long, Entry> entry : entries.entrySet())
		{
			Entry dbEntry = entry.getValue();
			switch (prop)
			{
				case PROP_FAT:
					sum += dbEntry.getFat();
					break;
				case PROP_CARB:
					sum += dbEntry.getCarb();
					break;
				case PROP_WATER:
					sum += dbEntry.getWater();
					break;
				case PROP_SPORT:
					sum += dbEntry.getSport();
					break;

			}
		}
		return sum;
	}

	private boolean hasNextDay(Date date)
	{
		return getEntry(DateUtil.dayAfter(date)) != null;
	}

	private boolean hasPreviousDay(Date date)
	{
		return getEntry(DateUtil.dayBefore(date)) != null;
	}

	private Entry getEntry(Date date)
	{
		return entries.get(date.getTime());
	}

	private void deactivate()
	{
		setButtonsEnabled(false);
	}

	/****************************************************************
	 * VERIFYING
	 ****************************************************************/

	private boolean verify()
	{
		if (!verifyStartDate()) return false;
		if (!verifyPointPrefs()) return false;
		return true;
	}

	private boolean verifyStartDate()
	{
		// Startdatum gesetzt?
		String prefKeyStartDate = getString(R.string.PREF_KEY_START_DATE);
		String startDateString = preferences.getString(prefKeyStartDate);
		if (startDateString == null || startDateString == "")
		{
			loggD("verifyStartDate()", "no StartDatePref");
			return false;
		}

		// Startdatum valide?
		startDate = DateUtil.string2Date(startDateString, "yyyy.MM.dd");
		if (startDate.after(today))
		{
			loggD("verify()", "Startdatum liegt nicht vor heute");
			return false;
		}

		return true;
	}

	private boolean verifyPointPrefs()
	{
		if (!verifyPointPref("DAILY_FAT_POINTS")) return false;
		if (!verifyPointPref("DAILY_CARB_POINTS")) return false;
		if (!verifyPointPref("DAILY_WATER_POINTS")) return false;
		if (!verifyPointPref("DAILY_SPORT_POINTS")) return false;
		return true;
	}

	private boolean verifyPointPref(String pointPropName)
	{
		try
		{
			Class<Points> ptsCls = Points.class;

			String prfPointsKey = pointPropName;
			String prfPointsVal = preferences.getString(prfPointsKey);
			int pointsVal = Integer.parseInt(prfPointsVal);
			if (pointsVal <= 0) return false;
			Field fldPts = ptsCls.getField(prfPointsKey);
			fldPts.set(null, pointsVal);

			String prfPointsStepsKey = prfPointsKey + "_STEPS";
			String prfPointsStepsVal = preferences.getString(prfPointsStepsKey);
			int pointsStepsVal = Integer.parseInt(prfPointsStepsVal);
			if (pointsStepsVal <= 0 || pointsStepsVal > pointsVal) return false;
			Field fldPtsStps = ptsCls.getField(prfPointsStepsKey);
			fldPtsStps.set(null, pointsStepsVal);
			return true;
		}
		catch (Exception e)
		{
			loggE(e);
			return false;
		}
	}

	/****************************************************************
	 * DB CREATON
	 ****************************************************************/
	private void createDbEntriesIfNeeded()
	{
		HashMap<Long, Entry> currentDbEntries = db.getEntris();
		Date[] allDates = DateUtil.getDaysFromTo(startDate, today);

		ArrayList<Entry> entriesNeedInDb = new ArrayList<Entry>();

		Entry lastEntry = null;

		for (Date date : allDates)
		{

			long dateMillis = date.getTime();

			Entry entry = currentDbEntries.get(dateMillis);

			if (lastEntry != null) loggD(lastEntry.getWeight());

			if (entry == null)
			{
				float weight = lastEntry != null ? lastEntry.getWeight() : 75f;

				entry = new Entry(null, dateMillis, 0, 0, 0, 0, weight);
				entriesNeedInDb.add(entry);
			}

			lastEntry = entry;

		}
		if (entriesNeedInDb.size() > 0)
		{

			loggD("muss was inne db", entriesNeedInDb.size());
			db.addEntries(entriesNeedInDb);
		}
		else
		{
			loggD("muss nüscht nei", entriesNeedInDb.size());
		}

	}

	/****************************************************************
	 * VIEW
	 ****************************************************************/
	private void setupView()
	{

		txtDate = (TextView) findViewById(R.id.txtDate);
		txtFat = (TextView) findViewById(R.id.txtFat);
		txtCarb = (TextView) findViewById(R.id.txtCarb);
		txtWater = (TextView) findViewById(R.id.txtWater);
		txtSport = (TextView) findViewById(R.id.txtSport);
		btnWeight = (Button) findViewById(R.id.btnWeight);
		btnDatePrevious = (Button) findViewById(R.id.btnDatePrevious);
		btnDateToday = (Button) findViewById(R.id.btnDateToday);
		btnDateNext = (Button) findViewById(R.id.btnDateNext);
		btnFatMinus = (Button) findViewById(R.id.btnFatMinus);
		btnFatPlus = (Button) findViewById(R.id.btnFatPlus);
		btnCarbMinus = (Button) findViewById(R.id.btnCarbMinus);
		btnCarbPlus = (Button) findViewById(R.id.btnCarbPlus);
		btnWeightPlus = (Button) findViewById(R.id.btnWeightPlus);
		btnWaterMinus = (Button) findViewById(R.id.btnWaterMinus);
		btnWaterPlus = (Button) findViewById(R.id.btnWaterPlus);
		btnSportMinus = (Button) findViewById(R.id.btnSportMinus);
		btnWeightMinus = (Button) findViewById(R.id.btnWeightMinus);
		btnSportPlus = (Button) findViewById(R.id.btnSportPlus);

		OnLongClickListener longClickListener = new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View v)
			{
				Button btnClicked = (Button) v;

				if (btnClicked.equals(btnWeightMinus))
				{
					float newVal = currentEntry.getWeight() - Points.DAILY_WEIGHT_POINTS_STEPS * 10;
					currentEntry.setWeight(newVal);
					db.update(currentEntry);
					updateTextWeight();
				}
				else if (btnClicked.equals(btnWeightPlus))
				{
					float newVal = currentEntry.getWeight() + Points.DAILY_WEIGHT_POINTS_STEPS * 10;
					currentEntry.setWeight(newVal);
					db.update(currentEntry);
					updateTextWeight();
				}
				return true;
			}
		};

		OnClickListener clickListener = new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Button btnClicked = (Button) v;

				if (btnClicked.equals(btnDatePrevious))
				{
					changeToPreviousDay();
				}
				else if (btnClicked.equals(btnDateToday))
				{
					changeToToday();
				}
				else if (btnClicked.equals(btnDateNext))
				{
					changeToNextDay();
				}
				else if (btnClicked.equals(btnFatMinus))
				{
					int newFat = currentEntry.getFat() - Points.DAILY_FAT_POINTS_STEPS;
					currentEntry.setFat(newFat);
					db.update(currentEntry);
					updateTextFat();
				}
				else if (btnClicked.equals(btnFatPlus))
				{
					int newVal = currentEntry.getFat() + Points.DAILY_FAT_POINTS_STEPS;
					currentEntry.setFat(newVal);
					db.update(currentEntry);
					updateTextFat();
				}
				else if (btnClicked.equals(btnCarbMinus))
				{
					int newVal = currentEntry.getCarb() - Points.DAILY_CARB_POINTS_STEPS;
					currentEntry.setCarb(newVal);
					db.update(currentEntry);
					updateTextCarb();
				}
				else if (btnClicked.equals(btnCarbPlus))
				{
					int newVal = currentEntry.getCarb() + Points.DAILY_CARB_POINTS_STEPS;
					currentEntry.setCarb(newVal);
					db.update(currentEntry);
					updateTextCarb();
				}
				else if (btnClicked.equals(btnWaterMinus))
				{
					int newVal = currentEntry.getWater() - Points.DAILY_WATER_POINTS_STEPS;
					currentEntry.setWater(newVal);
					db.update(currentEntry);
					updateTextWater();
				}
				else if (btnClicked.equals(btnWaterPlus))
				{
					int newVal = currentEntry.getWater() + Points.DAILY_WATER_POINTS_STEPS;
					currentEntry.setWater(newVal);
					db.update(currentEntry);
					updateTextWater();
				}
				else if (btnClicked.equals(btnSportMinus))
				{
					int newVal = currentEntry.getSport() - Points.DAILY_SPORT_POINTS_STEPS;
					currentEntry.setSport(newVal);
					db.update(currentEntry);
					updateTextSport();
				}
				else if (btnClicked.equals(btnSportPlus))
				{
					int newVal = currentEntry.getSport() + Points.DAILY_SPORT_POINTS_STEPS;
					currentEntry.setSport(newVal);
					db.update(currentEntry);
					updateTextSport();
				}
				else if (btnClicked.equals(btnWeightMinus))
				{
					float newVal = currentEntry.getWeight() - Points.DAILY_WEIGHT_POINTS_STEPS;
					currentEntry.setWeight(newVal);
					db.update(currentEntry);
					updateTextWeight();
				}
				else if (btnClicked.equals(btnWeightPlus))
				{
					float newVal = currentEntry.getWeight() + Points.DAILY_WEIGHT_POINTS_STEPS;
					currentEntry.setWeight(newVal);
					db.update(currentEntry);
					updateTextWeight();
				}
				else if (btnClicked.equals(btnWeight))
				{

					TimeSeries series = new TimeSeries("Gewicht in kg");
					List<Entry> currentEntries = db.getEntries();

					Date now = new Date();
					now.setHours(0);
					now.setMinutes(0);
					now.setSeconds(0);

					long xMax = now.getTime();
					long xMin = xMax;
					float yMax = 0;
					float yMin = 1000;

					for (Entry entry : currentEntries)
					{

						long dateMillies = entry.getDate();
						Date date = new Date(dateMillies);
						float weight = entry.getWeight();
						series.add(date, weight);
						if (dateMillies > xMax) xMax = dateMillies;
						if (dateMillies < xMin) xMin = dateMillies;

						if (weight < yMin) yMin = weight;
						if (weight > yMax) yMax = weight;
					}
					xMax += 86400000 / 4;
					xMin -= 86400000 / 4;
					yMax += 3;
					yMin -= 3;

					XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
					renderer.setAxisTitleTextSize(16);
					renderer.setShowGrid(true);
					renderer.setChartTitleTextSize(20);
					renderer.setLabelsTextSize(15);
					renderer.setLegendTextSize(15);
					renderer.setPointSize(5f);
					renderer.setMargins(new int[]
					{ 20, 20, 20, 20 });
					renderer.setRange(new double[]
					{ xMin, xMax, yMin, yMax });
					long gridX = (xMax-xMin)/86400000 +1;
					renderer.setXLabels((int) gridX);

					XYSeriesRenderer xySeriesRenderer = new XYSeriesRenderer();
					xySeriesRenderer.setPointStyle(PointStyle.CIRCLE);
					xySeriesRenderer.setColor(Color.GREEN);
					xySeriesRenderer.setFillPoints(true);
					

					renderer.addSeriesRenderer(xySeriesRenderer);
					renderer.setAxesColor(Color.DKGRAY);
					renderer.setLabelsColor(Color.LTGRAY);

					XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
					dataset.addSeries(series);

					Intent intent = ChartFactory.getTimeChartIntent(LightweightActivity.this, dataset, renderer, "dd.MM.");
					startActivity(intent);
				}
			}
		};

		btnFatMinus.setOnClickListener(clickListener);
		btnFatPlus.setOnClickListener(clickListener);
		btnCarbMinus.setOnClickListener(clickListener);
		btnDateNext.setOnClickListener(clickListener);
		btnCarbPlus.setOnClickListener(clickListener);
		btnDateToday.setOnClickListener(clickListener);
		btnWaterPlus.setOnClickListener(clickListener);
		btnWaterMinus.setOnClickListener(clickListener);
		btnSportMinus.setOnClickListener(clickListener);
		btnSportPlus.setOnClickListener(clickListener);
		btnDatePrevious.setOnClickListener(clickListener);
		btnWeight.setOnClickListener(clickListener);
		btnWeightMinus.setOnClickListener(clickListener);
		btnWeightMinus.setOnLongClickListener(longClickListener);
		btnWeightPlus.setOnClickListener(clickListener);
		btnWeightPlus.setOnLongClickListener(longClickListener);

		setButtonsEnabled(false);
	}

	public void setStatusText(TextView tv, String prop, long taken, long max, long left, long leftTotal)
	{
		String txt = prop + "\n" + taken + "/" + max + "/" + left + "  |  " + leftTotal;
		tv.setText(txt);
	}

	private void setButtonsEnabled(boolean enabled)
	{
		btnDatePrevious.setEnabled(enabled);
		btnDateToday.setEnabled(enabled);
		btnDateNext.setEnabled(enabled);
		btnFatMinus.setEnabled(enabled);
		btnFatPlus.setEnabled(enabled);
		btnCarbMinus.setEnabled(enabled);
		btnCarbPlus.setEnabled(enabled);
		btnWaterMinus.setEnabled(enabled);
		btnWaterPlus.setEnabled(enabled);
		btnSportMinus.setEnabled(enabled);
		btnSportPlus.setEnabled(enabled);
		btnWeight.setEnabled(enabled);
	}

	/****************************************************************
	 * MENU
	 ****************************************************************/
	@Override
	protected GreenDaoDatabase setupGreenDaoDb(GreenDaoApplication applicationContext)
	{
		return new LightweightDatabase(applicationContext);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		String pref = getString(R.string.menuItemPrefs);
		String resInit = getString(R.string.menuItemResetAndInit);
		menu.add(Menu.NONE, OPTIONSMENU_ITEM_PREFERENCES, 0, pref);
		menu.add(Menu.NONE, OPTIONSMENU_ITEM_INIT_RESET, 0, resInit);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		switch (item.getItemId())
		{
			case OPTIONSMENU_ITEM_PREFERENCES:
				return startPreferences();
			case OPTIONSMENU_ITEM_INIT_RESET:
				return openResetInitDialog();
		}
		return false;
	}

	private boolean openResetInitDialog()
	{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch (which)
				{
					case DialogInterface.BUTTON_POSITIVE:
						reset();
						verify();
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String msg = getString(R.string.dlgResetInitMsg);
		String yes = getString(R.string.dlgResetInitYes);
		String no = getString(R.string.dlgResetInitNo);
		builder.setMessage(msg);
		builder.setPositiveButton(yes, dialogClickListener);
		builder.setNegativeButton(no, dialogClickListener);
		builder.show();
		return true;
	}

	private boolean startPreferences()
	{
		Intent intent = new Intent(this, PrefsActivity.class);
		startActivity(intent);
		return true;
	}

}