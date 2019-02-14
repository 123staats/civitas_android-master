package eu.mlab.civitas.android.model;

/**
 * Created by christophstanik on 3/10/17.
 */

public class RESTEndpoint {
    //public static final String BASE_URL = "http://194.87.146.153:8080/";
    public static final String BASE_URL = "http://192.168.43.191:8080/";
    public static final String LOGIN = "login/";
    public static final String GET_USER_ROLE = "userRole/";
    public static final String ARTEFACTS = "artefacts/";
    public static final String CATEGORIES = "categories/";
    public static final String REGISTER = "register/";
    public static final String POST_ARTEFACT = "items/";
    public static final String GET_DELETE = "delete/";
    public static final String GET_ARTEFACT_ITEMS = "/items/";
    public static final String POST_ARTEFACT_ITEM = "uploadArtefactItem";
    public static final String POST_IMAGE = "imageFile";
    public static final String GET_IMAGE = "imageFile";
    public static final String POST_AUDIO = "audioFile";
    public static final String GET_AUDIO = "audioFile";
    public static final String GET_ARTEFACT_BY_ID = ARTEFACTS; // + artefactId
    public static final String GET_ARTEFACT_FAV = "items/favorites/";
    public static final String ATTR_NAME = "?name=";
}
