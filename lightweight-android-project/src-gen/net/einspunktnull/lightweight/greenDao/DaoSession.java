package net.einspunktnull.lightweight.greenDao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.DaoConfig;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.IdentityScopeType;

import net.einspunktnull.lightweight.greenDao.Entry;

import net.einspunktnull.lightweight.greenDao.EntryDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig entryDaoConfig;

    private final EntryDao entryDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        entryDaoConfig = daoConfigMap.get(EntryDao.class).clone();
        entryDaoConfig.initIdentityScope(type);

        entryDao = new EntryDao(entryDaoConfig, this);

        registerDao(Entry.class, entryDao);
    }
    
    public void clear() {
        entryDaoConfig.getIdentityScope().clear();
    }

    public EntryDao getEntryDao() {
        return entryDao;
    }

}
