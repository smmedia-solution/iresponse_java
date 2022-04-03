package tech.iresponse.interfaces;

import tech.iresponse.http.Response;

public interface Controller {
    Response controller(String action) throws Exception;
}
