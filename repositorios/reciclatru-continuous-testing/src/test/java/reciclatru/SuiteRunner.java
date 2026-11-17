package reciclatru;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ejecuta los features en paralelo.
 *
 * <pre>
 * mvn test                                   regresión completa (sin rate limit)
 * mvn test -Dsuite=smoke                     solo lo marcado @smoke
 * mvn test -Dsuite=ratelimit                 solo el límite de solicitudes (API con RATE_LIMIT_ENABLED=true)
 * mvn test -Dstorage=false                   omite lo que necesita el bucket de fotos
 * mvn test -DbaseUrl=https://mi-api/api/v1 -Dkarate.env=test -Dthreads=4
 * </pre>
 */
class SuiteRunner {

    @Test
    void suite() {
        String suite = System.getProperty("suite", "regression");
        boolean storage = Boolean.parseBoolean(System.getProperty("storage", "true"));
        // El límite de solicitudes cuenta por IP: en paralelo los resultados serían impredecibles.
        int threads = Integer.parseInt(System.getProperty("threads", suite.equals("ratelimit") ? "1" : "4"));

        List<String> tags = new ArrayList<>();
        tags.add("~@ignore");
        tags.add("~@known-issue"); // hallazgos documentados contra el contrato, aún sin corregir en la API
        switch (suite) {
            case "smoke" -> {
                tags.add("@smoke");
                tags.add("~@ratelimit");
            }
            case "ratelimit" -> tags.add("@ratelimit");
            default -> tags.add("~@ratelimit"); // regression
        }
        if (!storage) {
            tags.add("~@storage");
        }

        Results results = Runner.path("classpath:reciclatru")
                .tags(tags)
                .outputCucumberJson(true)
                .outputJunitXml(true)
                .parallel(threads);

        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}
