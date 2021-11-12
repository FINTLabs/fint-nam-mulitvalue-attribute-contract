package si.genlan.nam.idp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

public class Tracer {

    private static Tracer instance;
    private String applicationName;
    private final boolean trace;

    private Tracer(String property, String applicationName) {
        this.trace = Boolean.parseBoolean(property);
        this.applicationName = applicationName;
    }

    public static Tracer getInstance(String trace, String applicationName) {
        if (instance == null || !instance.equalName(applicationName)) {
            instance = new Tracer(trace, applicationName);
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
    public boolean equalName(String appName)
    {
        return appName.equals(applicationName);
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
        return applicationName + " @ " + dtf.format(now) + " ----> :" + logMessage.toString();
    }


}
