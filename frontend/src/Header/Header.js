import React from "react";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from  "@material-ui/core/Toolbar";
import Avatar from  "@material-ui/core/Avatar";
import InputBase from "@material-ui/core/InputBase"; 
import SearchIcon from "@material-ui/icons/Search";
import { fade, makeStyles } from '@material-ui/core/styles';

import logo1 from "../_static/FLNGN_LOGO_3.png"

const useStyles = makeStyles((theme) => ({
    root: {
      flexGrow: 1,
    },
    search: {
        position: 'relative',
        borderRadius: theme.shape.borderRadius,
        backgroundColor: fade(theme.palette.common.white, 0.15),
        '&:hover': {
          backgroundColor: fade(theme.palette.common.white, 0.25),
        },
        marginLeft: 0,
        width: '100%',
        [theme.breakpoints.up('sm')]: {
          marginLeft: theme.spacing(1),
          width: 'auto',
        },
    }
}));

export default function Header() {

    const classes = useStyles();

    return (
        <header>
            <div className={classes.root}>
                <AppBar position="static" >
                    <Toolbar>
                        <Avatar src={logo1} alt="logo"/>
                        <div >
                            <div >
                                <SearchIcon />
                                </div>
                                <InputBase
                                placeholder="Search…"
                                inputProps={{ 'aria-label': 'search' }}
                            />
                        </div>
                    </Toolbar>
                </AppBar>
            </div>
        </header>
    );
}