package emu.protoshift.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import emu.protoshift.ProtoShift;
import java.util.Arrays;

public class JlineLogbackAppender extends ConsoleAppender<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!started) {
            return;
        }
        Arrays.stream(
                new String(encoder.encode(eventObject)).split("\n\r")
        ).forEach(ProtoShift.getConsole()::printAbove);
    }
}
