package eu.mlab.civitas.android;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Christoph on 11.01.2017.
 */

public interface HistoricMapApi {
    @GET("/api/geojson.php")
    Call<String> getData(
            @Query("bbox") String area,
            @Query("zoom") int zoom);
}
