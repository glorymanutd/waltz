package com.khartec.waltz.service.user;

import com.khartec.waltz.data.user.UserPreferenceDao;
import com.khartec.waltz.model.user.UserPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotEmptyString;
import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class UserPreferenceService {

    private static final Logger LOG = LoggerFactory.getLogger(UserPreferenceService.class);

    private final UserPreferenceDao userPreferenceDao;


    @Autowired
    public UserPreferenceService(UserPreferenceDao userPreferenceDao) {
        checkNotNull(userPreferenceDao, "userPreferenceDao cannot be null");

        this.userPreferenceDao = userPreferenceDao;
    }


    public List<UserPreference> getPreferences(String userName) {
        checkNotEmptyString(userName, "userName cannot be empty");
        return userPreferenceDao.getPreferencesForUser(userName);
    }


    public List<UserPreference> savePreferences(String userName, List<UserPreference> preferences) {
        checkNotEmptyString(userName, "userName cannot be empty");
        checkNotNull(preferences, "preferences cannot be null");

        userPreferenceDao.savePreferencesForUser(userName, preferences);
        return getPreferences(userName);
    }


    public List<UserPreference> savePreference(String userName, UserPreference preference) {
        checkNotEmptyString(userName, "userName cannot be empty");
        checkNotNull(preference, "preference cannot be null");

        userPreferenceDao.savePreference(userName, preference);
        return getPreferences(userName);
    }


    public boolean clearPreferences(String userName) {
        checkNotEmptyString(userName, "userName cannot be empty");

        userPreferenceDao.clearPreferencesForUser(userName);
        return true;
    }

}