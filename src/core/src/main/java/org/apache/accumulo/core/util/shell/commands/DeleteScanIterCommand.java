/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.accumulo.core.util.shell.commands;

import java.util.Iterator;
import java.util.List;

import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.util.shell.Shell;
import org.apache.accumulo.core.util.shell.Shell.Command;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

public class DeleteScanIterCommand extends Command {
  private Option tableOpt, nameOpt, allOpt;
  
  @Override
  public int execute(String fullCommand, CommandLine cl, Shell shellState) throws Exception {
    String tableName;
    
    if (cl.hasOption(tableOpt.getOpt())) {
      tableName = cl.getOptionValue(tableOpt.getOpt());
      if (!shellState.getConnector().tableOperations().exists(tableName))
        throw new TableNotFoundException(null, tableName, null);
    }
    
    else {
      shellState.checkTableState();
      tableName = shellState.getTableName();
    }
    
    if (cl.hasOption(allOpt.getOpt())) {
      List<IteratorSetting> tableScanIterators = shellState.scanIteratorOptions.remove(tableName);
      if (tableScanIterators == null)
        Shell.log.info("No scan iterators set on table " + tableName);
      else
        Shell.log.info("Removed the following scan iterators from table " + tableName + ":" + tableScanIterators);
    } else if (cl.hasOption(nameOpt.getOpt())) {
      String name = cl.getOptionValue(nameOpt.getOpt());
      List<IteratorSetting> tableScanIterators = shellState.scanIteratorOptions.get(tableName);
      if (tableScanIterators != null) {
        boolean found = false;
        for (Iterator<IteratorSetting> iter = tableScanIterators.iterator(); iter.hasNext();) {
          if (iter.next().getName().equals(name)) {
            iter.remove();
            found = true;
            break;
          }
        }
        if (!found)
          Shell.log.info("No iterator named " + name + " found for table " + tableName);
        else {
          Shell.log.info("Removed scan iterator " + name + " from table " + tableName + " (" + shellState.scanIteratorOptions.get(tableName).size() + " left)");
          if (shellState.scanIteratorOptions.get(tableName).size() == 0)
            shellState.scanIteratorOptions.remove(tableName);
        }
      } else {
        Shell.log.info("No iterator named " + name + " found for table " + tableName);
      }
    }
    
    return 0;
  }
  
  @Override
  public String description() {
    return "deletes a table-specific scan iterator so it is no longer used during this shell session";
  }
  
  @Override
  public Options getOptions() {
    Options o = new Options();
    
    tableOpt = new Option(Shell.tableOption, "table", true, "tableName");
    tableOpt.setArgName("table");
    
    OptionGroup nameGroup = new OptionGroup();
    
    nameOpt = new Option("n", "name", true, "iterator to delete");
    nameOpt.setArgName("itername");
    
    allOpt = new Option("a", "all", false, "delete all scan iterators");
    allOpt.setArgName("all");
    
    nameGroup.addOption(nameOpt);
    nameGroup.addOption(allOpt);
    nameGroup.setRequired(true);
    o.addOption(tableOpt);
    o.addOptionGroup(nameGroup);
    
    return o;
  }
  
  @Override
  public int numArgs() {
    return 0;
  }
}
