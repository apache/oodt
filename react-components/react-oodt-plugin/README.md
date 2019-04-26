# React OODT Plugin

## Requirements

- [Node.js](https://nodejs.org/en/download/) v6.0.0 or higher
- NPM (v3.0.0+ recommended) (this comes with Node.js)

## Installation

**Using NPM:**

```
npm install --save react-oodt-plugin-sample
```

**Using Yarn:**

```
yarn add react-oodt-plugin-sample
```

## Usage

Here's an example of basic usage:

```
import React, { Component } from 'react';
import OODTPluginSample from 'react-oodt-plugin-sample';

class MyApp extends Component {
  render() {
    return (
      <div>
        <OODTPluginSample productId="ce4380c5-d0d2-11e8-89ca-121c29fc9f21" />
      </div>
    );
  }
}

export default MyApp;
```

#### props

- `productId`: Product ID key (eg.: `ce4380c5-d0d2-11e8-89ca-121c29fc9f21`).
- `interval`: Refresh interval in milliseconds (Default: `300000`).
