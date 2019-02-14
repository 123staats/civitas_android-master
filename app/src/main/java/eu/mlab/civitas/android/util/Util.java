package eu.mlab.civitas.android.util;

/**
 * Created by christophstanik on 3/8/17.
 */

public class Util {
    public static final String PROVIDER = "eu.mlab.civitas.android.provider";

    //public static final String INTRODUCTION_URL = "http://mlab-vm1.informatik.uni-hamburg.de";
    //public static final String INTRODUCTION_URL = "https://mast-civitas.informatik.uni-hamburg.de";
    //public static final String INTRODUCTION_URL = "http://194.87.146.153";
    public static final String INTRODUCTION_URL = "http://192.168.2.73";
    //public static final String INTRODUCTION_URL = "https://localhost";

    public static final String FILE_DIR_IMAGES = "images";
    public static final String FILE_DIR_AUDIO = "audio";

    public static final int AUDIO_PLAYER_PLAY = 2;
    public static final int AUDIO_PLAYER_PAUSE = 1;
    public static final int AUDIO_PLAYER_STOP = 0;

    public static final String PREF_KEY_FILE = "PREF_KEY_FILE";
    public static final String PREF_KEY_ZOOM_TO_CURRENT_LOCATION = "PREF_KEY_ZOOM_TO_CURRENT_LOCATION";
    public static final String PREF_USER_MAIL = "PREF_USER_MAIL";
    public static final String PREF_USER_FIRST_NAME = "PREF_USER_FIRST_NAME";
    public static final String PREF_USER_LAST_NAME = "PREF_USER_LAST_NAME";
    public static final String PREF_USER_ID = "PREF_USER_ID";
    public static final String EMAIL_MAP_KEY = "email";
    public static boolean readMode = false;

    // intent requests
    public static final int ARTEFACT_ITEM_UPDATE_REQUEST = 2;
    public static final int ADD_ARTEFACT_ELEMENT_REQUEST = 3;
    public static final int PICTURE_REQUEST = 4;
    public static final int PICTURE_UPDATE_REQUEST = 5;
    public static final int CAMERA_IMAGE_REQUEST = 6;

    // intent keys
    public static final String INTENT_ARTEFACT = "ARTEFACT";
    public static final String INTENT_ARTEFACT_ITEM = "INTENT_ARTEFACT_ITEM";
    public static final String INTENT_ARTEFACT_ITEM_POSITION = "INTENT_ARTEFACT_ITEM_POSITION";
    public static final String INTENT_REQUEST_CODE = "INTENT_REQUEST_CODE";
    public static final String INTENT_IMAGE_NAME = "INTENT_IMAGE_NAME";
    public static final String INTENT_IMAGE_PATH = "INTENT_IMAGE_PATH";
    public static final String INTENT_IMAGE_URI = "INTENT_IMAGE_URI";
    public static final String INTENT_IMAGE_AUTHOR = "INTENT_IMAGE_AUTHOR";
    public static final String INTENT_IMAGE_LICENSE = "INTENT_IMAGE_LICENSE";
    public static final String INTENT_IMAGE_LICENSE_LINK = "INTENT_IMAGE_LICENSE_LINK";

    // REST keys
    public static final String PARAM_ARTEFACT_NAME = "name";
    public static final String PARAM_LONGITUDE = "longitude";
    public static final String PARAM_LATITUDE = "latitude";
    public static final String PARAM_DATE = "dating";
    public static final String PARAM_CREATION_DATE = "creationDate";
    public static final String PARAM_CATEGORY_ID = "categoryId";
    public static final String PARAM_FIRST_NAME = "firstName";
    public static final String PARAM_LAST_NAME = "lastName";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_ARTEFACT_ITEM_DESCRIPTION = "description";
    public static final String PARAM_STATUS = "status";
    public static final String PARAM_ARTEFACT_ID = "artefactId";
    public static final String PARAM_ARTEFACT_ITEM_USER_ID = "userId";
    public static final String PARAM_ARTEFACT_ITEM_IMAGE_NAME = "imageName";
    public static final String PARAM_ARTEFACT_ITEM_IMAGE_FILE = "imageFile";
    public static final String PARAM_ARTEFACT_ITEM_AUDIO_NAME = "audioName";
    public static final String PARAM_ARTEFACT_ITEM_AUDIO_FILE = "audioFile";
    public static final String PARAM_ARTEFACT_ITEM_LICENSE_TYPE = "licenceType";
    public static final String PARAM_ARTEFACT_ITEM_LICENSE_LINK = "licenceLink";
    public static final String PARAM_ARTEFACT_ITEM_AUTHOR = "author";
    public static final String PARAM_ARTEFACT_ITEM_LANGUAGE = "language";
    public static final String PARAM_USER_ROLE = "userRole";
    public static final String PARAM_USER_EMAIL = "userEmail";
    public static final String PARAM_ID = "id";
    public static final String PARAM_ = "";

    // REST response codes
    public static final int RESPONSE_ARTEFACT_UPLOAD = 0;
    public static final int RESPONSE_REGISTER_USER = 1;
    public static final int RESPONSE_LOGIN_USER = 2;
    public static final int RESPONSE_LOAD_ALL_ARTEFACTS = 3;
    public static final int RESPONSE_LOAD_ARTEFACT_ITEMS = 4;
    public static final int RESPONSE_LOAD_ARTEFACT_IMAGE = 5;
    public static final int RESPONSE_LOAD_ARTEFACT_AUDIO = 6;
    public static final int RESPONSE_POST_ARTEFACT_IMAGE = 7;
    public static final int RESPONSE_POST_ARTEFACT_AUDIO = 8;
    public static final int RESPONSE_POST_ARTEFACT_ITEM = 9;
    public static final int RESPONSE_DELETE_ARTEFACT = 10;
    public static final int RESPONSE_LOAD_ALL_CATEGORIES = 11;
    public static final int RESPONSE_LOAD_USER_ROLE = 12;
    public static final int RESPONSE_DELETE_ARTEFACT_ITEMS = 13;

    // JSON keys
    public static final String JSON_FIRST_NAME = "firstName";
    public static final String JSON_LAST_NAME = "lastName";
    public static final String JSON_USER_ID = "id";
    public static final String JSON_EMAIL = "email";
    public static final String JSON_USER_ROLE = "userRole";
    public static final String JSON_ARTEFACT_ID = "id";
    public static final String JSON_ARTEFACT_NAME = "name";
    public static final String JSON_ARTEFACT_DATING = "dating";
    public static final String JSON_ARTEFACT_LONGITUDE = "longitude";
    public static final String JSON_ARTEFACT_LATITUDE = "latitude";
    public static final String JSON_ARTEFACT_CATEGORY = "categorie";
    public static final String JSON_ARTEFACT_CATEGORY_ID = "id";
    public static final String JSON_ARTEFACT_CATEGORY_NAME = "name";
    public static final String JSON_ARTEFACT_ITEM_IMAGE_NAME = "imageName";
    public static final String JSON_ARTEFACT_ITEM_AUDIO_NAME = "audioName";
    public static final String JSON_ARTEFACT_ITEM_DESCRIPTION = "description";
    public static final String JSON_ARTEFACT_ITEM_LICENSE_TYPE = "licenceType";
    public static final String JSON_ARTEFACT_ITEM_LICENSE_LINK = "licenceLink";
    public static final String JSON_ARTEFACT_ITEM_AUTHOR = "author";
    public static final String JSON_ = "";
}