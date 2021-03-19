import { createStore, combineReducers, applyMiddleware } from "redux";

import thunk from "redux-thunk";

import { AuthReducer } from "./../_reducers";

const rootReducer = combineReducers({
    auth : AuthReducer
});

export const store = createStore(rootReducer, applyMiddleware(thunk));