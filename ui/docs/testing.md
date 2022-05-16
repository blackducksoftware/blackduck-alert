# Testing Overview

`blackduck-alert` uses [Jest](https://jestjs.io/) and [Enzyme](https://enzymejs.github.io/enzyme/) to test it's components.  At the time of writing this, both of these libraries work seemlessly within our app to provide accurate testing coverage mocking calls, performing user functions, and ensuring changes within our UI repo don't break between builds.  I will attempt to provide a testing best practices, with examples, in this document as I make my way through the code writing these tests.  


## Setting up a Test

It's important to follow a similar pattern when writing tests.  It helps the readability for our devs as well as contributing to a smooth flowing debug process.  
  
### Filename Pattern  
A test's filename should directly correspond to the component's name it is being created for. For that reason, if we're adding a test for `MyComponent.jsx` then the test name should be: `MyComponent.test.jsx`

Furthermore, that test file should be placed within a  `__tests__` directory, which should be found at the same level as the jsx component we're creating a test for.  Example:
```
app
|_ __tests__
   |_MyComponent.test.jsx
|_MyComponent.jsx
```  

*Always create (or use if already exists) a `__tests__` directory.*


### File Structure

Another good pattern for testing is to have common structure.  At it's most simple the structure of the file should follow a `describe()` -> `it()` pattern.  What this means is you'd want to define which test group you're running within the `describe()` constructor, then define the action, method, or functionality you're testing within the `it()` constructor.
For example, if we are testing a component (`MyComponent.jsx`) that has two button's within it (Confirm and Cancel), the general setup would look like:
```
import ...
...
describe('Testing MyComponent`, () => {
    it('Rendering MyComponent button action confirm', () => {
        ...
    });
    it('Rendering MyComponent button action cancel', () => {
        ...
    });
});
```

You will see here that I have very clearly defined which component we are testing in the `describe()` method, as well as the action we are testing for individually within the `it()` methods.  How you want to devise up your tests is completely up to you.  If I had the ability to test both Confirm and Cancel within one test, then I might have done that.  The important information is to follow the `describe()` -> `it()` pattern and being clear/concise about what you are testing.