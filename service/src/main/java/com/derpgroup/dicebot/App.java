/**
 * Copyright (C) 2015 David Phillips
 * Copyright (C) 2015 Eric Olson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.derpgroup.dicebot;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;

import com.derpgroup.derpwizard.configuration.AccountLinkingDAOConfig;
import com.derpgroup.derpwizard.dao.AccountLinkingDAO;
import com.derpgroup.derpwizard.dao.impl.AccountLinkingDAOFactory;
import com.derpgroup.dicebot.configuration.DiceBotConfig;
import com.derpgroup.dicebot.configuration.DiceBotMainConfig;
import com.derpgroup.dicebot.health.BasicHealthCheck;
import com.derpgroup.dicebot.manager.DiceBotManager;
import com.derpgroup.dicebot.resource.DiceBotAlexaResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Main method for spinning up the HTTP server.
 *
 * @author Eric Olson
 * @since 0.0.1
 */
public class App extends Application<DiceBotMainConfig> {

  public static void main(String[] args) throws Exception {
    new App().run(args);
  }

  @Override
  public void initialize(Bootstrap<DiceBotMainConfig> bootstrap) {
  }

  @Override
  public void run(DiceBotMainConfig config, Environment environment) throws IOException {
    if (config.isPrettyPrint()) {
      ObjectMapper mapper = environment.getObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // Health checks
    environment.healthChecks().register("basics", new BasicHealthCheck(config, environment));
    
    DiceBotConfig diceBotConfig = config.getDiceBotConfig();
    DiceBotManager manager = new DiceBotManager(diceBotConfig);
    
    AccountLinkingDAOConfig accountLinkingDAOConfig = config.getDaoConfig().getAccountLinking();
    
    // DAO
    AccountLinkingDAO accountLinkingDAO = AccountLinkingDAOFactory.getDAO(accountLinkingDAOConfig);
    // Resources
    environment.jersey().register(new DiceBotAlexaResource(config, environment, manager, accountLinkingDAO));
  }
}
