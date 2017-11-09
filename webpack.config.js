var path = require('path');

const srcDir = path.resolve(__dirname, 'src');
const jsDir = path.resolve(srcDir, 'main', 'js');
const buildDir = path.resolve(__dirname, 'build');

module.exports = {
    entry: path.resolve(jsDir, 'App'),
    devtool: 'sourcemaps',
    output: {
        path: __dirname,
        filename: './src/main/resources/static/built/bundle.js'
    },
    module: {
        loaders: [
            {
                test: /\.js$/,
                exclude: /(node_modules)/,
                loader: 'babel-loader',
                query: {presets: ['es2015', 'react'] }
            },
            {
                test: /\.css$/,
                use: [
                    {
                        loader: 'style-loader'
                    },
                    {
                        loader: 'css-loader',
                        options: {
                            modules: true,
                            importLoaders: 1,
                            localIdentName: '[name]__[local]___[hash:base64:5]'
                        }
                    }
                ]
            }
        ]
    }
};