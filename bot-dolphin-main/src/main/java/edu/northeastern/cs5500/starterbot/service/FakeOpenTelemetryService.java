package edu.northeastern.cs5500.starterbot.service;

import edu.northeastern.cs5500.starterbot.ExcludeFromJacocoGeneratedReport;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;

@ExcludeFromJacocoGeneratedReport
public class FakeOpenTelemetryService implements OpenTelemetry {

    @Override
    public Span span(String name) {
        return Span.current();
    }

    @Override
    public Span span(String name, SpanKind kind) {
        return Span.current();
    }
}
