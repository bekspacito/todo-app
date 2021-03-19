import React from "react";
import { Route, Redirect } from "react-router-dom";

// Here value is passed from 'component' to 'Component'
// What you see as parameters is called 'object destructuring'
// three dots (...) at the end of the function parameters is 'rest parameters' and gathers the rest of the list of arguments into an array.
// three dots (...) in function call or alike, it's called a "spread operator" and expands an array into a list.
export const PrivateRoute = ({ component: Component, ...rest }) => {
    return <Route {...rest} render={
        props => (
            localStorage.getItem("user") 
                        ? <Component {...props} /> 
                        : <Redirect to={{ pathname: '/login' }}/>
        )}
    />
}