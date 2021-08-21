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

import React from 'react';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import ListSubheader from '@material-ui/core/ListSubheader';
import NoteAddIcon from '@material-ui/icons/NoteAdd';
import StorageIcon from '@material-ui/icons/Storage';
import WorkIcon from "@material-ui/icons/Work"
import FindInPage from '@material-ui/icons/FindInPage';
import {Link} from "react-router-dom";

export const fmMenuListItems = (
  <div>
    <ListSubheader>File Manager</ListSubheader>

    <ListItem button component={Link} to={"/productIngest"}>
      <ListItemIcon>
        <NoteAddIcon />
      </ListItemIcon>
      <ListItemText primary="Product Ingest" />
    </ListItem>

    <ListItem button component={Link} to={"/products"}>
      <ListItemIcon>
        <StorageIcon />
      </ListItemIcon>
      <ListItemText primary="Product Browser" />
    </ListItem>
  </div>
);

export const wmMenuListItems = (
  <div>
    <ListSubheader>WorkFlow Manager</ListSubheader>

    <ListItem button component={Link} to={"/workflows"}>
      <ListItemIcon>
        <WorkIcon />
      </ListItemIcon>
      <ListItemText primary="Workflow Browser" />
    </ListItem>
  </div>
);