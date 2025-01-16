package com.LoadBalancerDemo.Application.Models.ViewModels;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GamePointRequest {

    @JsonProperty("game")
    public String Game;

    @JsonProperty("gamerId")
    public String GamerId;
}
