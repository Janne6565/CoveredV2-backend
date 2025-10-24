package com.janne.coveredv2.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricService {
	private final MeterRegistry meterRegistry;

	public void incrementCounter(String metricName, List<String> tags) {
		meterRegistry.counter(metricName, tags.toArray(String[]::new)).increment();
	}

	public void incrementCounter(String metricName) {
		meterRegistry.counter(metricName).increment();
	}
}
