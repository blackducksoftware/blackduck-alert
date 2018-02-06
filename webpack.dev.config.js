const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const srcDir = path.resolve(__dirname, 'src');
const jsDir = path.resolve(srcDir, 'main', 'js');
const buildDir = path.resolve(__dirname, 'build', 'js');

module.exports = {
    entry: path.resolve(jsDir, 'App'),
    devtool: 'sourcemaps',
    output: {
        path: buildDir,
        filename: 'bundle.js'
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /(node_modules)/,
                loader: 'babel-loader',
                query: {
                    presets: ['es2015', 'react']
                }
            }, {
                test: /\.(jpg|png|svg)$/,
                use: [{
                    loader: 'file-loader',
                    options: {
                        name: '[path][name].[ext]'
                    }
                }]
            }, {
                test: /\.css$/,
                exclude: /node_modules/,
                use: [{
                    loader: 'style-loader'
                }, {
                    loader: 'css-loader',
                    options: {
                        modules: true,
                        importLoaders: 1,
                        localIdentName: '[name]__[local]___[hash:base64:5]'
                    }
                }]
            }, {
                test: /\.css$/,
                include: /node_modules/,
                use: [{
                    loader: 'style-loader'
                }, {
                    loader: 'css-loader',
                    options: {
                        modules: true,
                        importLoaders: 1,
                        localIdentName: '[local]'
                    }
                }]
            }, {
                test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                loader: 'url-loader?limit=10000&mimetype=application/font-woff'
            }, {
                test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                loader: 'file-loader'
            }
        ]
    },
    plugins: [new HtmlWebpackPlugin({
        template: 'src/main/resources/templates/index.html'
    })],
    devServer: {
        hot: true,
        publicPath: '/',
        historyApiFallback: {
            index: 'index.html'
        },
        proxy: [{
            context: ['/configuration', '/verify', '/login', '/logout', '/audit', '/hub'],
            target: "http://localhost:8081",
            secure: false,
            bypass: function(req, res, proxyOptions) {
                if (req.headers.accept.indexOf("html") !== -1) {
                    console.log("Skipping proxy for browser request.", req.url);
                    return '/index.html';
                }
            }
        }]
    }
};
