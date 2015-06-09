/**
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

package org.apache.oodt.cas.workflow.gui.perspective.view;

//OODT imports
import org.apache.oodt.cas.workflow.gui.model.ModelGraph;

/**
 * 
 * Represents a change in the Workflow GUI model view.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public abstract class ViewChange<T> {

  private T object;
  private View source;

  protected ViewChange(T object, View source) {
    this.object = object;
    this.source = source;
  }

  public T getObject() {
    return this.object;
  }

  public View getSource() {
    return this.source;
  }

  public static final class NEW_VIEW extends ViewChange<ModelGraph> {
    public NEW_VIEW(ModelGraph object, View source) {
      super(object, source);
    }
  }

  public static final class DELETE_VIEW extends ViewChange<View> {
    public DELETE_VIEW(View object, View source) {
      super(object, source);
    }
  }

  public static final class REFRESH_VIEW extends ViewChange<View> {
    public REFRESH_VIEW(View object, View source) {
      super(object, source);
    }
  }

  public static final class NEW_STATE extends ViewChange<ViewState> {
    public NEW_STATE(ViewState object, View source) {
      super(object, source);
    }
  }

  public static final class REMOVE_STATE extends ViewChange<ViewState> {
    public REMOVE_STATE(ViewState object, View source) {
      super(object, source);
    }
  }

  public static final class NEW_ACTIVE_STATE extends ViewChange<ViewState> {
    public NEW_ACTIVE_STATE(ViewState object, View source) {
      super(object, source);
    }
  }

  public static final class STATE_NAME_CHANGE extends ViewChange<ViewState> {
    public STATE_NAME_CHANGE(ViewState object, View source) {
      super(object, source);
    }
  }

  public static final class VIEW_MODEL extends ViewChange<String> {
    public VIEW_MODEL(String modelId, View source) {
      super(modelId, source);
    }
  }

}
