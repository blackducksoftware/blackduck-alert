const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const srcDir = path.resolve(__dirname, 'src');
const jsDir = path.resolve(srcDir, 'main', 'js');

const buildDir = path.resolve(__dirname, 'build', 'resources', 'main', 'static');
const imgDir = path.resolve(srcDir, 'main', 'img');

module.exports = {
    resolve: {
        modules: [path.resolve(__dirname, 'src', 'main', 'js'), 'node_modules'],
        extensions: ['.js']
    },
    entry: ['@babel/polyfill', 'whatwg-fetch', path.resolve(jsDir, 'Index')],
    output: {
        path: buildDir,
        filename: 'js/bundle.js',
        publicPath: '/alert/'
    },
    module: {
        rules: [{
            test: /\.m?js/,
            resolve: {
                fullySpecified: false
            }
        }, {
            test: /\.js$/,
            exclude: /(node_modules)/,
            loader: 'babel-loader'
        }, {
            test: /\.(jpg|png|svg)$/,
            exclude: [/(node_modules)/, imgDir],
            loader: 'file-loader',
            options: {
                name: '[path][name].[ext]'
            }
        },
        {
            test: /\.(jpg|png|svg)$/,
            include: imgDir,
            loader: 'file-loader',
            options: {
                name: 'img/[name].[ext]'
            }
        }, {
            test: /\.s[ac]ss$/i,
            use: ['style-loader', 'css-loader', 'sass-loader']
        }, {
            test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
            use: [
                {
                    loader: 'url-loader',
                    options: {
                        limit: 10000,
                        mimetype: 'application/font-woff',
                        name: 'fonts/[name].[ext]'
                    }
                }
            ]
        }, {
            test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
            use: [
                {
                    loader: 'url-loader',
                    options: {
                        limit: 10000,
                        name: 'fonts/[name].[ext]'
                    }
                }
            ]
        }, {
            test: /\.(ttf|eot|svg|otf)(\?v=[0-9]\.[0-9]\.[0-9])?$/i,
            loader: 'file-loader'
        }]
    },
    plugins: [
        new HtmlWebpackPlugin({
            favicon: 'src/main/img/BlackDuckIcon.png',
            template: 'src/main/js/index.html',
            xhtml: true
        })
    ]
};
