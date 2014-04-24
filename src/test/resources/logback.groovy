import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender


appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "| %-5level| %d{HH:mm:ss.SSS} | %logger{36} - %msg%n"
    }
}

logger("org.springframework", INFO)
logger("org.oasis.spring", DEBUG)

root(WARN, ["CONSOLE"])
