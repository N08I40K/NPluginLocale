package ru.n08i40k.npluginlocale;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.NonNull;
import meteordevelopment.orbit.IEventBus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import ru.n08i40k.npluginlocale.event.LocaleReloadEvent;

import java.io.File;
import java.util.*;

public class Locale {
    private static Locale INSTANCE;

    @Getter
    private final Plugin plugin;

    private final Logger logger;
    private final IEventBus eventBus;
    private String lang;

    Map<String, List<String>> translationMap;

    public Locale(@NonNull Plugin plugin, @NonNull IEventBus eventBus, @NonNull String lang) {
        INSTANCE = this;

        translationMap = new HashMap<>();

        this.plugin = plugin;
        this.eventBus = eventBus;
        this.lang = lang;

        logger = plugin.getSLF4JLogger();

        reload(null);
    }

    public static @NotNull Locale getInstance() {
        Preconditions.checkNotNull(INSTANCE);

        return INSTANCE;
    }

    public static boolean isLocaleLoaded() {
        return INSTANCE != null;
    }

    @NotNull
    private File getLocalesDirectory() {
        File directory = new File(plugin.getDataFolder(), "locales/");

        if (!directory.exists())
            Preconditions.checkState(directory.mkdirs());
        else
            Preconditions.checkState(directory.isDirectory());

        return directory;
    }

    @NotNull
    public List<File> getAvailableLocales() {
        File directory = getLocalesDirectory();

        File[] files = directory.listFiles();

        if (files == null)
            return ImmutableList.of();

        List<File> locales = new ArrayList<>();

        for (File file : files) {
            if (file.getName().endsWith(".yml")) {
                locales.add(file);
            }
        }

        return locales;
    }

    @NotNull
    public Set<String> getAvailableLocalesNames() {
        Set<String> filenames = new HashSet<>();

        getAvailableLocales().forEach(file ->
                filenames.add(file.getName().substring(0, file.getName().length() - 4)));

        return filenames;
    }

    public void loadTranslationMap(String currentLocale) {
        // Create locales directory, if not exists

        File directory = getLocalesDirectory();

        // Load locale file

        File localeFile = new File(directory, currentLocale + ".yml");
        Preconditions.checkState(localeFile.exists());

        // Load locale data

        FileConfiguration locale = YamlConfiguration.loadConfiguration(localeFile);

        for (String key : locale.getKeys(true)) {
            if (locale.isString(key))
                translationMap.put(key, List.of(Objects.requireNonNull(locale.getString(key))));
            else if (locale.isList(key))
                translationMap.put(key, locale.getStringList(key));
        }
    }

    public void reload(@Nullable String lang) {
        if (lang != null)
            this.lang = lang;

        eventBus.post(new LocaleReloadEvent.Pre());

        translationMap.clear();
        loadTranslationMap(this.lang);

        eventBus.post(new LocaleReloadEvent.Post());
    }

    public LocaleResult get(String key, Object... args) {
        if (!translationMap.containsKey(key)) {
            logger.warn("Localization file has no translation for {}!", key);
            return new LocaleResult(List.of(key));
        }

        return new LocaleResult(translationMap.get(key), args);
    }
}
