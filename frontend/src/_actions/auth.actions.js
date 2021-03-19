import { userConstants } from "./../_constants"

import { history } from "../_helpers";

export const loginAsync = loginData => dispatch => {

    dispatch({ type : userConstants.LOGIN_REQUEST });

    setTimeout(() => {
        const username = loginData.username;
        const password = loginData.password;

        if(username === "Myrza" && password === "123") {
            localStorage.setItem("user", JSON.stringify({
                username : username,
                token : "aaabbb123"
            }));
            history.push("/home")
            return dispatch({ type : userConstants.LOGIN_SUCCESS });
        }
        else {
            return dispatch({
                type : userConstants.LOGIN_FAILURE,
                msg : "Could't log in user"
            });
        }
    }, 2000);

}