package net.einspunktnull.leightweight.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.einspunktnull.android.greendao.GreenDaoDatabase;
import net.einspunktnull.lightweight.greenDao.DaoMaster;
import net.einspunktnull.lightweight.greenDao.DaoMaster.DevOpenHelper;
import net.einspunktnull.lightweight.greenDao.DaoSession;
import net.einspunktnull.lightweight.greenDao.Entry;
import net.einspunktnull.lightweight.greenDao.EntryDao;
import android.content.Context;
import de.greenrobot.dao.QueryBuilder;

public class LightweightDatabase extends GreenDaoDatabase
{

	private EntryDao entryDao;

	public LightweightDatabase(Context applicatonContext)
	{
		super(applicatonContext);
	}

	@Override
	protected void create(Context applicatonContext)
	{
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(applicatonContext, "leightweigth-db", null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		entryDao = ((DaoSession) daoSession).getEntryDao();
		addDao(entryDao);
	}

	public List<Entry> getEntries()
	{
		QueryBuilder<Entry> qb = entryDao.queryBuilder();
		return qb.list();
	}

	public HashMap<Long, Entry> getEntris()
	{
		List<Entry> entries = getEntries();
		HashMap<Long, Entry> dbEntries = new HashMap<Long, Entry>();
		for (Entry entry : entries)
		{
			dbEntries.put(entry.getDate(), entry);
		}
		return dbEntries;
	}

	public void addEntries(ArrayList<Entry> entries)
	{
		entryDao.insertInTx(entries);
	}

	public void clear()
	{
		entryDao.deleteAll();
	}

	public void update(Entry entry)
	{
		entryDao.update(entry);
	}

}
