package ru.n08i40k.npluginlocale;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocaleResult {
    private final List<String> lines;

    protected LocaleResult(List<String> formatBuffers, Object... args) {
        lines = new ArrayList<>();

        for (String fmt : formatBuffers) {
            lines.add(MessageFormat.format(fmt, args)
                    .replace("\\n", "\n"));
        }
    }

    public LocaleResult format(Map<String, Object> placeholders) {
        List<String> newLines = new ArrayList<>();

        lines.forEach(line -> {
            String newLine = line;

            for (String key : placeholders.keySet()) {
                newLine = newLine.replace("%" + key + "%", String.valueOf(placeholders.get(key)));
            }

            newLines.add(newLine);
        });

        lines.clear();

        for (String newLine : newLines) {
            lines.add(newLine.replace("\\n", "\n"));
        }

        return this;
    }

    public SingleLocaleResult getSingle() {
        return new SingleLocaleResult(lines.get(0));
    }

    public MultipleLocaleResult getMultiple() {
        return new MultipleLocaleResult(lines);
    }
}
