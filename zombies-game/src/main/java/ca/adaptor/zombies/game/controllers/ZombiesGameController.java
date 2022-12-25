package ca.adaptor.zombies.game.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ca.adaptor.zombies.game.controllers.ZombiesControllerConstants.PATH_GAMES;

@RestController
@RequestMapping(PATH_GAMES)
public class ZombiesGameController {
}
