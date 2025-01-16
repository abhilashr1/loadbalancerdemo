package com.LoadBalancerDemo.Application.Controllers;

import com.LoadBalancerDemo.Application.Models.ViewModels.GamePointRequest;
import com.LoadBalancerDemo.Application.Models.ViewModels.GamePointResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PointsController {

    @PostMapping(value = "/game/points")
    public GamePointResponse getGamePoints(@RequestBody GamePointRequest request) {
        GamePointResponse gp = new GamePointResponse();
        gp.Game = request.Game;
        gp.GamerId = request.GamerId;
        gp.Points = 100;
        return gp;
    }
}