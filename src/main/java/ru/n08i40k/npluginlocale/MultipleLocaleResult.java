package ru.n08i40k.npluginlocale;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class MultipleLocaleResult {
    private final List<SingleLocaleResult> resultList;

    public MultipleLocaleResult(List<String> lines) {
        resultList = new ArrayList<>();

        for (String line : lines) {
            resultList.add(new SingleLocaleResult(line));
        }
    }

    public MultipleLocaleResult sendMessage(CommandSender sender) {
        resultList.forEach((result) -> result.sendMessage(sender));
        return this;
    }

    public MultipleLocaleResult sendServerMessage() {
        resultList.forEach(SingleLocaleResult::sendServerMessage);
        return this;
    }

    public MultipleLocaleResult sendToAllServer() {
        resultList.forEach(SingleLocaleResult::sendToAllServer);
        return this;
    }

    public MultipleLocaleResult sendToAdmins() {
        resultList.forEach(SingleLocaleResult::sendToAdmins);
        return this;
    }

    public List<String> get() {
        List<String> lines = new ArrayList<>();

        resultList.forEach((result) -> lines.add(result.get()));

        return lines;
    }

    public List<Component> getC() {
        List<Component> lines = new ArrayList<>();

        resultList.forEach((result) -> lines.add(result.getC()));

        return lines;
    }

    public String getAll() {
        return String.join("\n", get());
    }
}
