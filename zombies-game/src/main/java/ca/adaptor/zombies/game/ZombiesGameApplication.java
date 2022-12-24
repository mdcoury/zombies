package ca.adaptor.zombies.game;

import ca.adaptor.zombies.game.repositories.ZombiesTileRepository;
import ca.adaptor.zombies.game.util.ZombiesTileImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
})
@EnableJpaRepositories(basePackages = "ca.adaptor.zombies.game.repositories")
public class ZombiesGameApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesGameApplication.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(ZombiesGameApplication.class, args);
	}

}
