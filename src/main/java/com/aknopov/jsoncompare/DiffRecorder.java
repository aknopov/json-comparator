package com.aknopov.jsoncompare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Collects discrepancy messages while walking the trees.
 */
class DiffRecorder
{
    private final List<Pattern> knownDiscrepancies;
    private final List<String> messages = new ArrayList<>();

    DiffRecorder(Collection<String> knownDiscrepancies)
    {
        this.knownDiscrepancies = knownDiscrepancies.stream().map(Pattern::compile).toList();
    }

    /**
     * Adds message to the list
     * @param message discrepancy message
     */
    void addMessage(String message)
    {
        knownDiscrepancies.stream()
                .filter(p -> p.matcher(message).find())
                .findFirst()
                .ifPresentOrElse(p -> {}, () -> messages.add(message));
    }

    /**
     * Checks if message list is empty
     * @return check result
     */
    boolean hasErrors()
    {
        return !messages.isEmpty();
    }

    List<String> getMessages()
    {
        return messages;
    }
}
