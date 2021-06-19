const path = require('path');

module.exports = {
    entry: path.join(__dirname, "src", "index.js"),
    output: {
        path: path.resolve(__dirname, 'dist/'),
        publicPath: '',
        filename: 'build.js',
        libraryTarget: 'umd'
    },
    mode: process.env.NODE_ENV || "development",
    resolve: { modules: [path.resolve(__dirname, "src"), "node_modules"] },
    plugins: [],
    module: {
        rules: [{
            test: /\.jsx?$/,
            exclude: /node_modules/,
            use: {
                loader: 'babel-loader?cacheDirectory=true',
            }
        }]
    },
    externals: {
        'react': "commonjs react",
        'react-dom': "commonjs react-dom"
    },
};