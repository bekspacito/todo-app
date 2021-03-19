import React,{ Component } from "react";

import { connect } from "react-redux";

import { loginAsync } from "../_actions";
import { userConstants } from "../_constants/auth.constants";

import "./Login.css";

class LoginPage extends Component {
    
    constructor(props) {
        super(props);
        this.state = {
            username : '',
            password : ''
        }

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleChange(event) {
        this.setState({
            [event.target.name] : event.target.value,
        })
    }

    handleSubmit(event) {
        this.props.login(this.state)
        event.preventDefault();
    }
    
    render() {

        let authState;
        switch (this.props.auth.userAuthState) {
            case userConstants.LOGIN_SUCCESS : 
                authState = <h1>You are already logged in</h1>;
                break;
            case userConstants.LOGIN_REQUEST: 
                authState = <h1>Wait for loggin to complete</h1>;
                break;
            default: 
                authState = <h1>You are not logged in</h1>;
        }

        return (
            <div>
                {authState}<br />
                <form onSubmit={this.handleSubmit} >
                    <div className="container">
                        <div className="container-elem" >
                            <label>
                                Username : 
                                <input type="text" name="username" value={this.state.username} onChange={this.handleChange} />
                            </label>
                        </div>
                        <div className="container-elem" >
                            <label>
                                Password : 
                                <input type="text" name="password" value={this.state.password} onChange={this.handleChange} />
                            </label>
                        </div>
                        <div className="container-elem" >
                            <input type="submit" value="Login" />
                        </div>
                    </div>
                </form>
            </div>
        );

    }
}

const mapStateToProps = (state) => {
    return {
        auth : state.auth
    };
}

const mapDispatchToProps = (dispatch) => {
    return {
        login: (loginData) => dispatch(loginAsync(loginData))
    };
}

export default connect(mapStateToProps, mapDispatchToProps) (LoginPage);