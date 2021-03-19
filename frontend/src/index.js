// React imports
import React from 'react';
import ReactDOM from 'react-dom';

// React-router imports
import { Router } from 'react-router-dom';

// Redux-imports
import { Provider } from "react-redux"
import { store } from "./_helpers"

import App from './App';

import { history } from "./_helpers"

ReactDOM.render(
  <Provider store={store} >
    <Router history={history} >
      <App />
    </Router>
  </Provider>,
  document.getElementById('root')
);