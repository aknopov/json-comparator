package com.aknopov.jsoncompare;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiffRecorderTest
{
    @Test
    void testKnownMessagesFiltering()
    {
        String recorderMessage = "Plain message";
        String rejectPattern = "commonData\\(\\d+\\)\\/dTim(Creation|LastChange)\\(\\d+\\)";
        String rejectedMessage1 = "commonData(11)/dTimCreation(1)";
        String rejectedMessage2 = "commonData(3)/dTimLastChange(2)";

        DiffRecorder diffRecorder = new DiffRecorder(List.of());
        diffRecorder.addMessage(recorderMessage);
        diffRecorder.addMessage(rejectedMessage1);
        diffRecorder.addMessage(rejectedMessage2);
        assertEquals(List.of(recorderMessage, rejectedMessage1, rejectedMessage2), diffRecorder.getMessages());

        diffRecorder = new DiffRecorder(List.of(rejectPattern));
        diffRecorder.addMessage(recorderMessage);
        diffRecorder.addMessage(rejectedMessage1);
        diffRecorder.addMessage(rejectedMessage2);
        assertEquals(List.of(recorderMessage), diffRecorder.getMessages());
    }

    @Test
    void testRegEx()
    {
        DiffRecorder diffRecorder = new DiffRecorder(List.of("^footer.*$"));

        diffRecorder.addMessage("header");
        diffRecorder.addMessage("body");
        diffRecorder.addMessage("footer");
        diffRecorder.addMessage(" footer2");
        assertEquals(List.of("header", "body", " footer2"), diffRecorder.getMessages());
    }
}