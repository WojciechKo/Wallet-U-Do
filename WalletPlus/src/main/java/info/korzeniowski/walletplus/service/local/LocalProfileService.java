package info.korzeniowski.walletplus.service.local;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.service.ProfileService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;

public class LocalProfileService implements ProfileService {
    private final Dao<Profile, Long> profileDao;
    private final WeakReference<Context> context;

    @Inject
    public LocalProfileService(Context context, Dao<Profile, Long> profileDao) {
        this.context = new WeakReference(context);
        this.profileDao = profileDao;
    }

    @Override
    public Long insert(Profile entity) {
        try {
            entity.setDatabaseFilePath(getFilePath(entity));
            profileDao.create(entity);
            return entity.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private String getFilePath(Profile entity) {
        return context.get().getApplicationInfo().dataDir
                + "/databases/"
                + entity.getName()
                + ".db";
    }

    @Override
    public Long count() {
        try {
            return profileDao.countOf();
        }catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Profile findById(Long id) {
        try {
            return profileDao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Profile findByName(String name) {
        try {
            return profileDao.queryBuilder().where().eq("name", name).queryForFirst();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<Profile> getAll() {
        try {
            return profileDao.queryBuilder().orderByRaw("name COLLATE NOCASE").query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void update(Profile newValue) {
        try {
            Profile original = profileDao.queryForId(newValue.getId());
            newValue.setDatabaseFilePath(getFilePath(newValue));
            profileDao.update(newValue);
            if (!original.getName().equals(newValue.getName())) {
                File database = new File(original.getDatabaseFilePath());
                database.renameTo(new File(getFilePath(newValue)));
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            Profile profile = profileDao.queryForId(id);
            deleteDatabaseFile(profile.getDatabaseFilePath());
            profileDao.deleteById(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void deleteDatabaseFile(String databaseFileName) {
        throw new RuntimeException("Not implemented!");
    }
}