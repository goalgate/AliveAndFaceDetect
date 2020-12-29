package cn.cbsd.network;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ConnectApi {


    @POST("faceMatch")
    Observable<ResponseBody> faceCompare(@Query("key") String key, @Query("photo") String photo);


}
