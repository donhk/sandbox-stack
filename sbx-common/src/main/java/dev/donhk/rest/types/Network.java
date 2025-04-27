package dev.donhk.rest.types;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Network(
        NetworkType networkType,
        String networkName
) {}