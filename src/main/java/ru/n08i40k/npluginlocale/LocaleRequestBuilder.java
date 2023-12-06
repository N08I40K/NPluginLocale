package ru.n08i40k.npluginlocale;

import org.apache.commons.lang.NullArgumentException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LocaleRequestBuilder {
    @Nullable
    private final LocaleRequestBuilder parent;

    @Nullable
    private final String currentLabel;

    public LocaleRequestBuilder(@Nullable LocaleRequestBuilder parent, @Nullable String currentLabel) {
        this.currentLabel = currentLabel;
        this.parent = parent;
    }

    protected String getKey(@Nullable String subLabel) {
        List<String> keyLabels = new ArrayList<>();

        if (parent != null)
            keyLabels.add(parent.getKey(null));

        if (currentLabel != null)
            keyLabels.add(currentLabel);

        if (subLabel != null)
            keyLabels.add(subLabel);

        if (keyLabels.isEmpty())
            throw new NullArgumentException("Parent label, current label and sub-label is null!");

        return String.join(".", keyLabels);
    }

    public LocaleRequestBuilder extend(String subLabel) {
        return new LocaleRequestBuilder(this, subLabel);
    }

    public LocaleResult get(@Nullable String subLabel, Object... args) {
        return Locale.getInstance().get(getKey(subLabel), args);
    }
}
