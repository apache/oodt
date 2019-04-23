import axios from "axios";

export const fmconnection = axios.create({
    baseURL: 'http://46.4.26.22:8012/fmprod/jaxrs',
});
