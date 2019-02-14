package eu.mlab.civitas.android.rest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import eu.mlab.civitas.android.BookmarksFragment;
import eu.mlab.civitas.android.model.Artefact;
import eu.mlab.civitas.android.model.ArtefactItem;
import eu.mlab.civitas.android.model.Category;
import eu.mlab.civitas.android.model.RESTEndpoint;
import eu.mlab.civitas.android.util.AudioHandler;
import eu.mlab.civitas.android.util.BitmapHandler;
import eu.mlab.civitas.android.util.Util;

/**
 * Created by liisa_000 on 09/01/2017.
 * Updated by Christoph Stanik on 10/03/2017
 */

public class RESTVolleyHandler {
    public static final String TAG = RESTVolleyHandler.class.getSimpleName();
    private final String url = RESTEndpoint.BASE_URL;
    private RESTStringResponse restStringResponseCallback;
    private RESTJSONArrayResponse restJSONArrayResponseCallback;
    private RESTBitmapResponse restBitmapResponseCallback;
    private RESTAudioResponse restAudioResponseCallback;
    private Context context;

    public RESTVolleyHandler(Context context) {
        this.context = context;

    }

    public void setRestStringResponseCallbackHandler(Context context) {
        try {
            this.restStringResponseCallback = (RESTStringResponse) context;
        } catch (ClassCastException e) {
            // not the AddArtefactActivity.java activity
            e.printStackTrace();
        }
    }

    public void setRestJSONArrayResponseCallbackHandler(Context context) {
        try {
            this.restJSONArrayResponseCallback = (RESTJSONArrayResponse) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public void setRestBitmapResponseCallbackHandler(Context context) {
        try {
            this.restBitmapResponseCallback = (RESTBitmapResponse) context;
        } catch (ClassCastException e) {
            // not the AddArtefactActivity.java activity
            e.printStackTrace();
        }
    }

    public void setRestAudioResponseCallbackHandler(Context context) {
        try {
            this.restAudioResponseCallback = (RESTAudioResponse) context;
        } catch (ClassCastException e) {
            // not the AddArtefactActivity.java activity
            e.printStackTrace();
        }
    }

    public void registerUser(final String firstName, final String lastName, final String email, final String userRole) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, RESTEndpoint.BASE_URL + RESTEndpoint.REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        restStringResponseCallback.onResponse(response, Util.RESPONSE_REGISTER_USER);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        restStringResponseCallback.onErrorResponse(error, Util.RESPONSE_REGISTER_USER);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Util.PARAM_FIRST_NAME, firstName);
                params.put(Util.PARAM_LAST_NAME, lastName);
                params.put(Util.PARAM_EMAIL, email);
                params.put(Util.PARAM_USER_ROLE, userRole);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public ArrayList loadAllArtefacts() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String suburl = RESTEndpoint.BASE_URL + RESTEndpoint.ARTEFACTS;
        final ArrayList<Artefact> list = new ArrayList<>();

        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, suburl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                restJSONArrayResponseCallback.onResponse(response, Util.RESPONSE_LOAD_ALL_ARTEFACTS);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                restJSONArrayResponseCallback.onErrorResponse(error, Util.RESPONSE_LOAD_ALL_ARTEFACTS);
            }
        });

        queue.start();
        queue.add(jsonRequest);

        return list;
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public void uploadArtefact(final Artefact artefact) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, RESTEndpoint.BASE_URL + RESTEndpoint.ARTEFACTS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        restStringResponseCallback.onResponse(response, Util.RESPONSE_ARTEFACT_UPLOAD);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        restStringResponseCallback.onErrorResponse(volleyError, Util.RESPONSE_ARTEFACT_UPLOAD);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp  = dateFormat.format(new Date());

                Map<String, String> params = new HashMap<>();
                if((artefact.getId() != 0) && (artefact.getId() != -1)){
                    params.put(Util.PARAM_ID, String.valueOf(artefact.getId()));
                }
                params.put(Util.PARAM_ARTEFACT_NAME, artefact.getName());
                params.put(Util.PARAM_CREATION_DATE, timestamp);
                params.put(Util.PARAM_LONGITUDE, String.valueOf(artefact.getLongitude()));
                params.put(Util.PARAM_LATITUDE, String.valueOf(artefact.getLatitude()));
                params.put(Util.PARAM_DATE, artefact.getDating());
                params.put(Util.PARAM_CATEGORY_ID, String.valueOf(artefact.getCategory().getId()));
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(stringRequest);
    }

    public void uploadArtefactItem(final int artefactId, final ArtefactItem item, final String userId) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String suburl = url + RESTEndpoint.GET_ARTEFACT_BY_ID + artefactId + RESTEndpoint.GET_ARTEFACT_ITEMS;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, suburl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("uploadResponse", s);
                        restStringResponseCallback.onResponse(s, Util.RESPONSE_POST_ARTEFACT_ITEM);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        restStringResponseCallback.onErrorResponse(volleyError, Util.RESPONSE_POST_ARTEFACT_ITEM);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(Util.PARAM_ARTEFACT_ID, String.valueOf(artefactId));
                params.put(Util.PARAM_ARTEFACT_ITEM_USER_ID, userId);
                params.put(Util.PARAM_ARTEFACT_ITEM_DESCRIPTION, item.getDescription());
                params.put(Util.PARAM_ARTEFACT_ITEM_IMAGE_NAME, item.getImagePath());
                params.put(Util.PARAM_ARTEFACT_ITEM_AUDIO_NAME, item.getAudioPath());
                params.put(Util.PARAM_ARTEFACT_ITEM_LICENSE_TYPE, item.getImageLicense());
                params.put(Util.PARAM_ARTEFACT_ITEM_LICENSE_LINK, item.getImageLicenseLink());
                params.put(Util.PARAM_ARTEFACT_ITEM_AUTHOR, item.getAuthor());
                params.put(Util.PARAM_ARTEFACT_ITEM_LANGUAGE, item.getLanguage());

                //returning parameters
                for (Map.Entry<String,String> entry : params.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    Log.d(TAG, "params: " + key + " " + value);
                }

                return params;
            }
        };
        queue.start();
        queue.add(stringRequest);
    }

    public void uploadImage(final String path, final String name) {
        String suburl = url + RESTEndpoint.POST_IMAGE;

        RequestQueue queue = Volley.newRequestQueue(context);
        final Bitmap bitmap = BitmapHandler.decodeFile(path, context);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, suburl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("uploadResponse", s);
                        restStringResponseCallback.onResponse(s, Util.RESPONSE_POST_ARTEFACT_IMAGE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        restStringResponseCallback.onErrorResponse(volleyError, Util.RESPONSE_POST_ARTEFACT_IMAGE);

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                //Converting Bitmap to String
                String image = BitmapHandler.transformImageToString(bitmap);


                //Adding parameters
                params.put("name", name);
                params.put("imageFile", image);


                for (Map.Entry<String,String> entry : params.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    Log.d(TAG, "params: " + key + " " + value);
                }

                //returning parameters
                return params;
            }
        };
        queue.start();
        queue.add(stringRequest);
    }

    public void uploadAudio(final String path, final String name) {
        String suburl = url + RESTEndpoint.POST_AUDIO;

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, suburl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("uploadResponse", s);
                        restStringResponseCallback.onResponse(s, Util.RESPONSE_POST_ARTEFACT_AUDIO);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        restStringResponseCallback.onErrorResponse(volleyError, Util.RESPONSE_POST_ARTEFACT_AUDIO);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                //Converting Bitmap to String
                String audio = AudioHandler.transformAudioToString(new File(path), context);


                //Adding parameters
                params.put("name", name);
                params.put("audioFile", audio);

                //returning parameters
                return params;
            }
        };
        queue.start();
        queue.add(stringRequest);
    }

    public void downloadImageFile(final String imageFileName) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String suburl = url + RESTEndpoint.GET_IMAGE;
        ImageRequest request = new ImageRequest(suburl + RESTEndpoint.ATTR_NAME + imageFileName,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        restBitmapResponseCallback.onResponse(response, Util.RESPONSE_LOAD_ARTEFACT_IMAGE, imageFileName);
                    }
                }, 0, 0, null, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        restBitmapResponseCallback.onErrorResponse(error, Util.RESPONSE_LOAD_ARTEFACT_IMAGE);
                    }
                }
        );

        queue.start();
        queue.add(request);
    }

    public void downloadAudioFile(final String audioFileName) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(context, new HurlStack());
        String suburl = url + RESTEndpoint.GET_AUDIO + RESTEndpoint.ATTR_NAME;
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, suburl + audioFileName,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        restAudioResponseCallback.onResponse(response, Util.RESPONSE_LOAD_ARTEFACT_AUDIO, audioFileName);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        restAudioResponseCallback.onErrorResponse(error, Util.RESPONSE_LOAD_ARTEFACT_AUDIO);
                    }
                }, null);
        mRequestQueue.add(request);
    }

    public void loadAllArtefactItemsByArtefactID(int id) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String suburl = url + RESTEndpoint.GET_ARTEFACT_BY_ID + id + RESTEndpoint.GET_ARTEFACT_ITEMS;
        JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET, suburl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                restJSONArrayResponseCallback.onResponse(response, Util.RESPONSE_LOAD_ARTEFACT_ITEMS);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                restJSONArrayResponseCallback.onErrorResponse(error, Util.RESPONSE_LOAD_ARTEFACT_ITEMS);
            }
        }) {};
        queue.start();
        queue.add(jsonRequest);
    }

    public void login(final String email, final String password) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = null;
        try {
            stringRequest = new StringRequest(Request.Method.POST,
                    RESTEndpoint.BASE_URL + RESTEndpoint.LOGIN,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // get response
                            Log.d(TAG, "response=" + response);
                            restStringResponseCallback.onResponse(response, Util.RESPONSE_LOGIN_USER);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "error=" + error.toString());
                    restStringResponseCallback.onErrorResponse(error, Util.RESPONSE_LOGIN_USER);
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put(Util.PARAM_EMAIL, email);
                    params.put(Util.PARAM_PASSWORD, password);
                    return params;
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return stringRequest;

//        queue.start();
        queue.add(stringRequest);
    }

    public void loadAllArtefactByUserID(int userID, final Object obj) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String suburl = url + "items/favorites/" + userID;
        final ArrayList<Artefact> list = new ArrayList<>();

        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, suburl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject o = response.getJSONObject(i);
                        list.add(new Artefact(o.getInt("id"), o.getString("name"), o.getString("dating"), o.getDouble("latitude"), o.getDouble("longitude"), o.getJSONObject("category").getInt("id")));
                        ((BookmarksFragment) obj).addCard(list);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        queue.start();
        queue.add(jsonRequest);
    }

    public void deleteArtefactItems(final Artefact artefact) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, RESTEndpoint.BASE_URL + RESTEndpoint.ARTEFACTS + RESTEndpoint.GET_DELETE + RESTEndpoint.POST_ARTEFACT + artefact.getId(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        restStringResponseCallback.onResponse(response, Util.RESPONSE_DELETE_ARTEFACT_ITEMS);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        restStringResponseCallback.onErrorResponse(error, Util.RESPONSE_DELETE_ARTEFACT_ITEMS);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void deleteArtefact(final Artefact artefact) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, RESTEndpoint.BASE_URL + RESTEndpoint.ARTEFACTS + RESTEndpoint.GET_DELETE + artefact.getId(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        restStringResponseCallback.onResponse(response, Util.RESPONSE_DELETE_ARTEFACT);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        restStringResponseCallback.onErrorResponse(error, Util.RESPONSE_DELETE_ARTEFACT);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public ArrayList loadAllCategories(){
        RequestQueue queue = Volley.newRequestQueue(context);
        String suburl = RESTEndpoint.BASE_URL + RESTEndpoint.CATEGORIES;
        final ArrayList<Category> list = new ArrayList<>();

        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, suburl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                restJSONArrayResponseCallback.onResponse(response, Util.RESPONSE_LOAD_ALL_CATEGORIES);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                restJSONArrayResponseCallback.onErrorResponse(error, Util.RESPONSE_LOAD_ALL_CATEGORIES);
            }
        });

        queue.start();
        queue.add(jsonRequest);

        return list;
    }

    public void getUserRole(final String userEmail) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = null;
        try {
            stringRequest = new StringRequest(Request.Method.GET,
                    RESTEndpoint.BASE_URL + RESTEndpoint.GET_USER_ROLE,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // get response
                            Log.d(TAG, "response=" + response);
                            restStringResponseCallback.onResponse(response, Util.RESPONSE_LOAD_USER_ROLE);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "error=" + error.toString());
                    restStringResponseCallback.onErrorResponse(error, Util.RESPONSE_LOAD_USER_ROLE);
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put(Util.PARAM_EMAIL, userEmail);
                    return params;
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        queue.add(stringRequest);
    }

    public interface RESTStringResponse {
        void onResponse(String response, int responseCode);

        void onErrorResponse(VolleyError error, int responseCode);
    }

    public interface RESTJSONArrayResponse {
        void onResponse(JSONArray response, int responseCode);

        void onErrorResponse(VolleyError error, int responseCode);
    }

    public interface RESTBitmapResponse {
        void onResponse(Bitmap response, int responseCode, String imagePath);

        void onErrorResponse(VolleyError error, int responseCode);
    }

    public interface RESTAudioResponse {
        void onResponse(byte[] response, int responseCode, String audioPath);

        void onErrorResponse(VolleyError error, int responseCode);
    }
}
