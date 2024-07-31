package au.com.mineauz.minigames;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

class StartUpLogHandler extends Handler {
    private final @NotNull StringBuilder builder = new StringBuilder();
    private final @NotNull StringBuilder exceptionBuilder = new StringBuilder();

    protected @NotNull String getExceptionLog() {
        return exceptionBuilder.toString();
    }

    protected @NotNull String getNormalLog() {
        return builder.toString();
    }

    @Override
    public void publish(@NotNull LogRecord record) {
        builder.append('[').append(record.getLevel().getName()).append("] ").append(record.getMessage()).append('\n');
        if (record.getThrown() != null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            record.getThrown().printStackTrace(printWriter);
            exceptionBuilder.append('[').append(record.getLevel().getName()).append("] ").append(record.getMessage()).append('\n')
                    .append(stringWriter).append('\n');
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
