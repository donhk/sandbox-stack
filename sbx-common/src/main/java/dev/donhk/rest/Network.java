package dev.donhk.rest;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Network(
        NetworkType networkType,
        String networkName
) {}