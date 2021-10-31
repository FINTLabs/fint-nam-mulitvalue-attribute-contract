package si.genlan.nam.idp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

public class Tracer {

    private static Tracer instance;

    private final boolean trace;

    private Tracer(String property) {
        this.trace = Boolean.parseBoolean(property);
    }

    public static Tracer getInstance(String trace) {
        if (instance == null) {
            instance = new Tracer(trace);
        }
        return instance;
    }

    public boolean getTracing() {
        return trace;
    }

    public void trace(String... messages) {
        if (trace) {
            System.out.println(createLogMessage(messages));
        }
    }

    public void traceConfig(Properties properties) {

        properties.keySet().stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .forEach(key ->
                        trace("Init: Found key " + key + " with value " + properties.getProperty(key))
                );

    }

    private String createLogMessage(String... messages) {
        StringBuilder logMessage = new StringBuilder();
        for (String message : messages) {
            logMessage.append(" ").append(message);
        }
        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return "ReadingAttributesQuery @ " + dtf.format(now) + " ----> :" + logMessage.toString();
    }


}
