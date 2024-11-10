package com.sudoku;

import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class LanguageManager {
    private static final String BUNDLE_NAME = "i18n/messages";
    private static final String PREF_LANGUAGE = "language";
    private static LanguageManager instance;
    private ResourceBundle bundle;
    private Locale currentLocale;
    private final Preferences prefs;

    public enum Language {
        ENGLISH("en"),
        RUSSIAN("ru");

        private final String code;

        Language(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static Language fromCode(String code) {
            for (Language lang : values()) {
                if (lang.code.equals(code)) {
                    return lang;
                }
            }
            return ENGLISH; // Default language
        }
    }

    private LanguageManager() {
        prefs = Preferences.userNodeForPackage(LanguageManager.class);
        String savedLanguage = prefs.get(PREF_LANGUAGE, Language.RUSSIAN.getCode());
        setLanguage(Language.fromCode(savedLanguage));
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    public void setLanguage(Language language) {
        try {
            System.out.println("Setting language to: " + language);
            currentLocale = new Locale(language.getCode());
            System.out.println("Created locale: " + currentLocale);

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale, classLoader);
            System.out.println("Bundle loaded: " + bundle);

            if (bundle != null) {
                System.out.println("Available keys: " + Collections.list(bundle.getKeys()));
            }

            prefs.put(PREF_LANGUAGE, language.getCode());
        } catch (Exception e) {
            System.err.println("Error in setLanguage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Language getCurrentLanguage() {
        return Language.fromCode(currentLocale.getLanguage());
    }

    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            System.err.println("Missing resource key: " + key);
            return key;
        }
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }
}