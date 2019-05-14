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

import React, { Component } from 'react';
import fetch from 'isomorphic-unfetch';
import { number, object, string } from 'yup';
import JsonTable from 'ts-react-json-table';

const schema = object().shape({
  productId: string().required(),
  interval: number()
});

export default class OODTSample extends Component {
  static defaultProps = {
    interval: 1000 * 60 * 5
  };

  state = {
    product: {},
    error: false,
    loading: true
  };

  componentDidMount() {
    schema
      .validate(this.props)
      .then(() => this.fetchData())
      .catch(error => {
        console.error(`${error.name} @ ${this.constructor.name}`, error.errors);
        this.setState({ error: true, loading: false });
      });
  }

  componentWillUnmount() {
    clearTimeout(this.timeout);
  }

  async fetchData() {
    const { productId, interval } = this.props;

    try {
      const res = await fetch(
        // `http://46.4.26.22:8012/fmprod/jaxrs/product?productId=${productId}`
        'http://localhost:9999/oodt' // fixed the cors
      );
      const json = await res.json();

      this.setState({
        product: json.product,
        error: false,
        loading: false
      });
    } catch (error) {
      this.setState({ error: true, loading: false });
    } finally {
      this.timeout = setTimeout(() => this.fetchData(), interval);
    }
  }

  render() {
    const { product, error, loading } = this.state;

    return loading ? (
      <div>Loading...</div>
    ) : error ? (
      <div>Error :(</div>
    ) : (
      <JsonTable rows={[product]} />
    );
  }
}
