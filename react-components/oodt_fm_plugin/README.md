# React OODT File Manager Plugin Sample


## Requirements

- [Node.js](https://nodejs.org/en/download/) v6.0.0 or higher
- NPM (v3.0.0+ recommended) (this comes with Node.js)

## Installation

**Using NPM:**

Since this Plugin is not officially published yet please follow below steps for local testing.
1. Install Node Modules for web pack dependencies.
    ```
    npm install
    ```
    
1. In the root of your NPM package, do this to build.
    ```
    npm run build
    ```

2.  Install the Package into local node_modules.
    ```
    npm install . -g
    ```
3. Create a symlink package that points to your working directory.
    ```
    npm link
    ```
    
4. Local NPM Package installation. Use this command in relevant React project directory. This will link global node_module plugin directory to project's local node_modules directory.
    ```
    npm link oodt_fm_plugin_sample
    ```    
## Usage

Here's an example of basic usage:

```
import React, { Component } from 'react';
import { Product } from 'oodt_fm_plugin_sample';

class MyApp extends Component {
  render() {
    return (
      <div>
        <Product productId="ce4380c5-d0d2-11e8-89ca-121c29fc9f21" />
      </div>
    );
  }
}

export default MyApp;
```

#### props

- `productId`: Product ID key (eg.: `ce4380c5-d0d2-11e8-89ca-121c29fc9f21`).

## Contributing

Developers can add New Components (eg: Product, ProductList...) by following the directory structure and export them accordingly in index.js files.