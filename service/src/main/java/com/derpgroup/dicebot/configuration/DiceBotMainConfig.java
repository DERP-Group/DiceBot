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

package com.derpgroup.dicebot.configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;

import com.derpgroup.derpwizard.configuration.AccountLinkingDAOConfig;
import com.derpgroup.derpwizard.configuration.MainConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Top-level configuration class.
 *
 * @author Eric Olson
 * @since 0.0.1
 */
public class DiceBotMainConfig extends MainConfig {
  
  @Valid
  @NotNull
  private DiceBotConfig diceBotConfig;

  @Valid
  @NotNull
  private DiceBotDAOConfig daoConfig;

  @JsonProperty
  public DiceBotConfig getDiceBotConfig() {
    return diceBotConfig;
  }

  @JsonProperty
  public void setDiceBotConfig(DiceBotConfig diceBotConfig) {
    this.diceBotConfig = diceBotConfig;
  }

  @JsonProperty
  public DiceBotDAOConfig getDaoConfig() {
    return daoConfig;
  }

  @JsonProperty
  public void setDaoConfig(DiceBotDAOConfig daoConfig) {
    this.daoConfig = daoConfig;
  }
}
