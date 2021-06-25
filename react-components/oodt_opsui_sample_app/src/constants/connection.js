import axios from "axios";

export const fmconnection = axios.create({
  baseURL: process.env.REACT_APP_FM_REST_API_URL
});

export const wmconnection = axios.create({
    baseURL: process.env.REACT_APP_WM_REST_API_URL
});