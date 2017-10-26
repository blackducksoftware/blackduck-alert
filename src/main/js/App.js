'use strict';

const React = require('react');
const ReactDOM = require('react-dom');

class App extends React.Component {
    render() {
        return (
            <h1>Hello World!!!</h1>
        )
    }
}

ReactDOM.render(
    <App />, document.getElementById('react')
);