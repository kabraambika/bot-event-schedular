package edu.northeastern.cs5500.starterbot.service;

import edu.northeastern.cs5500.starterbot.ExcludeFromJacocoGeneratedReport;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;

@ExcludeFromJacocoGeneratedReport
public interface OpenTelemetry {
    Span span(String name);

    Span span(String name, SpanKind kind);
}
