
import React from "react";

import {
  Route,
  Switch,
  Redirect,
  withRouter
} from "react-router-dom";

import { PrivateRoute } from "./../_components"

import Home from "../HomePage";
import LoginPage from "../LoginPage";

function App() {
  return (
    <div className="App">
        <Switch>
          <PrivateRoute path="/home" component={Home} />
          <Route path="/login" component={LoginPage} />
          <Redirect from="/" to="/home" />
        </Switch>
    </div>
  );
}

export default withRouter(App);
