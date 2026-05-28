package com.almoxe.almoxeapi.common;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String title,
        List<String> details
) {}
